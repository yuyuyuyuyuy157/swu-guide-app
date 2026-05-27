USE smart_guide_db;

-- Test users (password is MD5("12345678" + phone))
-- Admin: 13900139000 / 12345678
-- User:  13800138000 / 12345678
INSERT INTO users (id, phone, password_hash, avatar_url, role, status, is_deleted)
VALUES
(1, '13900139000', MD5(CONCAT('12345678', '13900139000')), 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', 'ADMIN', 1, 0),
(2, '13800138000', MD5(CONCAT('12345678', '13800138000')), 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', 'USER', 1, 0);

-- Test scenic spots
INSERT INTO scenic_spots (id, name, description, image_url, audio_url, latitude, longitude, location, radius, is_deleted)
VALUES
(1, '共青团花园',
 '西南大学标志性景观之一，绿树成荫，是同学们晨读、散步和拍照打卡的好去处。',
 'https://images.unsplash.com/photo-1596422846543-75c6fc197f07?auto=format&fit=crop&w=500&q=60',
 'https://downsc.chinaz.net/Files/DownLoad/sound1/201906/11582.mp3',
 29.815, 106.425,
 ST_GeomFromText('POINT(29.815 106.425)', 4326),
 50, 0),
(2, '崇德湖',
 '崇德湖是西南大学校园内一片宁静的水域，湖面碧波荡漾，四周绿树环绕，是师生们休闲放松的理想场所。',
 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=500&q=60',
 'https://downsc.chinaz.net/Files/DownLoad/sound1/201906/11582.mp3',
 29.818, 106.428,
 ST_GeomFromText('POINT(29.818 106.428)', 4326),
 80, 0),
(3, '中心图书馆',
 '西南大学中心图书馆藏书丰富，是西南地区最大的高校图书馆之一，为师生提供优质的学习环境。',
 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?auto=format&fit=crop&w=500&q=60',
 'https://downsc.chinaz.net/Files/DownLoad/sound1/201906/11582.mp3',
 29.812, 106.422,
 ST_GeomFromText('POINT(29.812 106.422)', 4326),
 60, 0);

-- Test invitation code (never expires)
INSERT INTO invitation_codes (id, code, status)
VALUES (1, 'ABC123', 1);
