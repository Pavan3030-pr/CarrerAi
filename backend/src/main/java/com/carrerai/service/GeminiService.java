package com.carrerai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GeminiService {

  private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
  private static final String MODEL = "gemini-2.0-flash";
  private static final String API_URL =
      "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";

  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final String apiKey;

  public GeminiService(@Value("${gemini.api.key:}") String apiKey, ObjectMapper objectMapper) {
    this.apiKey = apiKey;
    this.objectMapper = objectMapper;
    this.webClient = WebClient.builder().build();
  }

  public boolean isAvailable() {
    return apiKey != null && !apiKey.isBlank() && !apiKey.equals("replace_with_google_gemini_key");
  }

  /**
   * Send a structured prompt to Gemini and return the parsed JSON response.
   * Falls back gracefully — returns null on any error so callers can use mock data.
   */
  public JsonNode generateStructured(String systemInstruction, String userPrompt) {
    if (!isAvailable()) {
      log.warn("Gemini API key not configured — returning null");
      return null;
    }
    try {
      ObjectNode requestBody = objectMapper.createObjectNode();

      // System instruction (no role field, just parts)
      ObjectNode systemNode = objectMapper.createObjectNode();
      ArrayNode systemParts = systemNode.putArray("parts");
      systemParts.addObject().put("text", systemInstruction);
      requestBody.set("system_instruction", systemNode);

      // User content
      ObjectNode contentNode = objectMapper.createObjectNode();
      contentNode.put("role", "user");
      ArrayNode parts = contentNode.putArray("parts");
      parts.addObject().put("text", userPrompt);
      requestBody.set("contents", objectMapper.createArrayNode().add(contentNode));

      // Generation config
      ObjectNode config = objectMapper.createObjectNode();
      config.put("temperature", 0.4);
      config.put("maxOutputTokens", 2048);
      requestBody.set("generationConfig", config);

      String responseJson = webClient.post()
          .uri(API_URL + "?key=" + apiKey)
          .header("Content-Type", "application/json")
          .bodyValue(requestBody)
          .retrieve()
          .bodyToMono(String.class)
          .block();

      if (responseJson == null) return null;

      JsonNode root = objectMapper.readTree(responseJson);
      JsonNode candidates = root.get("candidates");
      if (candidates != null && candidates.isArray() && candidates.size() > 0) {
        JsonNode text = candidates.get(0)
            .path("content").path("parts").get(0).path("text");
        if (!text.isMissingNode()) {
          String rawText = text.asText().trim();
          // Remove markdown code fences if present
          if (rawText.startsWith("```")) {
            rawText = rawText.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
          }
          return objectMapper.readTree(rawText);
        }
      }
      return null;
    } catch (Exception e) {
      log.warn("Gemini API call failed: {}", e.getMessage());
      return null;
    }
  }
}
