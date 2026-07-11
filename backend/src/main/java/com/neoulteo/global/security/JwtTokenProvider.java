package com.neoulteo.global.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long validitySeconds;

    public JwtTokenProvider(ObjectMapper objectMapper,
            @Value("${neoulteo.jwt.secret:neoulteo-local-development-jwt-secret-key}") String secret,
            @Value("${neoulteo.jwt.validity-seconds:1209600}") long validitySeconds) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.validitySeconds = validitySeconds;
    }

    public String createToken(CustomUserDetails details) {
        long now = Instant.now().getEpochSecond();
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", details.getUsername());
        payload.put("uid", details.getUser().getId());
        payload.put("name", details.getUser().getName());
        payload.put("iat", now);
        payload.put("exp", now + validitySeconds);

        String unsignedToken = base64Url(toJson(header)) + "." + base64Url(toJson(payload));
        return unsignedToken + "." + sign(unsignedToken);
    }

    public String getSubject(String token) {
        Object subject = parsePayload(token).get("sub");
        return subject == null ? null : String.valueOf(subject);
    }

    public boolean isValid(String token) {
        try {
            String[] parts = split(token);
            String unsignedToken = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
                return false;
            }
            Object exp = parsePayload(token).get("exp");
            long expiresAt = exp instanceof Number number ? number.longValue() : Long.parseLong(String.valueOf(exp));
            return expiresAt > Instant.now().getEpochSecond();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private Map<String, Object> parsePayload(String token) {
        String[] parts = split(token);
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(decoded, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token payload", e);
        }
    }

    private String[] split(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is empty");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token");
        }
        return parts;
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize token", e);
        }
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign token", e);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null || left.length() != right.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length(); i += 1) {
            result |= left.charAt(i) ^ right.charAt(i);
        }
        return result == 0;
    }
}
