-- Gemini 생성 테스트용 데이터 --

-- H2/MySQL 호환용 주석 --

-- Dish 1번: '맛있는 밀키트 A'
INSERT INTO dish (name, base_price) VALUES ('맛있는 밀키트 A', 15000);

-- Dish 1번의 이미지들 (1번은 대표, 2번은 상세)
INSERT INTO dish_image (dish_id, path, sequence, image_type)
VALUES (1, 'https://example.com/images/mealkit_a_main.jpg', 1, 'REPRESENTATIVE');

INSERT INTO dish_image (dish_id, path, sequence, image_type)
VALUES (1, 'https://example.com/images/mealkit_a_detail.jpg', 2, 'DETAIL_INFO');


-- Dish 2번: '고단백 밀키트 B'
INSERT INTO dish (name, base_price) VALUES ('고단백 밀키트 B', 18000);

-- Dish 2번의 이미지들 (3번은 대표)
INSERT INTO dish_image (dish_id, path, sequence, image_type)
VALUES (2, 'https://example.com/images/mealkit_b_main.jpg', 1, 'REPRESENTATIVE');


-- Dish 3번: '저염 밀키트 C' (대표 이미지가 없는 케이스)
INSERT INTO dish (name, base_price) VALUES ('저염 밀키트 C', 16500);

INSERT INTO dish_image (dish_id, path, sequence, image_type)
VALUES (3, 'https://example.com/images/mealkit_c_detail.jpg', 1, 'DETAIL_INFO');