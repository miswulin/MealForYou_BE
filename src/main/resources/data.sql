-- Gemini 생성 테스트용 데이터 --

-- Dish 1번: 인기 1위, 추천 3위
INSERT INTO dish (name, base_price, popularity_rank, recommend_rank)
VALUES ('맛있는 밀키트 A', 15000, 1, 3);

-- Dish 2번: 인기 2위, 추천 1위
INSERT INTO dish (name, base_price, popularity_rank, recommend_rank)
VALUES ('고단백 밀키트 B', 18000, 2, 1);

-- Dish 3번: 인기 3위, 추천 2위
INSERT INTO dish (name, base_price, popularity_rank, recommend_rank)
VALUES ('저염 밀키트 C', 16500, 3, 2);

-- Dish 1번의 이미지들 (1번은 대표, 2번은 상세)
INSERT INTO dish_image (dish_id, path, sequence, image_type)
VALUES (1, 'https://example.com/images/mealkit_a_main.jpg', 1, 'REPRESENTATIVE');

INSERT INTO dish_image (dish_id, path, sequence, image_type)
VALUES (1, 'https://example.com/images/mealkit_a_detail.jpg', 2, 'DETAIL_INFO');

-- Dish 2번의 이미지들 (3번은 대표)
INSERT INTO dish_image (dish_id, path, sequence, image_type)
VALUES (2, 'https://example.com/images/mealkit_b_main.jpg', 1, 'REPRESENTATIVE');

-- Dish 3번: '저염 밀키트 C' (대표 이미지가 없는 케이스)
INSERT INTO dish_image (dish_id, path, sequence, image_type)
VALUES (3, 'https://example.com/images/mealkit_c_detail.jpg', 1, 'DETAIL_INFO');

-- Dish 4번: 인기 4위, 추천 4위
INSERT INTO dish (name, base_price, popularity_rank, recommend_rank)
VALUES ('비건 밀키트 D', 17000, 4, 4);
INSERT INTO dish_image (dish_id, path, sequence, image_type) VALUES (4, '...', 1, 'REPRESENTATIVE');

-- Dish 5번: 인기 5위, 추천 5위
INSERT INTO dish (name, base_price, popularity_rank, recommend_rank)
VALUES ('매콤 밀키트 E', 15500, 5, 5);
INSERT INTO dish_image (dish_id, path, sequence, image_type) VALUES (5, '...', 1, 'REPRESENTATIVE');

-- Dish 6번: 인기 6위, 추천 6위
INSERT INTO dish (name, base_price, popularity_rank, recommend_rank)
VALUES ('간편 밀키트 F', 14000, 6, 6);
INSERT INTO dish_image (dish_id, path, sequence, image_type) VALUES (6, '...', 1, 'REPRESENTATIVE');