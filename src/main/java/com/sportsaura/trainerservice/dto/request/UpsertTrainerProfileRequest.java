package com.sportsaura.trainerservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Body of {@code PUT /api/v1/trainers/me}: creates the trainer's profile or replaces it. */
public record UpsertTrainerProfileRequest(
    @NotBlank(message = "Display name is required")
        @Size(max = 100, message = "Display name must be at most 100 characters")
        String displayName,
    @NotBlank(message = "Headline is required")
        @Size(max = 150, message = "Headline must be at most 150 characters")
        String headline,
    @NotBlank(message = "Bio is required")
        @Size(max = 2000, message = "Bio must be at most 2000 characters")
        String bio,
    @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must be at most 100 characters")
        String city,
    @NotNull(message = "Experience is required")
        @Min(value = 0, message = "Experience cannot be negative")
        @Max(value = 60, message = "Experience must be at most 60 years")
        Integer experienceYears,
    @NotNull(message = "Hourly rate is required")
        @Min(value = 100, message = "Hourly rate must be at least ₹100")
        @Max(value = 100000, message = "Hourly rate must be at most ₹100000")
        Integer hourlyRateInr,
    @NotEmpty(message = "Choose at least one sport")
        @Size(max = 5, message = "Choose at most 5 sports")
        List<Long> sportIds) {}
