CREATE DATABASE IF NOT EXISTS smart_guide_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_guide_db;

-- ===================================================================
-- 1. 用户信息表 (重构：引入账户状态机、软删除及安全重登时效控制)
-- ===================================================================
CREATE TABLE `users` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '唯一标识',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号（明文存储，界面渲染展示时需脱敏处理）',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '加密后的密码（前端提供明暗文眼睛图标切换）',
    `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT '自定义头像URL，若为NULL则前端展示品牌默认剪影头像',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色类型：USER(普通用户), ADMIN(管理员)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账户状态：1-正常, 2-冻结, 3-注销',
    `password_last_changed_at` TIMESTAMP NULL DEFAULT NULL COMMENT '密码最后修改时间（用于强制清理存量 JWT Token，保障安全重登流转）',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否软删除：0-未删除, 1-已删除',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册/创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '账户信息最后修改时间',
    `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone_active` (`phone`, `is_deleted`) -- 联合索引确保未删除的手机号唯一，支持注销后重新注册
) ENGINE=InnoDB COMMENT='用户信息表';


-- ===================================================================
-- 2. 景点信息表 (重构：升级 SPATIAL 空间索引以支撑高并发围栏计算，引入全文检索与软删除)
-- ===================================================================
CREATE TABLE `scenic_spots` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '景点唯一标识',
    `name` VARCHAR(100) NOT NULL COMMENT '景点名称（列表展示的核心文本）',
    `description` TEXT NOT NULL COMMENT '介绍文本（支持骨架屏过渡加载，B端抽屉编辑）',
    `image_url` VARCHAR(512) DEFAULT NULL COMMENT '景点卡片默认展示图片',
    `audio_url` VARCHAR(512) DEFAULT NULL COMMENT 'AI 自动生成的语音讲解音频缓存文件路径',
    `latitude` DECIMAL(10, 7) NOT NULL COMMENT '纬度面显展现字段（限制输入数字与小数点）',
    `longitude` DECIMAL(10, 7) NOT NULL COMMENT '经度面显展现字段（限制输入数字与小数点）',
    `location` POINT NOT NULL SRID 4326 COMMENT '空间几何点坐标（包含经纬度，由地理围栏高频空间索引计算专用）',
    `radius` INT NOT NULL DEFAULT 50 COMMENT '感应范围（米，电子围栏触发核心依据）',
    `updated_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '上次修改人 ID（关联用户表，用于低对比度审计流展示）',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否软删除：0-未删除, 1-已删除',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '上次修改时间（用于低对比度审计流展示）',
    `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    SPATIAL KEY `idx_spatial_location` (`location`), -- 空间索引：彻底干掉全表扫描，专为用户进出围栏计算设计
    FULLTEXT KEY `ft_idx_spot_search` (`name`, `description`) WITH PARSER ngram, -- 中文全文检索索引，优化B端及搜索框检索效率
    CONSTRAINT `fk_spots_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT -- 改为 RESTRICT，由软删除保证历史可溯源
) ENGINE=InnoDB COMMENT='景点信息表';


-- ===================================================================
-- 3. 景点变更审计日志表 (全新引入：彻底闭环 B 端“数据防错与追责回滚”痛点)
-- ===================================================================
CREATE TABLE `scenic_spots_audit_log` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '审计日志唯一标识',
    `spot_id` BIGINT UNSIGNED NOT NULL COMMENT '关联景点 ID',
    `action_type` VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE(创建), UPDATE(修改), DELETE(软删除)',
    `before_snapshot` JSON DEFAULT NULL COMMENT '修改前的老数据快照 (JSON 存储)',
    `after_snapshot` JSON DEFAULT NULL COMMENT '修改后的新数据快照 (JSON 存储)',
    `operator_id` BIGINT UNSIGNED NOT NULL COMMENT '操作管理员 ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_spot_id` (`spot_id`),
    KEY `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB COMMENT='景点变更审计日志表';


-- ===================================================================
-- 4. 自动播放设置表 (重构：补齐用户端播放倍速等全局个性化偏好控制)
-- ===================================================================
CREATE TABLE `user_audio_settings` (
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联用户 ID',
    `auto_play_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '自动播放总开关：0-手动点播（关闭）, 1-开启自动（开启）',
    `repeat_policy` TINYINT NOT NULL DEFAULT 1 COMMENT '策略一（频次）：1-每个景点只播放一次, 2-可以播放多次',
    `switch_policy` TINYINT NOT NULL DEFAULT 1 COMMENT '策略二（触发）：1-播完再切, 2-随位置实时切, 3-增加弹窗提醒',
    `background_play_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '后台播放设置开关：0-关闭, 1-开启（触发后台定位权限检测）',
    `default_speed` DECIMAL(2,1) NOT NULL DEFAULT 1.0 COMMENT '默认讲解播放倍速偏好：1.0, 1.2, 1.5, 2.0',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '配置最后更新时间',
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_settings_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='用户自动播放设置表';


-- ===================================================================
-- 5. 用户播放足迹表 (保持：精准去重逻辑，建立良好的联合唯一覆盖)
-- ===================================================================
CREATE TABLE `user_playback_history` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '唯一标识',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
    `spot_id` BIGINT UNSIGNED NOT NULL COMMENT '景点 ID',
    `play_count` INT NOT NULL DEFAULT 0 COMMENT '累计播放次数（用于去重控制判断）',
    `last_triggered_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近一次触发/播放的时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_spot` (`user_id`, `spot_id`), 
    CONSTRAINT `fk_history_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_history_spot_id` FOREIGN KEY (`scenic_spots`.`id` 对应的旧外键已解绑) REFERENCES `scenic_spots` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='用户播放足迹表';


-- ===================================================================
-- 6. 注册邀请码表 (重构：升级布尔值为严谨状态机，引入过期机制与发放追溯)
-- ===================================================================
CREATE TABLE `invitation_codes` (
    `id` INT UNSIGNED AUTO_INCREMENT COMMENT '唯一标识',
    `code` VARCHAR(50) NOT NULL COMMENT '邀请码文本',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态枚举：1-未使用, 2-已使用, 3-已作废, 4-已过期',
    `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '生成该码的管理员 ID',
    `used_by_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '被哪位新注册的用户使用了',
    `expire_at` TIMESTAMP NULL DEFAULT NULL COMMENT '邀请码失效截止时间',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    `used_at` TIMESTAMP NULL DEFAULT NULL COMMENT '实际被使用的时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    CONSTRAINT `fk_codes_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_codes_used_by` FOREIGN KEY (`used_by_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='注册邀请码表';