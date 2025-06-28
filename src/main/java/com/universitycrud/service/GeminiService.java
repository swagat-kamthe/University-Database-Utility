package com.universitycrud.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String API_KEY = "AIzaSyB9IN2zth7zKfHyr_ZGMymlVm5TK9ewzMA";
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String getGeminiResponseText(String userInput) {
        try {
            // Build request JSON as String
            String requestJson = """
                {
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": "%s"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(userInput);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    ENDPOINT,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String responseBody = responseEntity.getBody();
                // Parse JSON to extract text field
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode candidates = rootNode.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode content = candidates.get(0).path("content");
                    JsonNode parts = content.path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        return parts.get(0).path("text").asText("[No reply text]");
                    }
                }
                return "[No candidates or text found]";
            } else {
                return "[Error: " + responseEntity.getStatusCode() + "]";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "[Exception occurred: " + e.getMessage() + "]";
        }
    }
}
