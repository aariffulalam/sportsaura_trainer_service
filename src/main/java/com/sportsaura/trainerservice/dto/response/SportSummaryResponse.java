package com.sportsaura.trainerservice.dto.response;

/** The short form of a sport, used inside a trainer. */
public record SportSummaryResponse(Long id, String slug, String name, String icon) {}
