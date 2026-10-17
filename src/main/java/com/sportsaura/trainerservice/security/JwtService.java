package com.sportsaura.trainerservice.security;

import com.sportsaura.trainerservice.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/** Checks access tokens issued by the user service. This service never creates tokens. */
@Service
public class JwtService {

  private final SecretKey signingKey;

  public JwtService(JwtProperties properties) {
    this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
  }

  /** The user inside the token, or empty if it is expired, tampered with, or missing claims. */
  public Optional<AuthenticatedUser> parse(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
      String role = claims.get("role", String.class);
      if (claims.getSubject() == null || role == null) {
        return Optional.empty();
      }
      return Optional.of(
          new AuthenticatedUser(
              Long.valueOf(claims.getSubject()),
              claims.get("email", String.class),
              Role.valueOf(role)));
    } catch (JwtException | IllegalArgumentException ex) {
      return Optional.empty();
    }
  }
}
