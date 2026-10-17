package com.sportsaura.trainerservice.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportsaura.trainerservice.TestTokens;
import com.sportsaura.trainerservice.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private final JwtService jwtService = new JwtService(new JwtProperties(TestTokens.SECRET));

  @Test
  void readsUserIdEmailAndRoleFromAUserServiceToken() {
    assertThat(jwtService.parse(TestTokens.token(7, "TRAINER")))
        .contains(new AuthenticatedUser(7L, "user7@example.com", Role.TRAINER));
  }

  @Test
  void rejectsATokenSignedWithAnotherSecret() {
    String token =
        TestTokens.token(
            7, "TRAINER", "a-completely-different-secret-key-of-32-chars", Duration.ofMinutes(5));

    assertThat(jwtService.parse(token)).isEmpty();
  }

  @Test
  void rejectsAnExpiredToken() {
    String token = TestTokens.token(7, "TRAINER", TestTokens.SECRET, Duration.ofMinutes(-1));

    assertThat(jwtService.parse(token)).isEmpty();
  }

  @Test
  void rejectsATokenWithoutARole() {
    String token =
        Jwts.builder()
            .subject("7")
            .signWith(Keys.hmacShaKeyFor(TestTokens.SECRET.getBytes(StandardCharsets.UTF_8)))
            .compact();

    assertThat(jwtService.parse(token)).isEmpty();
  }

  @Test
  void rejectsAnUnknownRole() {
    assertThat(jwtService.parse(TestTokens.token(7, "SUPERUSER"))).isEmpty();
  }

  @Test
  void rejectsGarbage() {
    assertThat(jwtService.parse("not-a-token")).isEmpty();
  }
}
