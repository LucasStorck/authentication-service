package com.lucas.auth.controllers;

import com.lucas.auth.dtos.LoginRequestDto;
import com.lucas.auth.dtos.LoginResponseDto;
import com.lucas.auth.dtos.RefreshRequestDto;
import com.lucas.auth.entities.User;
import com.lucas.auth.repositories.UserRepository;
import com.lucas.auth.services.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TokenController {

  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final JwtDecoder jwtDecoder;

  public TokenController(JwtService jwtService, AuthenticationManager authenticationManager,
      UserRepository userRepository, JwtDecoder jwtDecoder) {
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
    this.jwtDecoder = jwtDecoder;
  }

  @Operation(summary = "User Login", description = "Authenticates user and returns access and refresh tokens.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful login."),
      @ApiResponse(responseCode = "401", description = "Invalid credentials.")
  })
  @PostMapping("/api/login")
  public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
    var authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

    var user = userRepository.findByUsername(loginRequest.username())
        .orElseThrow(() -> new BadCredentialsException("User not found"));

    var accessToken = jwtService.generateAccessToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);

    return ResponseEntity.ok(new LoginResponseDto(accessToken, refreshToken, 300L));
  }

  @Operation(summary = "Refresh Token", description = "Generates a new access token using a valid refresh token.")
  @PostMapping("/api/refresh")
  public ResponseEntity<LoginResponseDto> refresh(@RequestBody RefreshRequestDto refreshRequest) {
    try {
      Jwt jwt = jwtDecoder.decode(refreshRequest.refreshToken());
      String userId = jwt.getSubject();

      User user = userRepository.findById(UUID.fromString(userId))
          .orElseThrow(() -> new BadCredentialsException("User not found"));

      var accessToken = jwtService.generateAccessToken(user);

      return ResponseEntity.ok(new LoginResponseDto(accessToken, refreshRequest.refreshToken(), 300L));
    } catch (JwtException e) {
      throw new BadCredentialsException("Invalid refresh token");
    }
  }
}
