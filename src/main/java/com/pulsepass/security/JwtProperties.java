package com.pulsepass.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Base64;

// PRD §9.1: configuración JWT obligatoria (issuer, secreto Base64 >= 32 bytes, TTL).
// La clave secreta vive SOLO en la variable de entorno PULSEPASS_JWT_SECRET_BASE64 (nunca en el repositorio);
// la aplicación DEBE fallar al arrancar si la clave falta o es insuficiente (deny-by-default del emisor de tokens).
// Los @Value van en el PARÁMETRO DEL CONSTRUCTOR (no en campos): así, cuando una prueba construye el bean a
// mano con la clave de tests, Spring no lo sobreescribe con la inyección de propiedades.
@Component
public class JwtProperties {

    public static final int MIN_SECRET_BYTES = 32;

    private static final String SECRET_PROPERTY = "pulsepass.security.jwt.secret-base64";
    private static final String SECRET_ENV = "PULSEPASS_JWT_SECRET_BASE64";

    private final String issuer;
    private final String secretBase64;
    private final String accessTokenTtl;

    public JwtProperties(@Value("${pulsepass.security.jwt.issuer:pulsepass-api}") String issuer,
                         @Value("${pulsepass.security.jwt.secret-base64:}") String secretBase64,
                         @Value("${pulsepass.security.jwt.access-token-ttl:PT15M}") String accessTokenTtl) {
        this.issuer = issuer;
        this.secretBase64 = secretBase64;
        this.accessTokenTtl = accessTokenTtl;
    }

    @PostConstruct
    void validateAtStartup() {
        // Falla en el arranque (startup) si el secreto falta, no es Base64 o es corto
        decodedSecret();
        // y si el TTL no es una duración ISO-8601 válida mayor que cero
        getAccessTokenTtl();
    }

    // Decodifica y valida la clave secreta; lanza IllegalStateException con mensaje accionable si no cumple.
    public byte[] decodedSecret() {
        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException(
                    "Falta la variable de entorno " + SECRET_ENV + " (" + SECRET_PROPERTY + "). "
                    + "Debe contener una clave Base64 de al menos " + MIN_SECRET_BYTES
                    + " bytes para firmar JWT con HS256. La aplicación no puede iniciar sin ella.");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secretBase64.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    SECRET_ENV + " (" + SECRET_PROPERTY + ") no es una clave Base64 válida.", ex);
        }
        if (decoded.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    SECRET_ENV + " (" + SECRET_PROPERTY + ") debe decodificar al menos "
                    + MIN_SECRET_BYTES + " bytes; se recibieron " + decoded.length
                    + " bytes. Genere una clave más larga (p. ej.: openssl rand -base64 48).");
        }
        return decoded;
    }

    public String getIssuer() { return issuer; }

    public String getSecretBase64() { return secretBase64; }

    public Duration getAccessTokenTtl() {
        // ISO-8601 (p. ej. PT15M); la validación explícita evita silenciar un TTL mal formado.
        // Nota: Duration.parse lanza DateTimeParseException, que NO es subclase de IllegalArgumentException.
        Duration ttl;
        try {
            ttl = Duration.parse(accessTokenTtl);
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "pulsepass.security.jwt.access-token-ttl no es una duración ISO-8601 válida: " + accessTokenTtl, ex);
        }
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalStateException(
                    "pulsepass.security.jwt.access-token-ttl debe ser mayor que cero: " + accessTokenTtl);
        }
        return ttl;
    }
}
