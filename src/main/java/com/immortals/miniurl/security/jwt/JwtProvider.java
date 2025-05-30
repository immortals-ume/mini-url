package com.immortals.miniurl.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {

    @Value("${auth.publicKeyFile}")
    private String publicKeyFileName;

    @Value("${auth.key-location}")
    private String keyLocation;

    @Value("${app.jwt-issuer}")
    private String jwtIssuer;
    private RSAPublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            publicKey = readPublicKey(getFilePath(publicKeyFileName));
            log.info("RSA keys loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load RSA keys: {}", e.getMessage(), e);
            throw new IllegalStateException("Could not initialize RSA keys for JWT", e);
        }
    }

    private Path getFilePath(String fileName) {
        return Paths.get(keyLocation, fileName);
    }

    private RSAPublicKey readPublicKey(Path filePath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Files.readAllBytes(filePath);
        String keyPem = new String(keyBytes, StandardCharsets.UTF_8).replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("\\s", "")
                .replace("-----END PUBLIC KEY-----", "");
        byte[] decoded = Base64.decodeBase64(keyPem);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return (RSAPublicKey) keyFactory.generatePublic(spec);
    }

    /**
     * Validates the JWT token signature and claims (issuer, expiration).
     */
    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                log.warn("Invalid JWT signature");
                return false;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // Check expiration
            if (claims.getExpirationTime() == null || new Date().after(claims.getExpirationTime())) {
                log.warn("JWT token expired");
                return false;
            }

            // Check issuer
            if (!jwtIssuer.equals(claims.getIssuer())) {
                log.warn("JWT token has invalid issuer");
                return false;
            }

            return true;
        } catch (ParseException | JOSEException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public JWTClaimsSet getClaimsFromToken(String token) throws ParseException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new ParseException("Invalid JWT token format: " + e.getMessage(), e.getErrorOffset());
        }
    }

    public Long getUserIdFromClaims(JWTClaimsSet claims, String customClaimName) {
        Object userIdObj = claims.getClaim(customClaimName);
        switch (userIdObj) {
            case null -> throw new IllegalArgumentException("userId claim is missing in the token");
            case Number number -> {
                return number.longValue();
            }
            case String s -> {
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("userId claim is not a valid number");
                }
            }
            default -> {
            }
        }

        throw new IllegalArgumentException("userId claim has unsupported type: " + userIdObj.getClass());
    }


    /**
     * Extracts username (subject) from JWT token.
     */
    public String getUsernameFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet()
                    .getSubject();
        } catch (ParseException e) {
            log.error("Failed to parse JWT token", e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Extracts authorities from JWT token claims.
     * Expected the "auth" claim to contain a collection of authorities.
     */
    public List<String> getAuthoritiesFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            Object authClaim = claims.getClaim("auth");
            if (authClaim instanceof List<?>) {
                return ((List<?>) authClaim).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
            return List.of();
        } catch (ParseException e) {
            log.error("Failed to parse JWT token for authorities", e);
            return List.of();
        }
    }
}
