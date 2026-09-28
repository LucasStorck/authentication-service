package com.lucas.auth.dtos;

import com.lucas.auth.entities.Role;
import com.lucas.auth.entities.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserResponseDto(
    UUID id,
    String username,
    String email,
    Set<String> roles,
    Instant createdAt,
    Instant updatedAt
) {

  public static UserResponseDto from(User user) {
    Set<String> roleNames = user.getRoles().stream()
        .map(Role::getName)
        .collect(Collectors.toSet());

    return new UserResponseDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        roleNames,
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
