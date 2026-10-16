package com.lucas.auth.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.lucas.auth.entities.Role;
import com.lucas.auth.entities.User;
import com.lucas.auth.repositories.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

  @Mock
  private UserRepository userRepository;
  @InjectMocks
  private UserDetailsServiceImpl service;

  @Test
  void mapsUserAndRolesToUserDetails() {
    var role = new Role();
    role.setName("SUPERUSER");
    var user = new User("lucas", "lucas@example.com", "hash", Instant.now(), Instant.now(), Set.of(role));
    when(userRepository.findByUsername("lucas")).thenReturn(Optional.of(user));

    UserDetails details = service.loadUserByUsername("lucas");

    assertThat(details.getUsername()).isEqualTo("lucas");
    assertThat(details.getPassword()).isEqualTo("hash");
    assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
      .containsExactly("SUPERUSER");
  }

  @Test
  void throwsWhenUserDoesNotExist() {
    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
      .isInstanceOf(UsernameNotFoundException.class);
  }
}
