package com.sportsaura.trainerservice.service;

import com.sportsaura.trainerservice.dto.response.SportResponse;
import com.sportsaura.trainerservice.exception.ResourceNotFoundException;
import com.sportsaura.trainerservice.mapper.CatalogMapper;
import com.sportsaura.trainerservice.repository.SportRepository;
import com.sportsaura.trainerservice.repository.SportTrainerCount;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The sports catalog, with how many trainers teach each sport. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SportService {

  private final SportRepository sportRepository;
  private final CatalogMapper mapper;

  public List<SportResponse> listActive() {
    Map<Long, Long> trainerCounts = trainerCountsBySport();
    return sportRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc().stream()
        .map(sport -> mapper.toSportResponse(sport, trainerCounts.getOrDefault(sport.getId(), 0L)))
        .toList();
  }

  public SportResponse getBySlug(String slug) {
    return sportRepository
        .findBySlugAndActiveTrue(slug)
        .map(
            sport ->
                mapper.toSportResponse(
                    sport, trainerCountsBySport().getOrDefault(sport.getId(), 0L)))
        .orElseThrow(() -> new ResourceNotFoundException("Sport '" + slug + "' not found"));
  }

  private Map<Long, Long> trainerCountsBySport() {
    return sportRepository.countActiveTrainersPerSport().stream()
        .collect(Collectors.toMap(SportTrainerCount::sportId, SportTrainerCount::trainers));
  }
}
