package store.mealforyou.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductTag {
    HIGH_PROTEIN("고단백"),
    LOW_CARB("저탄수"),
    GLUTEN_FREE("글루텐프리"),
    LOW_SODIUM("저염"),
    LOW_GLYCEMIC("저혈당"),
    VEGAN("비건");

    private final String description; // 한글 설명 (화면 표시용)
}