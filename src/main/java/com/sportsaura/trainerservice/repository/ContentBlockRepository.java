package com.sportsaura.trainerservice.repository;

import com.sportsaura.trainerservice.entity.ContentBlock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentBlockRepository extends JpaRepository<ContentBlock, Long> {

  List<ContentBlock> findByPageKeyAndActiveTrueOrderBySectionKeyAscSortOrderAsc(String pageKey);
}
