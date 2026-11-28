package com.pulsepass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

// PRD §9.2: emisión y validación de JWT firmados con HS256 (JJWT 0.13.x).
// Validación explícita de: algoritmo HS256, issuer, y claims obligatorios sub, iat, exp, jti.
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        // Clave derivada de PULSEPASS_JWT_SECRET_BASE64 (>= 32 bytes). WeakKeyException si no cumple:
        // el bean no se crea y la aplicación no arranca (deny-by-default).
        this.signingKey = Keys.hmacShaKeyFor(properties.decodedSecret());
        log.info("JWT habilitado: issuer={}, ttl={}, algoritmo=HS256",
                properties.getIssuer(), properties.getAccessTokenTtl());
    }

    // Emite el access token con los claims sub, iss, iat, exp y jti (PRD §9.2)
    public String generateToken(SecurityPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(principal.getUserId()))
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getAccessTokenTtl())))
                .id(UUID.randomUUID().toString())
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    // Valida un token completo. Lanza JwtException (subclases) si el token es inválido:
    // malformado, expirado, firma alterada, algoritmo distinto de HS256 o issuer incorrecto.
    public Claims validate(String token) {
        if (token == null || token.isBlank()) {
            throw new MalformedJwtException("Token JWT vacío");
        }

        Jws<Claims> jws;
        try {
            jws = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException ex) {
            // Nunca registrar el token ni la firma en claro (NFR-SEC-006)
            log.debug("Token JWT rechazado: {} ({})", ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }

        // Regla explícita: el header debe declarar exactamente HS256 (defensa frente a "alg" manipulado)
        String algorithm = jws.getHeader().getAlgorithm();
        if (!Jwts.SIG.HS256.getId().equals(algorithm)) {
            throw new UnsupportedJwtException("Algoritmo de firma no permitido: " + algorithm);
        }

        Claims claims = jws.getPayload();
        if (claims.getSubject() == null || claims.getSubject().isBlank()
                || claims.getIssuedAt() == null
                || claims.getExpiration() == null
                || claims.getId() == null) {
            throw new MalformedJwtException("El token no contiene los claims obligatorios (sub, iat, exp, jti)");
        }
        return claims;
    }

    public Duration getTokenTtl() {
        return properties.getAccessTokenTtl();
    }

    public String getIssuer() {
        return properties.getIssuer();
    }
}
