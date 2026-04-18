package com.fitnesscoach.chat;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GroqClient {

  private static final String MODEL = "llama-3.3-70b-versatile";
  private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

  @Value("${app.groq.api-key}")
  private String apiKey;

  public String complete(List<Map<String, String>> messages) {
    RestClient client = RestClient.create();

    Map<String, Object> body = Map.of("model", MODEL, "messages", messages, "max_tokens", 1024);

    GroqResponse response =
        client
            .post()
            .uri(API_URL)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(GroqResponse.class);

    if (response == null || response.choices() == null || response.choices().isEmpty()) {
      throw new IllegalStateException("Groq devolvio respuesta vacia");
    }
    return response.choices().get(0).message().content();
  }

  record GroqResponse(List<Choice> choices) {}

  record Choice(Message message) {}

  record Message(String role, String content) {}
}
