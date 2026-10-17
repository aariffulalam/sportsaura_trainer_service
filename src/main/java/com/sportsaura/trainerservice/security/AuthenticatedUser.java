package com.sportsaura.trainerservice.security;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * The logged-in user, read from the JWT. This service has no users table: the user id, email and
 * role come straight from the token the user service signed.
 *
 * @param id the user's id in the user service
 */
public record AuthenticatedUser(Long id, String email, Role role) {

  public List<GrantedAuthority> authorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }
}
