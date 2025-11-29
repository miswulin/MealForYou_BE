package store.mealforyou.dto;

import jakarta.validation.constraints.Size;
import store.mealforyou.constant.ProductTag;

import java.util.List;

public record HealthTagsUpdateRequest(
        @Size(max = 3, message = "선호 식단은 최대 3개까지 선택할 수 있습니다.")
        List<ProductTag> healthTags
) {}
