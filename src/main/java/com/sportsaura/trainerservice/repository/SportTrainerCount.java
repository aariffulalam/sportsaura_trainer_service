package com.sportsaura.trainerservice.repository;

/** One row of {@link SportRepository#countActiveTrainersPerSport()}. */
public record SportTrainerCount(Long sportId, Long trainers) {}
