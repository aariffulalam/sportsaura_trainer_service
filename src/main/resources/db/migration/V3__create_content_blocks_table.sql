-- Website text, grouped by page and section. Editing a row changes the website without a release.

CREATE TABLE content_blocks (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    page_key    VARCHAR(50)   NOT NULL,
    section_key VARCHAR(50)   NOT NULL,
    sort_order  INT           NOT NULL,
    title       VARCHAR(150)  NOT NULL,
    body        VARCHAR(2000) NOT NULL,
    icon        VARCHAR(10)   NULL,
    link_label  VARCHAR(50)   NULL,
    link_url    VARCHAR(200)  NULL,
    active      BIT(1)        NOT NULL DEFAULT b'1',
    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_content_blocks_page (page_key, section_key, sort_order)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
