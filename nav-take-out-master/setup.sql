USE smart_guide_db;

-- 1. users table
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT,
    `phone` VARCHAR(20) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `avatar_url` VARCHAR(512) DEFAULT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
    `status` TINYINT NOT NULL DEFAULT 1,
    `password_last_changed_at` TIMESTAMP NULL DEFAULT NULL,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone_active` (`phone`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. scenic_spots table
CREATE TABLE IF NOT EXISTS `scenic_spots` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT NOT NULL,
    `image_url` VARCHAR(512) DEFAULT NULL,
    `audio_url` VARCHAR(512) DEFAULT NULL,
    `latitude` DECIMAL(10, 7) NOT NULL,
    `longitude` DECIMAL(10, 7) NOT NULL,
    `location` POINT NOT NULL SRID 4326,
    `radius` INT NOT NULL DEFAULT 50,
    `updated_by` BIGINT UNSIGNED DEFAULT NULL,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    SPATIAL KEY `idx_spatial_location` (`location`),
    FULLTEXT KEY `ft_idx_spot_search` (`name`, `description`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. scenic_spots_audit_log table
CREATE TABLE IF NOT EXISTS `scenic_spots_audit_log` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT,
    `spot_id` BIGINT UNSIGNED NOT NULL,
    `action_type` VARCHAR(20) NOT NULL,
    `before_snapshot` JSON DEFAULT NULL,
    `after_snapshot` JSON DEFAULT NULL,
    `operator_id` BIGINT UNSIGNED NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_spot_id` (`spot_id`),
    KEY `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. user_audio_settings table
CREATE TABLE IF NOT EXISTS `user_audio_settings` (
    `user_id` BIGINT UNSIGNED NOT NULL,
    `auto_play_enabled` TINYINT(1) NOT NULL DEFAULT 0,
    `repeat_policy` TINYINT NOT NULL DEFAULT 1,
    `switch_policy` TINYINT NOT NULL DEFAULT 1,
    `background_play_enabled` TINYINT(1) NOT NULL DEFAULT 0,
    `default_speed` DECIMAL(2,1) NOT NULL DEFAULT 1.0,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. user_playback_history table
CREATE TABLE IF NOT EXISTS `user_playback_history` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `spot_id` BIGINT UNSIGNED NOT NULL,
    `play_count` INT NOT NULL DEFAULT 0,
    `last_triggered_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_spot` (`user_id`, `spot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. invitation_codes table
CREATE TABLE IF NOT EXISTS `invitation_codes` (
    `id` INT UNSIGNED AUTO_INCREMENT,
    `code` VARCHAR(50) NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `created_by` BIGINT UNSIGNED DEFAULT NULL,
    `used_by_user_id` BIGINT UNSIGNED DEFAULT NULL,
    `expire_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `used_at` TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
