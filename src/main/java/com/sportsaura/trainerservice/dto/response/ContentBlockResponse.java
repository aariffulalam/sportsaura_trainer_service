package com.sportsaura.trainerservice.dto.response;

/**
 * One piece of website text.
 *
 * @param icon emoji, or null
 * @param linkLabel button text, or null when the block has no button
 * @param linkUrl where the button goes, e.g. "/register"
 */
public record ContentBlockResponse(
    String title, String body, String icon, String linkLabel, String linkUrl) {}
