package com.example.oopsLog.common.llm.provider;

import com.example.oopsLog.common.llm.LlmRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiLlmClientTest {

    @Test
    void requestBody_usesMaxCompletionTokens_notMaxTokens() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(hasJsonFields(mapper))
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            { "message": { "content": "{\\"ok\\":true}" }, "finish_reason": "stop" }
                          ],
                          "usage": { "prompt_tokens": 1, "completion_tokens": 2, "total_tokens": 3 }
                        }
                        """, MediaType.APPLICATION_JSON));

        OpenAiLlmClient client = new OpenAiLlmClient("test-key", "gpt-5.4-mini", restTemplate, mapper);
        var result = client.generate(new LlmRequest("sys", "user"), "req-1");

        assertEquals("{\"ok\":true}", result.text());
        server.verify();
    }

    private RequestMatcher hasJsonFields(ObjectMapper mapper) {
        return request -> {
            if (!(request instanceof MockClientHttpRequest mockRequest)) {
                fail("Expected MockClientHttpRequest but got: " + request.getClass());
                return;
            }
            String body = mockRequest.getBodyAsString();
            JsonNode json = mapper.readTree(body);

            assertEquals("gpt-5.4-mini", json.path("model").asText());
            assertTrue(json.has("max_completion_tokens"), "max_completion_tokens must be present");
            assertFalse(json.has("max_tokens"), "max_tokens must NOT be present");

            JsonNode messages = json.path("messages");
            assertTrue(messages.isArray());
            assertEquals(2, messages.size());
            assertEquals("system", messages.get(0).path("role").asText());
            assertEquals("user", messages.get(1).path("role").asText());

            assertEquals("json_object", json.path("response_format").path("type").asText());
        };
    }
}
