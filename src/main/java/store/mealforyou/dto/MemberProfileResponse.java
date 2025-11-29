package store.mealforyou.dto;

import store.mealforyou.constant.ProductTag;

import java.util.List;

public record MemberProfileResponse(
        String email,
        String name,
        String phone, // E.164 형식 전화번호
        AddressDTO address, // 기본 배송지 정보 (없으면 null)
        List<ProductTag> healthTags // 선호 식단 태그 목록
) {}
