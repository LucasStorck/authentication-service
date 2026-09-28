package com.lucas.auth.controllers;

import com.lucas.auth.dtos.CreateUserDto;
import com.lucas.auth.dtos.UpdateUserDto;
import com.lucas.auth.entities.Role;
import com.lucas.auth.entities.RoleType;
import com.lucas.auth.entities.User;
import com.lucas.auth.repositories.RoleRepository;
import com.lucas.auth.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
public class UserController {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public UserController(UserRepository userRepository, RoleRepository roleRepository,
      BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Operation(summary = "Create a new user")
  @PostMapping
  @Transactional
  public ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
    if (userRepository.findByUsername(createUserDto.username()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
    }

    Role userRole = Optional.ofNullable(roleRepository.findByName(RoleType.USER.name()))
        .orElseGet(() -> {
          Role newRole = new Role();
          newRole.setName(RoleType.USER.name());
          return roleRepository.save(newRole);
        });

    User newUser = new User();
    newUser.setUsername(createUserDto.username());
    newUser.setEmail(createUserDto.email());
    newUser.setPassword(bCryptPasswordEncoder.encode(createUserDto.password()));
    newUser.setRoles(Set.of(userRole));

    userRepository.save(newUser);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Operation(summary = "List all users")
  @GetMapping
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER')")
  public ResponseEntity<List<User>> getAllUsers() {
    return ResponseEntity.ok(userRepository.findAll());
  }

  @Operation(summary = "Get user by username")
  @GetMapping("/{username}")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER') or #username == authentication.name")
  public ResponseEntity<User> getUserByUsername(@PathVariable String username, JwtAuthenticationToken token) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    validateUserAccess(user, token);

    return ResponseEntity.ok(user);
  }

  @Operation(summary = "Update user")
  @PutMapping("/{username}")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER') or hasAuthority('SCOPE_USER')")
  @Transactional
  public ResponseEntity<Void> updateUser(@PathVariable String username, @Valid @RequestBody UpdateUserDto updateUserDto,
      JwtAuthenticationToken token) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    validateUserAccess(user, token);

    if (updateUserDto.username() != null)
      user.setUsername(updateUserDto.username());
    if (updateUserDto.email() != null)
      user.setEmail(updateUserDto.email());
    if (updateUserDto.password() != null)
      user.setPassword(bCryptPasswordEncoder.encode(updateUserDto.password()));

    userRepository.save(user);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Delete user")
  @DeleteMapping("/{username}")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER') or hasAuthority('SCOPE_USER')")
  @Transactional
  public ResponseEntity<Void> deleteUser(@PathVariable String username, JwtAuthenticationToken token) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    validateUserAccess(user, token);

    userRepository.delete(user);
    return ResponseEntity.noContent().build();
  }

  private void validateUserAccess(User targetUser, JwtAuthenticationToken token) {
    boolean isSuperuser = token.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("SCOPE_SUPERUSER"));
    String userIdFromToken = token.getName();

    if (!isSuperuser && !targetUser.getId().toString().equals(userIdFromToken)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access other users' data");
    }
  }
}
