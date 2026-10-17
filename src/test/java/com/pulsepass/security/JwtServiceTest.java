package com.pulsepass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// PRD §9.2: emisión y validación de JWT HS256 con los claims sub, iss, iat, exp y jti.
class JwtServiceTest {

    private static final String ISSUER = "pulsepass-api";

    // Clave de 64 bytes: además de HS256 permite firmar HS512 válido para demostrar que el filtro
    // rechaza explícitamente cualquier algoritmo distinto de HS256 (regla del PRD §9.2).
    private static final String SECRET_64_BYTES = Base64.getEncoder().encodeToString(
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
                    .getBytes(StandardCharsets.UTF_8));

    private final JwtProperties properties = new JwtProperties(ISSUER, SECRET_64_BYTES, "PT15M");
    private final JwtService jwtService = new JwtService(properties);
    private final SecretKey key = Keys.hmacShaKeyFor(properties.decodedSecret());

    private SecurityPrincipal principal() {
        return new SecurityPrincipal(7L, "andrea", "andrea@email.com", "$2a$10$hash", true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("generateToken incluye los claims obligatorios sub, iss, iat, exp y jti firmados con HS256")
    void generateToken_IncluyeClaimsObligatorios_YAlgoritmoHS256() {
        String token = jwtService.generateToken(principal());

        var jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        Claims claims = jws.getPayload();

        assertThat(jws.getHeader().getAlgorithm()).isEqualTo("HS256");
        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getId()).isNotBlank();
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    @DisplayName("El TTL emitido es exactamente el configurado (900 s = exp - iat)")
    void generateToken_TtlExacto_D900Segundos() {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(jwtService.generateToken(principal()))
                .getPayload();

        Duration ttl = Duration.between(claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant());
        assertThat(ttl).isEqualTo(Duration.ofSeconds(900));
    }

    @Test
    @DisplayName("validate acepta un token propio (round-trip) y devuelve los claims")
    void validate_TokenValido_RetornaClaims() {
        Claims claims = jwtService.validate(jwtService.generateToken(principal()));

        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
    }

    @Test
    @DisplayName("Token expirado -> ExpiredJwtException (T-SEC-007)")
    void validate_TokenExpirado_LanzaExpiredJwtException() {
        Instant past = Instant.now().minus(1, java.time.temporal.ChronoUnit.HOURS);
        String expired = Jwts.builder()
                .subject("7")
                .issuer(ISSUER)
                .issuedAt(Date.from(past))
                .expiration(Date.from(past.plus(Duration.ofMinutes(30))))
                .id("jti-expirado")
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtService.validate(expired))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Firma alterada -> JwtException (token rechazado, nunca confiable)")
    void validate_FirmaAlterada_LanzaJwtException() {
        String valid = jwtService.generateToken(principal());
        // Se altera el PRIMER carácter de la firma (no el último): cada carácter Base64 codifica 6 bits
        // completos, así que el primer carácter siempre cambia los bytes decodificados y la verificación
        // falla. Modificar el último es no determinista (sus bits de relleno pueden no cambiar la firma).
        int signatureStart = valid.lastIndexOf('.') + 1;
        String signature = valid.substring(signatureStart);
        char tamperedFirst = (signature.charAt(0) == 'A') ? 'B' : 'A';
        String tampered = valid.substring(0, signatureStart) + tamperedFirst + signature.substring(1);

        assertThatThrownBy(() -> jwtService.validate(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Issuer incorrecto -> JwtException (el parser exige requireIssuer)")
    void validate_IssuerIncorrecto_LanzaJwtException() {
        String foreign = Jwts.builder()
                .subject("7")
                .issuer("otro-emisor")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .id("jti-otro")
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtService.validate(foreign))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Token sin firma (alg=none) -> JwtException")
    void validate_TokenSinFirma_LanzaJwtException() {
        String unsecured = Jwts.builder()
                .subject("7")
                .issuer(ISSUER)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .id("jti-none")
                .compact();

        assertThatThrownBy(() -> jwtService.validate(unsecured))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Algoritmo distinto de HS256 (HS512 firmado con la misma clave) -> UnsupportedJwtException")
    void validate_AlgoritmoDistinto_LanzaUnsupportedJwtException() {
        String hs512 = Jwts.builder()
                .subject("7")
                .issuer(ISSUER)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .id("jti-hs512")
                .signWith(key, Jwts.SIG.HS512)
                .compact();

        assertThatThrownBy(() -> jwtService.validate(hs512))
                .isInstanceOf(UnsupportedJwtException.class)
                .hasMessageContaining("HS512")
                .hasMessageContaining("no permitido");
    }

    @Test
    @DisplayName("Token sin los claims obligatorios (sin jti) -> MalformedJwtException")
    void validate_SinClaimsObligatorios_LanzaMalformedJwtException() {
        String withoutJti = Jwts.builder()
                .subject("7")
                .issuer(ISSUER)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtService.validate(withoutJti))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("Token nulo o vacío -> MalformedJwtException")
    void validate_TokenVacio_LanzaMalformedJwtException() {
        assertThatThrownBy(() -> jwtService.validate(null))
                .isInstanceOf(MalformedJwtException.class);
        assertThatThrownBy(() -> jwtService.validate("   "))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("JwtService no se construye con secreto corto: la aplicación no arranca (deny-by-default)")
    void constructor_SecretoCorto_FallaAlArrancar() {
        String shortSecret = Base64.getEncoder().encodeToString("1234567890123456".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> new JwtService(new JwtProperties(ISSUER, shortSecret, "PT15M")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }
}
