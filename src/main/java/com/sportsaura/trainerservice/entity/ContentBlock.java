package com.sportsaura.trainerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * One piece of website text: a heading, a paragraph, an optional icon and button. A row of the
 * {@code content_blocks} table.
 *
 * <p>Blocks are grouped by page (e.g. "home") and section (e.g. "features") and shown in {@code
 * sortOrder}. Changing a row changes the website without a new release.
 */
@Entity
@Table(name = "content_blocks")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentBlock {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "page_key", nullable = false, length = 50)
  private String pageKey;

  @Column(name = "section_key", nullable = false, length = 50)
  private String sectionKey;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(nullable = false, length = 2000)
  private String body;

  @Column(length = 10)
  private String icon;

  @Column(name = "link_label", length = 50)
  private String linkLabel;

  @Column(name = "link_url", length = 200)
  private String linkUrl;

  @Builder.Default
  @Column(nullable = false)
  private boolean active = true;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
