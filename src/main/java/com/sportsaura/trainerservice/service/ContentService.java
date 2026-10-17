package com.sportsaura.trainerservice.service;

import com.sportsaura.trainerservice.dto.response.ContentBlockResponse;
import com.sportsaura.trainerservice.dto.response.PageContentResponse;
import com.sportsaura.trainerservice.entity.ContentBlock;
import com.sportsaura.trainerservice.exception.ResourceNotFoundException;
import com.sportsaura.trainerservice.mapper.CatalogMapper;
import com.sportsaura.trainerservice.repository.ContentBlockRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Website text stored in the database, served one page at a time. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {

  private final ContentBlockRepository contentRepository;
  private final CatalogMapper mapper;

  public PageContentResponse getPage(String pageKey) {
    List<ContentBlock> blocks =
        contentRepository.findByPageKeyAndActiveTrueOrderBySectionKeyAscSortOrderAsc(pageKey);
    if (blocks.isEmpty()) {
      throw new ResourceNotFoundException("No content for page '" + pageKey + "'");
    }
    Map<String, List<ContentBlockResponse>> sections = new LinkedHashMap<>();
    for (ContentBlock block : blocks) {
      sections
          .computeIfAbsent(block.getSectionKey(), key -> new ArrayList<>())
          .add(mapper.toContentBlockResponse(block));
    }
    return new PageContentResponse(pageKey, sections);
  }
}
