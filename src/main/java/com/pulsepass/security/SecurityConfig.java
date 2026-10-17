package com.pulsepass.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

// PRD §16 (SecurityFilterChain stateless) y matriz de autorización §10.
// Denegación por defecto: cualquier ruta no listada exige autenticación (anyRequest().authenticated()).
// El orden de los matchers importa: los patrones específicos se declaran ANTES de los genéricos
// (p. ej. /api/events/{eventCode}/tickets/paid antes que /api/events/{eventCode}).
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // refuerzo en métodos de servicio cuando corresponda (PRD §16)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   CustomUserDetailsService userDetailsService,
                                                   RestAuthenticationEntryPoint authenticationEntryPoint,
                                                   RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        // Instanciado aquí (y no como @Bean) para que Spring Boot no lo registre dos veces como
        // Filter del contenedor: se ejecuta exclusivamente dentro de la cadena de seguridad.
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        http
            // API stateless con Bearer: sin sesión y sin CSRF (PRD §9.4)
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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
            .addFilterBefore(jwtAuthenticationFilter, AuthorizationFilter.class);

        return http.build();
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
