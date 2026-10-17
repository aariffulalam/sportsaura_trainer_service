package com.sportsaura.trainerservice.controller;

import com.sportsaura.trainerservice.dto.response.SportResponse;
import com.sportsaura.trainerservice.service.SportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sports")
@RequiredArgsConstructor
@Tag(name = "Sports", description = "The sports catalog. Public, no token needed.")
public class SportController {

  private final SportService sportService;

  @GetMapping
  @Operation(summary = "List all sports, with the number of trainers for each")
  public List<SportResponse> list() {
    return sportService.listActive();
  }

  @GetMapping("/{slug}")
  @Operation(summary = "Get one sport by its slug, e.g. football")
  public SportResponse getBySlug(@PathVariable String slug) {
    return sportService.getBySlug(slug);
  }
}
