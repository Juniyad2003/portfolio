package com.finance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class GroqAiServiceTest {

    @Mock
    RestTemplate restTemplate;

    GroqAiService groqAiService;

    @BeforeEach
    void setUp() {

        groqAiService = new GroqAiService(restTemplate, "test-api-key");
    }

    @Test
    void testAskAiSuccessParsesContent() throws Exception {
        String question = "What is diversification?";
        String mockResponseJson = "{\n"
                + "  \"choices\": [\n"
                + "    {\"message\": {\"content\": \"Diversification is spreading risk...\"}}\n"
                + "  ]\n"
                + "}";

        ResponseEntity<String> respEntity = new ResponseEntity<>(mockResponseJson, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(respEntity);

        String result = groqAiService.askAi(question);

        assertEquals("Diversification is spreading risk...", result);


        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(String.class));

        HttpEntity sent = captor.getValue();
        assertNotNull(sent);
        HttpHeaders headers = sent.getHeaders();
        assertEquals("Bearer test-api-key", headers.getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals("application/json", headers.getContentType().toString());
        assertTrue(sent.getBody().toString().contains("llama-3.1-8b-instant"));
    }

    @Test
    void testAskAiRestTemplateThrowsReturnsErrorMessage() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("connection failed"));

        String result = groqAiService.askAi("hello");

        assertTrue(result.startsWith("Groq API error:"));
        assertTrue(result.contains("connection failed"));
    }
}
