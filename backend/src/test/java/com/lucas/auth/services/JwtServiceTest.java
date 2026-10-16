package com.lucas.auth.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.lucas.auth.entities.Role;
import com.lucas.auth.entities.User;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtServiceTest {

  private JwtService jwtService;
  private JwtDecoder jwtDecoder;
  private User user;

  @BeforeEach
  void setUp() throws Exception{
    var generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    var keyPair = generator.generateKeyPair();
    var publicKey = (RSAPublicKey) keyPair.getPublic();
    var privateKey = (RSAPrivateKey) keyPair.getPrivate();

    var jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
    jwtService = new JwtService(new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk))));
    jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

    var role = new Role();
    role.setName("USER");
    user = new User("lucas", "lucas@example.com", "hash", Instant.now(), Instant.now(), Set.of(role));
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

  }

  @Test
  void accessTokenCarriesRolesInScopeAndExpiresInFiveMinutes() {
    Jwt jwt = jwtDecoder.decode(jwtService.generateAccessToken(user));

    assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
    assertThat(jwt.getClaimAsString("scope")).isEqualTo("USER");
    assertThat(jwt.getClaimAsString("iss")).isEqualTo("auth-service-api");
    assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(5));
  }

  @Test
  void refreshTokenHasNoScopeAndExpiresInOneDay() {
    Jwt jwt = jwtDecoder.decode(jwtService.generateRefreshToken(user));

    assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
    assertThat(jwt.getClaims()).doesNotContainKey("scope");
    assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(24));
  }
}
