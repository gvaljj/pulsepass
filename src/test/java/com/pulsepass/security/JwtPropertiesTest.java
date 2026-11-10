package com.pulsepass.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// PRD §9.1: la configuración JWT DEBE fallar de forma explícita (startup) si el secreto falta o es insuficiente.
class JwtPropertiesTest {

    private static final String ISSUER = "pulsepass-api";
    private static final String VALID_SECRET = SecurityTestConfiguration.TEST_SECRET_BASE64;
    private static final String VALID_TTL = "PT15M";

    private JwtProperties properties(String issuer, String secret, String ttl) {
        return new JwtProperties(issuer, secret, ttl);
    }

    @Test
    @DisplayName("Secreto Base64 válido >= 32 bytes se decodifica correctamente")
    void decodedSecret_SecretoValido_RetornaBytes() {
        byte[] decoded = properties(ISSUER, VALID_SECRET, VALID_TTL).decodedSecret();

        assertThat(decoded).hasSizeGreaterThanOrEqualTo(JwtProperties.MIN_SECRET_BYTES);
        assertThat(decoded).isEqualTo(Base64.getDecoder().decode(VALID_SECRET));
    }

    @Test
    @DisplayName("Secreto ausente -> IllegalStateException con la variable de entorno accionable (arranque denegado)")
    void decodedSecret_SecretoVacio_LanzaIllegalState() {
        assertThatThrownBy(() -> properties(ISSUER, "", VALID_TTL).decodedSecret())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PULSEPASS_JWT_SECRET_BASE64")
                .hasMessageContaining("32");
    }

    @Test
    @DisplayName("Secreto con menos de 32 bytes -> IllegalStateException que exige una clave más larga")
    void decodedSecret_SecretoCorto_LanzaIllegalState() {
        String shortSecret = Base64.getEncoder().encodeToString("1234567890123456".getBytes()); // 16 bytes

        assertThatThrownBy(() -> properties(ISSUER, shortSecret, VALID_TTL).decodedSecret())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("al menos 32 bytes");
    }

    @Test
    @DisplayName("Secreto que no es Base64 -> IllegalStateException")
    void decodedSecret_NoEsBase64_LanzaIllegalState() {
        assertThatThrownBy(() -> properties(ISSUER, "no-es-base64!!", VALID_TTL).decodedSecret())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64");
    }

    @Test
    @DisplayName("TTL ISO-8601 válido se devuelve como Duration")
    void getAccessTokenTtl_DuracionValida_RetornaDuration() {
        assertThat(properties(ISSUER, VALID_SECRET, "PT15M").getAccessTokenTtl()).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("TTL no ISO-8601 -> IllegalStateException (no se silencia un TTL mal formado)")
    void getAccessTokenTtl_DuracionInvalida_LanzaIllegalState() {
        assertThatThrownBy(() -> properties(ISSUER, VALID_SECRET, "15 minutos").getAccessTokenTtl())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ISO-8601");
    }

    @Test
    @DisplayName("TTL cero o negativo -> IllegalStateException (el token no expiraría)")
    void getAccessTokenTtl_DuracionNoPositiva_LanzaIllegalState() {
        assertThatThrownBy(() -> properties(ISSUER, VALID_SECRET, "PT0S").getAccessTokenTtl())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mayor que cero");
    }
}
