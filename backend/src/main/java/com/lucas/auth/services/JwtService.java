package com.lucas.auth.services;

import com.lucas.auth.entities.Role;
import com.lucas.auth.entities.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class JwtService {

  private final JwtEncoder jwtEncoder;

  public JwtService(JwtEncoder jwtEncoder) {
    this.jwtEncoder = jwtEncoder;
  }

  public String generateAccessToken(User user) {
    Instant now = Instant.now();
    long expiresIn = 300L;

    String scope = user.getRoles().stream()
        .map(Role::getName)
        .collect(Collectors.joining(" "));

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("auth-service-api")
        .subject(user.getId().toString())
        .issuedAt(now)
        .expiresAt(now.plusSeconds(expiresIn))
        .claim("scope", scope)
        .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public String generateRefreshToken(User user) {
    Instant now = Instant.now();
    long expiresIn = 3600L * 24; // 24 hours

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("auth-service-api")
        .subject(user.getId().toString())
        .issuedAt(now)
        .expiresAt(now.plusSeconds(expiresIn))
        .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }
}
