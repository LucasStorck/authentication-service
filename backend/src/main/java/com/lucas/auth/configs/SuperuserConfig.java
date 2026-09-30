package com.lucas.auth.configs;

import com.lucas.auth.entities.RoleType;
import com.lucas.auth.entities.User;
import com.lucas.auth.repositories.RoleRepository;
import com.lucas.auth.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Configuration
public class SuperuserConfig implements CommandLineRunner {

  private final RoleRepository roleRepository;

  private final UserRepository userRepository;

  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public SuperuserConfig(RoleRepository roleRepository, UserRepository userRepository,
      BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    var roleSuperuser = roleRepository.findByName(RoleType.SUPERUSER.name());

    var userAdmin = userRepository.findByUsername("superuser");

    userAdmin.ifPresentOrElse(
        (user) -> {
          System.out.println("THE SUPERUSER ALREADY EXISTS");
        },
        () -> {
          var user = new User();
          user.setUsername("superuser");
          user.setEmail("superuser@email.com");
          user.setPassword(bCryptPasswordEncoder.encode("123"));
          user.setRoles(Set.of(roleSuperuser));
          userRepository.save(user);
        });
  }
}
