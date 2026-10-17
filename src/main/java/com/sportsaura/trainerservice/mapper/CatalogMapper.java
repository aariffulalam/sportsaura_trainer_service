package com.sportsaura.trainerservice.mapper;

import com.sportsaura.trainerservice.dto.response.ContentBlockResponse;
import com.sportsaura.trainerservice.dto.response.SportResponse;
import com.sportsaura.trainerservice.dto.response.SportSummaryResponse;
import com.sportsaura.trainerservice.dto.response.TrainerResponse;
import com.sportsaura.trainerservice.entity.ContentBlock;
import com.sportsaura.trainerservice.entity.Sport;
import com.sportsaura.trainerservice.entity.TrainerProfile;
import java.util.Comparator;
import org.springframework.stereotype.Component;

/** Converts entities to API responses, so entities never leave the service layer. */
@Component
public class CatalogMapper {

  public SportResponse toSportResponse(Sport sport, long trainerCount) {
    return new SportResponse(
        sport.getId(),
        sport.getSlug(),
        sport.getName(),
        sport.getDescription(),
        sport.getIcon(),
        trainerCount);
  }

  public SportSummaryResponse toSportSummary(Sport sport) {
    return new SportSummaryResponse(
        sport.getId(), sport.getSlug(), sport.getName(), sport.getIcon());
  }

  public TrainerResponse toTrainerResponse(TrainerProfile trainer) {
    return new TrainerResponse(
        trainer.getId(),
        trainer.getUserId(),
        trainer.getDisplayName(),
        trainer.getHeadline(),
        trainer.getBio(),
        trainer.getCity(),
        trainer.getExperienceYears(),
        trainer.getHourlyRateInr(),
        trainer.getSports().stream()
            .sorted(Comparator.comparing(Sport::getDisplayOrder))
            .map(this::toSportSummary)
            .toList(),
        trainer.getCreatedAt(),
        trainer.getUpdatedAt());
  }

  public ContentBlockResponse toContentBlockResponse(ContentBlock block) {
    return new ContentBlockResponse(
        block.getTitle(),
        block.getBody(),
        block.getIcon(),
        block.getLinkLabel(),
        block.getLinkUrl());
  }
}
