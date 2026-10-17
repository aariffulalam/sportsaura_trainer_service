package com.sportsaura.trainerservice.dto.response;

import java.util.List;
import java.util.Map;

/**
 * All website text for one page, grouped by section.
 *
 * @param sections section key (e.g. "hero", "features") to its blocks, in display order
 */
public record PageContentResponse(String page, Map<String, List<ContentBlockResponse>> sections) {}
