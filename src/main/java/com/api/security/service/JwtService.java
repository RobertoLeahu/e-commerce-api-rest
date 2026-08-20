package com.api.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Clave secreta codificada en Base64 (debe tener al menos 256 bits para HS256)
    @Value("${application.security.jwt.secret-key:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${application.security.jwt.expiration:900000}") // 15 minutos en milisegundos
    private long jwtExpiration;

    @Value("${application.security.jwt.verification-expiration:86400000}") // 24 horas en milisegundos
    private long verificationExpiration;

    private static final String PURPOSE_CLAIM = "purpose";
    private static final String VERIFICATION_PURPOSE = "EMAIL_VERIFICATION";

    // --- GENERACIÓN DE TOKENS ---

    /**
     * Genera un Access Token para autenticación en la API.
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Incluimos los roles en el token para que el cliente pueda leerlos si lo necesita
        extraClaims.put("roles", userDetails.getAuthorities());

        return buildToken(extraClaims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Genera un Token Stateless para la verificación de correo electrónico.
     */
    public String generateVerificationToken(String email) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(PURPOSE_CLAIM, VERIFICATION_PURPOSE);

        return buildToken(extraClaims, email, verificationExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject) // Email del usuario
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // --- VALIDACIÓN DE TOKENS ---

    /**
     * Valida un Access Token contra el UserDetails cargado.
     */
    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Valida si un Token de Verificación es legítimo y de tipo EMAIL_VERIFICATION.
     */
    public boolean isVerificationTokenValid(String token) {
        try {
            String purpose = extractClaim(token, claims -> claims.get(PURPOSE_CLAIM, String.class));
            return VERIFICATION_PURPOSE.equals(purpose) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // --- EXTRACCIÓN DE DATOS ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey()) //getSignInKey() debe retornar SecretKey
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
