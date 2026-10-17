package com.sportsaura.trainerservice.dto.response;

import java.time.Instant;
import java.util.List;

/** A trainer's public profile. Contains nothing private: no email, no phone. */
public record TrainerResponse(
    Long id,
    Long userId,
    String displayName,
    String headline,
    String bio,
    String city,
    Integer experienceYears,
    Integer hourlyRateInr,
    List<SportSummaryResponse> sports,
    Instant createdAt,
    Instant updatedAt) {}
