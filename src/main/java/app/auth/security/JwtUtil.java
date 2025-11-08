package app.auth.security;

import app.auth.entities.User;
import app.auth.exceptions.InvalidTokenException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;


@Component
public class JwtUtil {
    @Value("${app.security.jwt.key-id}")
    private String KEY_ID;

    private final RSAKey rsaPrivateJWK;
    private final RSAKey rsaPublicJWK;

    public JwtUtil() {
        try {
            rsaPrivateJWK = new RSAKeyGenerator(2048)
                    .keyID(KEY_ID).generate();
            rsaPublicJWK = rsaPrivateJWK.toPublicJWK();

        } catch (JOSEException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String generateAccessToken(User user) {
        try {
            JWSSigner signer = new RSASSASigner(rsaPrivateJWK);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .issuer("")
                    .expirationTime(new Date(new Date().getTime() + 15 * 60 * 1000))
                    .issueTime(new Date(new Date().getTime()))
                    .claim("id", user.getId())
                    .claim("email", user.getEmail())
                    .claim("fullName", user.getFullName())
                    .claim("status", user.getStatus())
                    .claim("role", user.getRole())
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaPrivateJWK.getKeyID()).build(), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (JOSEException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String generateRefreshToken(User user) {
        try {
            JWSSigner signer = new RSASSASigner(rsaPrivateJWK);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .issuer("")
                    .expirationTime(new Date(new Date().getTime() + 7 * 24 * 60 * 60 * 1000))
                    .issueTime(new Date(new Date().getTime()))
                    .claim("id", user.getId())
                    .claim("email", user.getEmail())
                    .claim("fullName", user.getFullName())
                    .claim("status", user.getStatus())
                    .claim("role", user.getRole())
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaPrivateJWK.getKeyID()).build(), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (JOSEException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void verifyToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);
            if (!signedJWT.verify(verifier)) {
                throw new InvalidTokenException("Invalid Token");
            }

            if (signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())) {
                throw new InvalidTokenException("Token expired");
            }

        } catch (JOSEException | ParseException ex) {
            throw new InvalidTokenException(ex.getMessage());
        }
    }

    public boolean isValidClaims(Map<String, Object> jwtClaims) {
        return jwtClaims.containsKey("id")
                && jwtClaims.containsKey("email")
                && jwtClaims.containsKey("fullName")
                && jwtClaims.containsKey("role")
                && jwtClaims.containsKey("status")
                && jwtClaims.containsKey("iat");
    }

    public Map<String, Object> getJWTClaimsSet(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getClaims();
        } catch (ParseException ex) {
            throw new InvalidTokenException();
        }
    }
}
