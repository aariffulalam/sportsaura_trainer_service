package com.sportsaura.trainerservice.controller;

import com.sportsaura.trainerservice.config.OpenApiConfig;
import com.sportsaura.trainerservice.dto.request.UpsertTrainerProfileRequest;
import com.sportsaura.trainerservice.dto.response.TrainerResponse;
import com.sportsaura.trainerservice.security.AuthenticatedUser;
import com.sportsaura.trainerservice.service.TrainerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
@Tag(
    name = "Trainers",
    description = "Browse trainers (public). Trainers manage their own profile (TRAINER token).")
public class TrainerController {

  private final TrainerProfileService trainerService;

  @GetMapping
  @Operation(summary = "Search trainers, optionally by sport slug and city")
  public PagedModel<TrainerResponse> search(
      @RequestParam(required = false) String sport,
      @RequestParam(required = false) String city,
      @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return new PagedModel<>(trainerService.search(sport, city, pageable));
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('TRAINER')")
  @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
  @Operation(summary = "Get my trainer profile (TRAINER only)")
  public TrainerResponse getMine(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
    return trainerService.getMine(me.id());
  }

  @PutMapping("/me")
  @PreAuthorize("hasRole('TRAINER')")
  @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
  @Operation(summary = "Create or update my trainer profile (TRAINER only)")
  public TrainerResponse upsertMine(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
      @Valid @RequestBody UpsertTrainerProfileRequest request) {
    return trainerService.upsertMine(me.id(), request);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get one trainer's public profile")
  public TrainerResponse getPublic(@PathVariable Long id) {
    return trainerService.getPublic(id);
  }
}
