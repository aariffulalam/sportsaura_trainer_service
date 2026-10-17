package com.sportsaura.trainerservice.dto.response;

/**
 * A sport in the catalog.
 *
 * @param trainerCount how many active trainers teach it
 */
public record SportResponse(
    Long id, String slug, String name, String description, String icon, long trainerCount) {}
