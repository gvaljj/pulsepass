package com.pulsepass.security;

import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

// Configuración compartida para las pruebas @WebMvcTest de la Capa 3 (PRD §17.4):
// importa la cadena de seguridad REAL (SecurityConfig + sus dependencias) en la slice, sin usar
// jamás addFilters=false. Cada prueba configura su identidad con @WithMockUser / @WithAnonymousUser.
@TestConfiguration(proxyBeanMethods = false)
@Import({SecurityConfig.class,
        JwtService.class,
        CustomUserDetailsService.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        SecurityErrorResponseWriter.class})
public class SecurityTestConfiguration {

    // Clave Base64 EXCLUSIVA del entorno de pruebas (44 bytes decodificados). Nunca la de producción.
    public static final String TEST_SECRET_BASE64 = "cHVsc2VwYXNzLWZhc2U0LXNlY3JldC1rZXktZm9yLXRlc3RzLTMyYnl0ZXM=";

    @Bean
    public JwtProperties jwtProperties() {
        return new JwtProperties("pulsepass-api", TEST_SECRET_BASE64, "PT15M");
    }

    // La slice no carga JPA: se sustituyen los repositorios que necesita CustomUserDetailsService
    // por mocks. Ninguna de estas pruebas de contrato depende de datos reales de identidad.
    @Bean
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    public UserCredentialRepository userCredentialRepository() {
        return Mockito.mock(UserCredentialRepository.class);
    }

    @Bean
    public UserRoleRepository userRoleRepository() {
        return Mockito.mock(UserRoleRepository.class);
    }
}
