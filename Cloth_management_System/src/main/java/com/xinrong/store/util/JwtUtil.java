package com.xinrong.store.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-hours:24}")
    private long expireHours;

    public String generateToken(Long userId, String username) {
        long now = Instant.now().getEpochSecond();

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userId);
        payload.put("username", username);
        payload.put("iat", now);
        payload.put("exp", now + expireHours * 3600);

        String headerPart = encodeJson(header);
        String payloadPart = encodeJson(payload);
        String content = headerPart + "." + payloadPart;
        return content + "." + sign(content);
    }

    public Long parseUserId(String token) {
        Map<String, Object> payload = parsePayload(token);
        Object subject = payload.get("sub");
        if (subject instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(subject));
    }

    public boolean validateToken(String token) {
        try {
            parsePayload(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> parsePayload(String token) {
        String[] parts = token == null ? new String[0] : token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("token格式错误");
        }

        String content = parts[0] + "." + parts[1];
        String expectedSign = sign(content);
        if (!MessageDigest.isEqual(expectedSign.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("token签名无效");
        }

        try {
            byte[] bytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> payload = OBJECT_MAPPER.readValue(bytes, new TypeReference<>() {});
            Object exp = payload.get("exp");
            long expireAt = exp instanceof Number number ? number.longValue() : Long.parseLong(String.valueOf(exp));
            if (Instant.now().getEpochSecond() >= expireAt) {
                throw new IllegalArgumentException("token已过期");
            }
            return payload;
        } catch (Exception e) {
            throw new IllegalArgumentException("token解析失败", e);
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(value);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("JWT序列化失败", e);
        }
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] digest = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("JWT签名失败", e);
        }
    }
}
