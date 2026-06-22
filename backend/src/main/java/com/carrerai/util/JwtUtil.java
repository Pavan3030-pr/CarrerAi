package com.carrerai.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final String secret;
  private final ObjectMapper objectMapper;

  public JwtUtil(@Value("${app.demo.jwt-secret}") String secret, ObjectMapper objectMapper) {
    this.secret = secret;
    this.objectMapper = objectMapper;
  }

  public String generateToken(String name, String email) {
    try {
      String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
      long now = Instant.now().toEpochMilli();
      long exp = now + 86_400_000L; // 24 hours
      String payloadJson = objectMapper.writeValueAsString(
          java.util.Map.of("name", name, "email", email, "iat", now, "exp", exp));
      String payload = base64Url(payloadJson);
      String signature = hmacSha256(header + "." + payload, secret);
      return header + "." + payload + "." + signature;
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate token", e);
    }
  }

  public String validateToken(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) return null;
      String expectedSig = hmacSha256(parts[0] + "." + parts[1], secret);
      if (!MessageDigest.isEqual(expectedSig.getBytes(StandardCharsets.UTF_8),
          parts[2].getBytes(StandardCharsets.UTF_8))) {
        return null;
      }
      byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
      JsonNode node = objectMapper.readTree(payloadBytes);
      return node.has("email") ? node.get("email").asText() : null;
    } catch (Exception e) {
      return null;
    }
  }

  private String base64Url(String data) {
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString(data.getBytes(StandardCharsets.UTF_8));
  }

  private String hmacSha256(String data, String key) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    mac.init(keySpec);
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
  }
}
