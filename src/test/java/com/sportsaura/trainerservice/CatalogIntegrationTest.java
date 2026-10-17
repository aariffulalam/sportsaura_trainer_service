package com.sportsaura.trainerservice;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsaura.trainerservice.repository.SportRepository;
import com.sportsaura.trainerservice.repository.TrainerProfileRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Starts the whole application on a real MySQL (Docker) and calls the real endpoints. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class CatalogIntegrationTest {

  private static final long RAHUL = 2L;
  private static final long PRIYA = 3L;

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private TrainerProfileRepository trainerRepository;
  @Autowired private SportRepository sportRepository;

  @BeforeEach
  void cleanTrainers() {
    trainerRepository.deleteAll();
  }

  @Test
  void sportsArePublicAndInCatalogOrder() throws Exception {
    mockMvc
        .perform(get("/api/v1/sports"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(8)))
        .andExpect(jsonPath("$[0].slug", is("football")))
        .andExpect(jsonPath("$[0].icon", is("⚽")))
        .andExpect(jsonPath("$[0].trainerCount", is(0)));
  }

  @Test
  void oneSportBySlugAndUnknownSportIs404() throws Exception {
    mockMvc.perform(get("/api/v1/sports/yoga")).andExpect(jsonPath("$.name", is("Yoga")));
    mockMvc.perform(get("/api/v1/sports/curling")).andExpect(status().isNotFound());
  }

  @Test
  void pageContentIsPublicAndGroupedBySection() throws Exception {
    mockMvc
        .perform(get("/api/v1/content/home"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sections.hero[0].linkUrl", is("/trainers")))
        .andExpect(jsonPath("$.sections.features", hasSize(3)))
        .andExpect(jsonPath("$.sections.features[0].title", is("Trainers for every sport")));
    mockMvc
        .perform(get("/api/v1/content/how-it-works"))
        .andExpect(jsonPath("$.sections.players", hasSize(3)))
        .andExpect(jsonPath("$.sections.trainers", hasSize(3)));
    mockMvc.perform(get("/api/v1/content/nope")).andExpect(status().isNotFound());
  }

  @Test
  void trainerCreatesProfileAndVisitorsCanFindIt() throws Exception {
    upsert(RAHUL, "TRAINER", profile("Rahul Sharma", "Mumbai", sportIds("football", "fitness")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", is((int) RAHUL)))
        .andExpect(jsonPath("$.sports[*].slug", contains("football", "fitness")));
    upsert(PRIYA, "TRAINER", profile("Priya Nair", "Bengaluru", sportIds("yoga")));

    mockMvc.perform(get("/api/v1/trainers")).andExpect(jsonPath("$.page.totalElements", is(2)));
    mockMvc
        .perform(get("/api/v1/trainers").param("sport", "yoga"))
        .andExpect(jsonPath("$.content[*].displayName", contains("Priya Nair")));
    mockMvc
        .perform(get("/api/v1/trainers").param("city", "mumbai"))
        .andExpect(jsonPath("$.content[*].displayName", contains("Rahul Sharma")));
    mockMvc.perform(get("/api/v1/sports/football")).andExpect(jsonPath("$.trainerCount", is(1)));

    long id = trainerRepository.findByUserId(RAHUL).orElseThrow().getId();
    mockMvc
        .perform(get("/api/v1/trainers/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.headline", is("Coach in Mumbai")));
  }

  @Test
  void updatingKeepsOneProfilePerTrainer() throws Exception {
    upsert(RAHUL, "TRAINER", profile("Rahul", "Mumbai", sportIds("football")));
    upsert(RAHUL, "TRAINER", profile("Rahul Sharma", "Pune", sportIds("cricket")))
        .andExpect(jsonPath("$.city", is("Pune")));

    mockMvc
        .perform(get("/api/v1/trainers/me").header(HttpHeaders.AUTHORIZATION, bearer(RAHUL)))
        .andExpect(jsonPath("$.displayName", is("Rahul Sharma")))
        .andExpect(jsonPath("$.sports[*].slug", contains("cricket")));
    mockMvc.perform(get("/api/v1/trainers")).andExpect(jsonPath("$.page.totalElements", is(1)));
  }

  @Test
  void myProfileIs404BeforeItIsCreated() throws Exception {
    mockMvc
        .perform(get("/api/v1/trainers/me").header(HttpHeaders.AUTHORIZATION, bearer(RAHUL)))
        .andExpect(status().isNotFound());
  }

  @Test
  void onlyTrainersCanHaveAProfile() throws Exception {
    upsert(5L, "USER", profile("Asha", "Delhi", sportIds("tennis")))
        .andExpect(status().isForbidden());
  }

  @Test
  void managingAProfileNeedsAValidToken() throws Exception {
    mockMvc.perform(get("/api/v1/trainers/me")).andExpect(status().isUnauthorized());
    mockMvc
        .perform(get("/api/v1/trainers/me").header(HttpHeaders.AUTHORIZATION, "Bearer garbage"))
        .andExpect(status().isUnauthorized());
    mockMvc
        .perform(
            put("/api/v1/trainers/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(profile("X", "Y", sportIds("tennis")))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void invalidProfileReturns400WithFieldErrors() throws Exception {
    upsert(RAHUL, "TRAINER", Map.of("displayName", "", "hourlyRateInr", 5, "sportIds", List.of()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.displayName").isNotEmpty())
        .andExpect(jsonPath("$.errors.hourlyRateInr").isNotEmpty())
        .andExpect(jsonPath("$.errors.sportIds").isNotEmpty())
        .andExpect(jsonPath("$.errors.city").isNotEmpty());
  }

  @Test
  void unknownSportIsRejected() throws Exception {
    upsert(RAHUL, "TRAINER", profile("Rahul", "Mumbai", List.of(9999L)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail", is("Unknown sport id(s): [9999]")));
  }

  @Test
  void unknownTrainerIs404() throws Exception {
    mockMvc.perform(get("/api/v1/trainers/123456")).andExpect(status().isNotFound());
  }

  private ResultActions upsert(long userId, String role, Map<String, Object> body)
      throws Exception {
    return mockMvc.perform(
        put("/api/v1/trainers/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestTokens.token(userId, role))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(body)));
  }

  private static Map<String, Object> profile(String name, String city, List<Long> sportIds) {
    return Map.of(
        "displayName",
        name,
        "headline",
        "Coach in " + city,
        "bio",
        "Experienced coach.",
        "city",
        city,
        "experienceYears",
        6,
        "hourlyRateInr",
        900,
        "sportIds",
        sportIds);
  }

  private List<Long> sportIds(String... slugs) {
    return Arrays.stream(slugs)
        .map(slug -> sportRepository.findBySlugAndActiveTrue(slug).orElseThrow().getId())
        .toList();
  }

  private String json(Object body) throws Exception {
    return objectMapper.writeValueAsString(body);
  }

  private static String bearer(long userId) {
    return TestTokens.bearer(userId, "TRAINER");
  }
}
