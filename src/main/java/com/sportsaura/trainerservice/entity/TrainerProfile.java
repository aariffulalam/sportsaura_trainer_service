package com.sportsaura.trainerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * A trainer's public profile. A row of the {@code trainer_profiles} table.
 *
 * <p>The person behind it is an account in the user service with role TRAINER. This service keeps
 * only that account's id ({@code userId}), never a copy of the account.
 */
@Entity
@Table(name = "trainer_profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Id of the account in the user service. One profile per account. */
  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Column(name = "display_name", nullable = false, length = 100)
  private String displayName;

  /** One line shown on cards, e.g. "UEFA B licensed football coach". */
  @Column(nullable = false, length = 150)
  private String headline;

  @Column(nullable = false, length = 2000)
  private String bio;

  @Column(nullable = false, length = 100)
  private String city;

  @Column(name = "experience_years", nullable = false)
  private Integer experienceYears;

  /** Price of one hour of training, in whole rupees. */
  @Column(name = "hourly_rate_inr", nullable = false)
  private Integer hourlyRateInr;

  @Builder.Default
  @Column(nullable = false)
  private boolean active = true;

  // Loaded for up to 50 trainers in one query, so a list page does not query once per trainer.
  @Builder.Default
  @ManyToMany
  @BatchSize(size = 50)
  @JoinTable(
      name = "trainer_sports",
      joinColumns = @JoinColumn(name = "trainer_profile_id"),
      inverseJoinColumns = @JoinColumn(name = "sport_id"))
  private Set<Sport> sports = new LinkedHashSet<>();

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
