package com.sportsaura.trainerservice.repository;

import com.sportsaura.trainerservice.entity.Sport;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SportRepository extends JpaRepository<Sport, Long> {

  List<Sport> findByActiveTrueOrderByDisplayOrderAscNameAsc();

  Optional<Sport> findBySlugAndActiveTrue(String slug);

  List<Sport> findByIdInAndActiveTrue(Collection<Long> ids);

  /** How many active trainers teach each sport. Sports without trainers are not listed. */
  @Query(
      "select new com.sportsaura.trainerservice.repository.SportTrainerCount(s.id, count(t))"
          + " from TrainerProfile t join t.sports s where t.active = true group by s.id")
  List<SportTrainerCount> countActiveTrainersPerSport();
}
