package com.sportsaura.trainerservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Proves the migrations work on real MySQL. The app starting at all already proves that Hibernate
 * found the tables it expects ({@code ddl-auto: validate}).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class FlywayMigrationIntegrationTest {

  @Autowired private Flyway flyway;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void everyMigrationIsAppliedSuccessfully() {
    MigrationInfo[] applied = flyway.info().applied();

    assertThat(applied).allMatch(info -> info.getState() == MigrationState.SUCCESS);
    assertThat(flyway.info().pending()).isEmpty();
    assertThat(Arrays.stream(applied).map(info -> info.getVersion().getVersion()))
        .containsExactly("1", "2", "3", "4", "5");
  }

  @Test
  void expectedTablesExist() {
    List<String> tables =
        jdbcTemplate.queryForList(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
            String.class);

    assertThat(tables).contains("sports", "trainer_profiles", "trainer_sports", "content_blocks");
  }

  @Test
  void referenceDataIsLoaded() {
    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sports", Integer.class))
        .isEqualTo(8);
    assertThat(
            jdbcTemplate.queryForList(
                "SELECT DISTINCT page_key FROM content_blocks ORDER BY page_key", String.class))
        .containsExactly("for-trainers", "home", "how-it-works");
  }

  @Test
  void emojiAreStoredIntact() {
    assertThat(
            jdbcTemplate.queryForObject(
                "SELECT icon FROM sports WHERE slug = 'football'", String.class))
        .isEqualTo("⚽");
  }

  @Test
  void oneProfilePerAccount() {
    Integer uniqueIndexes =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE()"
                + " AND table_name = 'trainer_profiles' AND column_name = 'user_id'"
                + " AND non_unique = 0",
            Integer.class);

    assertThat(uniqueIndexes).isEqualTo(1);
  }
}
