package com.lucas.auth.integration;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;


public class AuthFlowIntegrationTest extends AbstractIntegrationTest{

  @Test
  void registersANewUser() throws Exception {
    String username = uniqueName();

    mockMvc.perform(post("/api/user").contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("username", username, "email", username + "@example.com", "password", "secret123"))))
      .andExpect(status().isCreated());
  }

  @Test
  void rejectsDuplicateUsername() throws Exception {
    String username = uniqueName();
    register(username, "secret123");

    mockMvc.perform(post("/api/user").contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("username", username, "email", "other-" + username + "@example.com",
          "password", "secret123"))))
      .andExpect(status().isConflict());
  }

  @Test
  void rejectsInvalidRegistrationPayload() throws Exception {
    mockMvc.perform(post("/api/user").contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("username", uniqueName(), "email", "not-an-email", "password", "123"))))
      .andExpect(status().isBadRequest());
  }

  @Test
  void loginReturnsAccessAndRefreshTokens() throws Exception {
    String username = uniqueName();
    register(username, "secret123");

    mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("username", username, "password", "secret123"))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.accessToken", notNullValue()))
      .andExpect(jsonPath("$.refreshToken", notNullValue()))
      .andExpect(jsonPath("$.expiresIn").value(300));
  }

  @Test
  void loginWithWrongPasswordIsUnauthorized() throws Exception {
    String username = uniqueName();
    register(username, "secret123");

    mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("username", username, "password", "wrong-password"))))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshIssuesANewAccessToken() throws Exception {
    String username = uniqueName();
    register(username, "secret123");
    String refreshToken = login(username, "secret123").get("refreshToken").asText();

    mockMvc.perform(post("/api/refresh").contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("refreshToken", refreshToken))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.accessToken", notNullValue()));
  }

  @Test
  void refreshWithGarbageTokenIsUnauthorized() throws Exception {
    mockMvc.perform(post("/api/refresh").contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("refreshToken", "not-a-jwt"))))
      .andExpect(status().isUnauthorized());
  }
}
