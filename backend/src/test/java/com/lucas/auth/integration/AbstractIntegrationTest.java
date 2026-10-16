package com.lucas.auth.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AbstractIntegrationTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  // The app reads its RSA keys from properties; generate a throwaway pair per test run.
  @DynamicPropertySource
  static void jwtKeys(DynamicPropertyRegistry registry) throws Exception {
    var generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    var keyPair = generator.generateKeyPair();
    var encoder = Base64.getEncoder();
    String publicKey = "-----BEGIN PUBLIC KEY-----" + encoder.encodeToString(keyPair.getPublic().getEncoded())
      + "-----END PUBLIC KEY-----";
    String privateKey = "-----BEGIN PRIVATE KEY-----" + encoder.encodeToString(keyPair.getPrivate().getEncoded())
      + "-----END PRIVATE KEY-----";
    registry.add("jwt.public-key", () -> publicKey);
    registry.add("jwt.private-key", () -> privateKey);
  }

  protected String uniqueName() {
    return "user-" + UUID.randomUUID().toString().substring(0, 8);
  }

  protected String json(Map<String, ?> body) throws Exception {
    return objectMapper.writeValueAsString(body);
  }

  protected void register(String username, String password) throws Exception {
    mockMvc.perform(post("/api/user").contentType(MediaType.APPLICATION_JSON)
      .content(json(Map.of("username", username, "email", username + "@example.com", "password", password))));
  }

  /**
   * Logs in and returns the parsed token response.
   */
  protected JsonNode login(String username, String password) throws Exception {
    String body = mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("username", username, "password", password))))
      .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(body);
  }

  protected String bearer(String accessToken) {
    return "Bearer " + accessToken;
  }
}
