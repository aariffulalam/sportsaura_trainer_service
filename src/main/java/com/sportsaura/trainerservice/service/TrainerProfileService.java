package com.sportsaura.trainerservice.service;

import com.sportsaura.trainerservice.dto.request.UpsertTrainerProfileRequest;
import com.sportsaura.trainerservice.dto.response.TrainerResponse;
import com.sportsaura.trainerservice.entity.Sport;
import com.sportsaura.trainerservice.entity.TrainerProfile;
import com.sportsaura.trainerservice.exception.InvalidRequestException;
import com.sportsaura.trainerservice.exception.ResourceNotFoundException;
import com.sportsaura.trainerservice.mapper.CatalogMapper;
import com.sportsaura.trainerservice.repository.SportRepository;
import com.sportsaura.trainerservice.repository.TrainerProfileRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Business logic for browsing trainers and for trainers managing their own profile. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerProfileService {

  private final TrainerProfileRepository trainerRepository;
  private final SportRepository sportRepository;
  private final CatalogMapper mapper;

  public Page<TrainerResponse> search(String sport, String city, Pageable pageable) {
    return trainerRepository
        .search(blankToNull(sport), blankToNull(city), pageable)
        .map(mapper::toTrainerResponse);
  }

  public TrainerResponse getPublic(Long id) {
    return trainerRepository
        .findByIdAndActiveTrue(id)
        .map(mapper::toTrainerResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Trainer " + id + " not found"));
  }

  public TrainerResponse getMine(Long userId) {
    return trainerRepository
        .findByUserId(userId)
        .map(mapper::toTrainerResponse)
        .orElseThrow(
            () -> new ResourceNotFoundException("You have not created a trainer profile yet"));
  }

  /** Creates the profile of this account, or replaces it if it already exists. */
  @Transactional
  public TrainerResponse upsertMine(Long userId, UpsertTrainerProfileRequest request) {
    TrainerProfile trainer =
        trainerRepository
            .findByUserId(userId)
            .orElseGet(() -> TrainerProfile.builder().userId(userId).build());

    trainer.setDisplayName(request.displayName().trim());
    trainer.setHeadline(request.headline().trim());
    trainer.setBio(request.bio().trim());
    trainer.setCity(request.city().trim());
    trainer.setExperienceYears(request.experienceYears());
    trainer.setHourlyRateInr(request.hourlyRateInr());
    trainer.setSports(findSports(request.sportIds()));

    return mapper.toTrainerResponse(trainerRepository.saveAndFlush(trainer));
  }

  private Set<Sport> findSports(List<Long> sportIds) {
    Set<Long> wanted = new LinkedHashSet<>(sportIds);
    List<Sport> found = sportRepository.findByIdInAndActiveTrue(wanted);
    if (found.size() != wanted.size()) {
      Set<Long> foundIds = found.stream().map(Sport::getId).collect(Collectors.toSet());
      wanted.removeAll(foundIds);
      throw new InvalidRequestException("Unknown sport id(s): " + wanted);
    }
    return new LinkedHashSet<>(found);
  }

  private static String blankToNull(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }
}
