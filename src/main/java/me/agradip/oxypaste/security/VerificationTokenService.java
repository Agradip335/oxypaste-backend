package me.agradip.oxypaste.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import me.agradip.oxypaste.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class VerificationTokenService {

    @Value("${security.verification-token-secret}")
    private String secretKey;

    private static final ObjectMapper mapper = new ObjectMapper();
    private Mac mac;

    @PostConstruct
    public void init() {
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MAC", e);
        }
    }

    public String signPayload(Map<String, Object> data) {
        try {
            String json = mapper.writeValueAsString(data);
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            String signature = sign(encodedPayload);
            return encodedPayload + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign token", e);
        }
    }

    public Map<String, Object> verifyToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) throw new ApiException(400, "Invalid token format");

            String encodedPayload = parts[0];
            String providedSig = parts[1];
            String expectedSig = sign(encodedPayload);

            if (!MessageDigest.isEqual(providedSig.getBytes(), expectedSig.getBytes())) {
                throw new ApiException(400, "Invalid token signature");
            }

            String json = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
            Map<String, Object> data = mapper.readValue(json, Map.class);

            long now = Instant.now().getEpochSecond();
            long exp = ((Number) data.get("exp")).longValue();
            if (exp < now) {
                throw new ApiException(410, "Token expired");
            }

            return data;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(400, "Failed to parse token");
        }
    }

    private String sign(String payload) {
        byte[] signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }
}
