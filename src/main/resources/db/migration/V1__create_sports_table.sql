-- Flyway migration. Never edit this file after it has run anywhere.
-- To change the schema, add a new file: V<next number>__short_description.sql

CREATE TABLE sports (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    slug          VARCHAR(50)  NOT NULL,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(500) NOT NULL,
    icon          VARCHAR(10)  NOT NULL,
    display_order INT          NOT NULL,
    active        BIT(1)       NOT NULL DEFAULT b'1',
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sports_slug UNIQUE (slug)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
