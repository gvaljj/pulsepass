package com.pulsepass.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

// PRD §16 (SecurityFilterChain stateless) y matriz de autorización §10.
// Denegación por defecto: cualquier ruta no listada exige autenticación (anyRequest().authenticated()).
// El orden de los matchers importa: los patrones específicos se declaran ANTES de los genéricos
// (p. ej. /api/events/{eventCode}/tickets/paid antes que /api/events/{eventCode}).
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // refuerzo en métodos de servicio cuando corresponda (PRD §16)
public class SecurityConfig {

    // NFR-SEC-008: orígenes permitidos para CORS, parametrizados y vacíos por defecto
    // (deny-by-default: sin la variable de entorno no se habilita ningún origen cross-origin).
    @Value("${pulsepass.security.cors.allowed-origins:}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   CustomUserDetailsService userDetailsService,
                                                   RestAuthenticationEntryPoint authenticationEntryPoint,
                                                   RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        // Instanciados aquí (y no como @Bean) para que Spring Boot no los registre dos veces como
        // Filter del contenedor: se ejecutan exclusivamente dentro de la cadena de seguridad.
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        // NFR-SEC-006: inyecta el request ID en el MDC para correlacionar respuesta y logs.
        RequestIdFilter requestIdFilter = new RequestIdFilter();

        http
            // API stateless con Bearer: sin sesión y sin CSRF (PRD §9.4)
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // NFR-SEC-008: política CORS parametrizada (pulsepass.security.cors.allowed-origins);
            // lista vacía por defecto -> ningún origen cross-origin (deny-by-default)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                    // --- Públicos (sin token): registro, login y catálogos
                    .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/events/published").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/events/by-artist").permitAll() // EVT-06: borradores filtrados en el Service
                    .requestMatchers(HttpMethod.GET, "/api/artists/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/venues/**").permitAll()

                    // --- Escritura de eventos -> solo ADMIN (EVT-03/04/05)
                    .requestMatchers(HttpMethod.POST, "/api/events").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/events/*/publish").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/events/*/artists/*").hasRole("ADMIN")

                    // --- Operaciones de staff -> STAFF o ADMIN (TKT-04, TKT-06)
                    .requestMatchers(HttpMethod.GET, "/api/events/*/tickets/paid").hasAnyRole("STAFF", "ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/use").hasAnyRole("STAFF", "ADMIN")

                    // --- Lectura pública de evento por código -> permitAll; el Service oculta borradores
                    //     (respuesta idéntica a 404 para no revelar su existencia, EVT-02/T-SEC-017)
                    .requestMatchers(HttpMethod.GET, "/api/events/*").permitAll()

                    // --- Autoservicio -> solo USER; la propiedad se comprueba en el Service con el id persistente
                    .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("USER")

                    // --- Identidad del token (siempre autenticada)
                    .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()

                    // --- Denegación por defecto: el resto de rutas exige autenticación
                    //     (usuarios, tickets, etc. La propiedad anti-IDOR vive en AuthorizationService)
                    .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
            // Orden: request id -> ExceptionTranslationFilter -> filtro JWT -> autorización. El filtro JWT
            // se registra primero para que sus posiciones no interfieran; RequestIdFilter se ancla a
            // ExceptionTranslationFilter (filtro nativo con orden registrado) para que ABARQUE las
            // denegaciones 401/403: si se situara dentro, su bloque finally limpiaría el MDC antes de
            // que EntryPoint/DeniedHandler escribieran el log y el requestId se perdería.
            .addFilterBefore(jwtAuthenticationFilter, AuthorizationFilter.class)
            .addFilterBefore(requestIdFilter, ExceptionTranslationFilter.class);

        return http.build();
    }

    // NFR-SEC-008: CORS parametrizado y deny-by-default. Se expone como bean CorsConfigurationSource
    // para que Spring Security lo aplique a la cadena (ver .cors(...)); sin orígenes configurados, el
    // navegador no recibe cabeceras Access-Control-Allow-Origin y las peticiones cross-origin se bloquean.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(this.allowedOrigins)
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        if (!origins.isEmpty()) {
            configuration.setAllowedOrigins(origins);
            // Solo los métodos que expone la API (PRD §10) y los métodos idempotentes de lectura.
            configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
            // Cabeceras usadas por la API: autenticación Bearer y JSON.
            configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
            // El navegador no necesita leer cabeceras de la respuesta, pero X-Request-Id ayuda a
            // correlacionar incidencias reportadas por el cliente (NFR-SEC-006).
            configuration.setExposedHeaders(List.of("X-Request-Id"));
        }
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    // PRD §9.1: contraseñas siempre como hash BCrypt, nunca en claro
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Login por username o email contra CustomUserDetailsService con el PasswordEncoder BCrypt
    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
                                                        PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }
}
