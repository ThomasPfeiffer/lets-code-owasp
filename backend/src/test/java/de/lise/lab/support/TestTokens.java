package de.lise.lab.support;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Erzeugt echte, signierte JWTs fuer Tests, die den kompletten Decoder-Pfad
 * (Signaturpruefung, Timestamp- und Issuer-Validierung) durchlaufen.
 *
 * <p>Haelt zwei RSA-Schluessel: einen "korrekten" (dessen JWKS der Test-Decoder kennt) und
 * einen "fremden" zum Simulieren einer ungueltigen Signatur.
 */
public final class TestTokens {

    public static final String ISSUER = "http://localhost:3010/realms/lab";

    private static final RSAKey CORRECT_KEY;
    private static final RSAKey WRONG_KEY;

    static {
        try {
            CORRECT_KEY = new RSAKeyGenerator(2048).keyID("test-key").generate();
            WRONG_KEY = new RSAKeyGenerator(2048).keyID("wrong-key").generate();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    private TestTokens() {
    }

    /** Public-Teil des korrekten Schluessels als JWKS-JSON (wie Keycloak es ausliefert). */
    public static String jwksJson() {
        return "{\"keys\":[" + CORRECT_KEY.toPublicJWK().toJSONString() + "]}";
    }

    public static String validToken(String username, List<String> realmRoles) {
        return sign(CORRECT_KEY, ISSUER, username, realmRoles,
                Instant.now().minusSeconds(30), Instant.now().plusSeconds(3600));
    }

    public static String expiredToken(String username, List<String> realmRoles) {
        return sign(CORRECT_KEY, ISSUER, username, realmRoles,
                Instant.now().minusSeconds(7200), Instant.now().minusSeconds(3600));
    }

    public static String wrongIssuerToken(String username, List<String> realmRoles) {
        return sign(CORRECT_KEY, "http://evil.example.com/realms/lab", username, realmRoles,
                Instant.now().minusSeconds(30), Instant.now().plusSeconds(3600));
    }

    public static String wrongSignatureToken(String username, List<String> realmRoles) {
        // Korrekt aussehendes Token, aber mit einem Schluessel signiert, der nicht im JWKS steht.
        return sign(WRONG_KEY, ISSUER, username, realmRoles,
                Instant.now().minusSeconds(30), Instant.now().plusSeconds(3600));
    }

    private static String sign(RSAKey key, String issuer, String username, List<String> realmRoles,
                               Instant issuedAt, Instant expiresAt) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject(username)
                    .claim("preferred_username", username)
                    .claim("realm_access", Map.of("roles", realmRoles))
                    .issueTime(Date.from(issuedAt))
                    .expirationTime(Date.from(expiresAt))
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(),
                    claims);
            jwt.sign(new RSASSASigner(key));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }
}
