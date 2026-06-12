package com.wondealer.dto.response;

import com.wondealer.entity.GameCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameCategoryResDto {

    private Long categoryId;
    private String categoryName;

    public static GameCategoryResDto from(GameCategory category) {
        return GameCategoryResDto.builder()
                .categoryId(category.getId())
                .categoryName(category.getCategoryName())
                .build();
    }
}
