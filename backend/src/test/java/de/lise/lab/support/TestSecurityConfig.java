package de.lise.lab.support;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.text.ParseException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Ersetzt den produktiven (netzwerkgebundenen) JwtDecoder durch einen, dessen JWKS der
 * in-Test erzeugte Public-Key ist. Issuer- und Timestamp-Validierung bleiben identisch zum
 * Produktionscode, sodass abgelaufene / falsch signierte / falscher-Issuer-Tokens wie in
 * Produktion abgelehnt werden.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() {
        try {
            JWKSet jwkSet = JWKSet.parse(TestTokens.jwksJson());
            JWSKeySelector<SecurityContext> keySelector =
                    new JWSVerificationKeySelector<>(
                            com.nimbusds.jose.JWSAlgorithm.RS256,
                            new ImmutableJWKSet<>(jwkSet));

            DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            processor.setJWSKeySelector(keySelector);

            NimbusJwtDecoder decoder = new NimbusJwtDecoder(processor);

            OAuth2TokenValidator<Jwt> validators = new DelegatingOAuth2TokenValidator<>(
                    new JwtTimestampValidator(),
                    new JwtIssuerValidator(TestTokens.ISSUER));
            decoder.setJwtValidator(validators);

            return decoder;
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }
}
