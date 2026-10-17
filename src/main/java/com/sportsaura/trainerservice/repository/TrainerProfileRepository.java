package com.sportsaura.trainerservice.repository;

import com.sportsaura.trainerservice.entity.TrainerProfile;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long> {

  Optional<TrainerProfile> findByUserId(Long userId);

  Optional<TrainerProfile> findByIdAndActiveTrue(Long id);

  /**
   * Active trainers, optionally only those teaching one sport and/or in one city. A null filter
   * means "any".
   */
  @Query(
      "select t from TrainerProfile t where t.active = true"
          + " and (:sport is null or t.id in"
          + " (select tp.id from TrainerProfile tp join tp.sports s where s.slug = :sport))"
          + " and (:city is null or lower(t.city) = lower(:city))")
  Page<TrainerProfile> search(
      @Param("sport") String sport, @Param("city") String city, Pageable pageable);
}
