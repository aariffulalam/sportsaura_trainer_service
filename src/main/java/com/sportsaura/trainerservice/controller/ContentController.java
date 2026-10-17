package com.sportsaura.trainerservice.controller;

import com.sportsaura.trainerservice.dto.response.PageContentResponse;
import com.sportsaura.trainerservice.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
@Tag(name = "Website content", description = "Page text for the website. Public, no token needed.")
public class ContentController {

  private final ContentService contentService;

  @GetMapping("/{page}")
  @Operation(summary = "Get all text for one page: home, how-it-works or for-trainers")
  public PageContentResponse getPage(@PathVariable String page) {
    return contentService.getPage(page);
  }
}
