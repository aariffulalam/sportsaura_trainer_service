-- A trainer's public profile. user_id is the account's id in the user service; there is no
-- foreign key to it because that table lives in another service's database.

CREATE TABLE trainer_profiles (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    user_id          BIGINT        NOT NULL,
    display_name     VARCHAR(100)  NOT NULL,
    headline         VARCHAR(150)  NOT NULL,
    bio              VARCHAR(2000) NOT NULL,
    city             VARCHAR(100)  NOT NULL,
    experience_years INT           NOT NULL,
    hourly_rate_inr  INT           NOT NULL,
    active           BIT(1)        NOT NULL DEFAULT b'1',
    created_at       DATETIME(6)   NOT NULL,
    updated_at       DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_trainer_profiles_user_id UNIQUE (user_id),
    INDEX idx_trainer_profiles_city (city)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Which sports each trainer teaches (both tables live in this service, so foreign keys are fine).
CREATE TABLE trainer_sports (
    trainer_profile_id BIGINT NOT NULL,
    sport_id           BIGINT NOT NULL,
    PRIMARY KEY (trainer_profile_id, sport_id),
    CONSTRAINT fk_trainer_sports_trainer FOREIGN KEY (trainer_profile_id)
        REFERENCES trainer_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_trainer_sports_sport FOREIGN KEY (sport_id) REFERENCES sports (id),
    INDEX idx_trainer_sports_sport (sport_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
