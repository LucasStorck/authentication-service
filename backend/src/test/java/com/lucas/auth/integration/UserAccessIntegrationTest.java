package com.lucas.auth.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class UserAccessIntegrationTest extends AbstractIntegrationTest {

  private String tokenFor(String username) throws Exception {
    register(username, "secret123");
    return login(username, "secret123").get("accessToken").asText();
  }

  // Seeded at startup by SuperuserConfig.
  private String superuserToken() throws Exception {
    return login("superuser", "123").get("accessToken").asText();
  }

  @Test
  void protectedEndpointWithoutTokenIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/user/anyone")).andExpect(status().isUnauthorized());
  }

  @Test
  void userCanReadOwnProfileWithoutExposingPasswordHash() throws Exception {
    String username = uniqueName();
    String token = tokenFor(username);

    mockMvc.perform(get("/api/user/" + username).header(HttpHeaders.AUTHORIZATION, bearer(token)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.username").value(username))
      .andExpect(jsonPath("$.roles", hasItem("USER")))
      .andExpect(jsonPath("$.password").doesNotExist());
  }

  @Test
  void userCannotReadAnotherUsersProfile() throws Exception {
    String owner = uniqueName();
    tokenFor(owner);
    String intruderToken = tokenFor(uniqueName());

    mockMvc.perform(get("/api/user/" + owner).header(HttpHeaders.AUTHORIZATION, bearer(intruderToken)))
      .andExpect(status().isForbidden());
  }

  @Test
  void superuserCanReadAnyProfile() throws Exception {
    String username = uniqueName();
    tokenFor(username);

    mockMvc.perform(get("/api/user/" + username).header(HttpHeaders.AUTHORIZATION, bearer(superuserToken())))
      .andExpect(status().isOk());
  }

  @Test
  void onlySuperuserCanListUsers() throws Exception {
    String userToken = tokenFor(uniqueName());

    mockMvc.perform(get("/api/user").header(HttpHeaders.AUTHORIZATION, bearer(userToken)))
      .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/user").header(HttpHeaders.AUTHORIZATION, bearer(superuserToken())))
      .andExpect(status().isOk());
  }

  @Test
  void userCanUpdateOwnEmail() throws Exception {
    String username = uniqueName();
    String token = tokenFor(username);

    mockMvc.perform(put("/api/user/" + username).header(HttpHeaders.AUTHORIZATION, bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("email", "new-" + username + "@example.com"))))
      .andExpect(status().isOk());

    mockMvc.perform(get("/api/user/" + username).header(HttpHeaders.AUTHORIZATION, bearer(token)))
      .andExpect(jsonPath("$.email").value("new-" + username + "@example.com"));
  }

  @Test
  void userCannotUpdateAnotherUser() throws Exception {
    String owner = uniqueName();
    tokenFor(owner);
    String intruderToken = tokenFor(uniqueName());

    mockMvc.perform(put("/api/user/" + owner).header(HttpHeaders.AUTHORIZATION, bearer(intruderToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("email", "hacked@example.com"))))
      .andExpect(status().isForbidden());
  }

  @Test
  void userCanDeleteOwnAccount() throws Exception {
    String username = uniqueName();
    String token = tokenFor(username);

    mockMvc.perform(delete("/api/user/" + username).header(HttpHeaders.AUTHORIZATION, bearer(token)))
      .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/user/" + username).header(HttpHeaders.AUTHORIZATION, bearer(superuserToken())))
      .andExpect(status().isNotFound());
  }
}
