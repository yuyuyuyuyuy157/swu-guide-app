USE smart_guide_db;
DELETE FROM user_playback_history;
DELETE FROM user_audio_settings;
DELETE FROM invitation_codes;
DELETE FROM scenic_spots_audit_log;
DELETE FROM scenic_spots;
DELETE FROM users;
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE scenic_spots AUTO_INCREMENT = 1;
