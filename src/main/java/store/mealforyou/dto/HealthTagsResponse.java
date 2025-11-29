package store.mealforyou.dto;

import store.mealforyou.constant.ProductTag;

import java.util.List;

public record HealthTagsResponse(
        List<ProductTag> healthTags
) {}
