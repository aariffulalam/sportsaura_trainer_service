package com.sportsaura.trainerservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportsaura.trainerservice.dto.request.UpsertTrainerProfileRequest;
import com.sportsaura.trainerservice.dto.response.TrainerResponse;
import com.sportsaura.trainerservice.entity.Sport;
import com.sportsaura.trainerservice.entity.TrainerProfile;
import com.sportsaura.trainerservice.exception.InvalidRequestException;
import com.sportsaura.trainerservice.exception.ResourceNotFoundException;
import com.sportsaura.trainerservice.mapper.CatalogMapper;
import com.sportsaura.trainerservice.repository.SportRepository;
import com.sportsaura.trainerservice.repository.TrainerProfileRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class TrainerProfileServiceTest {

  @Mock private TrainerProfileRepository trainerRepository;
  @Mock private SportRepository sportRepository;

  private TrainerProfileService service;

  private final Sport football = sport(1L, "football", 1);
  private final Sport yoga = sport(7L, "yoga", 7);

  @BeforeEach
  void setUp() {
    service = new TrainerProfileService(trainerRepository, sportRepository, new CatalogMapper());
  }

  @Test
  void createsAProfileForANewTrainer() {
    when(trainerRepository.findByUserId(42L)).thenReturn(Optional.empty());
    when(sportRepository.findByIdInAndActiveTrue(Set.of(7L, 1L)))
        .thenReturn(List.of(yoga, football));
    when(trainerRepository.saveAndFlush(any(TrainerProfile.class)))
        .thenAnswer(i -> i.getArgument(0));

    TrainerResponse response = service.upsertMine(42L, request(List.of(7L, 1L)));

    assertThat(response.userId()).isEqualTo(42L);
    assertThat(response.displayName()).isEqualTo("Rahul Sharma");
    assertThat(response.city()).isEqualTo("Mumbai");
    // Sports are listed in catalog order, not the order they were sent in.
    assertThat(response.sports()).extracting("slug").containsExactly("football", "yoga");
  }

  @Test
  void updatesTheExistingProfileInsteadOfCreatingASecondOne() {
    TrainerProfile existing =
        TrainerProfile.builder().id(5L).userId(42L).displayName("Old name").build();
    when(trainerRepository.findByUserId(42L)).thenReturn(Optional.of(existing));
    when(sportRepository.findByIdInAndActiveTrue(Set.of(1L))).thenReturn(List.of(football));
    when(trainerRepository.saveAndFlush(existing)).thenReturn(existing);

    TrainerResponse response = service.upsertMine(42L, request(List.of(1L)));

    assertThat(response.id()).isEqualTo(5L);
    assertThat(existing.getDisplayName()).isEqualTo("Rahul Sharma");
  }

  @Test
  void rejectsUnknownSports() {
    when(trainerRepository.findByUserId(42L)).thenReturn(Optional.empty());
    when(sportRepository.findByIdInAndActiveTrue(Set.of(1L, 99L))).thenReturn(List.of(football));

    assertThatThrownBy(() -> service.upsertMine(42L, request(List.of(1L, 99L))))
        .isInstanceOf(InvalidRequestException.class)
        .hasMessageContaining("99");
    verify(trainerRepository, never()).saveAndFlush(any());
  }

  @Test
  void getMineExplainsWhenThereIsNoProfileYet() {
    when(trainerRepository.findByUserId(42L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getMine(42L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("not created a trainer profile");
  }

  @Test
  void searchTreatsBlankFiltersAsAny() {
    when(trainerRepository.search(null, null, null)).thenReturn(Page.empty());

    service.search("  ", "", null);

    verify(trainerRepository).search(null, null, null);
  }

  private static UpsertTrainerProfileRequest request(List<Long> sportIds) {
    return new UpsertTrainerProfileRequest(
        " Rahul Sharma ",
        "Football coach",
        "Ten years of coaching.",
        " Mumbai ",
        10,
        800,
        sportIds);
  }

  private static Sport sport(Long id, String slug, int order) {
    return Sport.builder().id(id).slug(slug).name(slug).icon("x").displayOrder(order).build();
  }
}
