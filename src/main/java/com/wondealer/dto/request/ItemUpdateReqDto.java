package com.wondealer.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class ItemUpdateReqDto {

    private String title;

    private String description;

    @Positive
    private Long basePrice;
}
