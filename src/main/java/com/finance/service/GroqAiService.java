package com.finance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GroqAiService {

    @Value("${groq.api-key}")
    private String apiKey;
    private static final Logger log = LoggerFactory.getLogger(GroqAiService.class);

    private RestTemplate restTemplate;

    public GroqAiService() {
        this.restTemplate = new RestTemplate();
    }

    // Package-visible constructor to allow injecting a mock RestTemplate and apiKey in tests
    GroqAiService(RestTemplate restTemplate, String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public String askAi(String question) {
        log.info("Calling Groq AI");
        String url = "https://api.groq.com/openai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Use ObjectMapper to construct valid JSON
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.node.ObjectNode root = mapper.createObjectNode();
        root.put("model", "llama-3.1-8b-instant");

        com.fasterxml.jackson.databind.node.ArrayNode messages = root.putArray("messages");

        com.fasterxml.jackson.databind.node.ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content",
                "You are a financial education assistant. Do not give investment advice or price prediction.");

        com.fasterxml.jackson.databind.node.ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", question);

        String body = "";
        try {
            body = mapper.writeValueAsString(root);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Groq AI error");
            return "Error creating JSON: " + e.getMessage();
        }

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            com.fasterxml.jackson.databind.JsonNode responseRoot = mapper.readTree(response.getBody());
            return responseRoot.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "Groq API error: " + e.getMessage();
        }
    }
}