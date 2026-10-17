package com.sportsaura.trainerservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/** Builds tokens shaped like the user service's, signed with the test secret. */
public final class TestTokens {

  public static final String SECRET = "test-only-secret-key-with-at-least-32-characters";

  private TestTokens() {}

  public static String token(long userId, String role) {
    return token(userId, role, SECRET, Duration.ofMinutes(5));
  }

  public static String token(long userId, String role, String secret, Duration validFor) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("email", "user" + userId + "@example.com")
        .claim("role", role)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(validFor)))
        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  public static String bearer(long userId, String role) {
    return "Bearer " + token(userId, role);
  }
}
