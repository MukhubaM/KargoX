package com.kargox.dto.booking;

import com.kargox.entity.GoodsCategory;
import com.kargox.entity.GoodsSizeTier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoodsItemDto {

    @NotBlank
    private String name;

    @NotNull
    private GoodsCategory category;

    @NotNull
    private GoodsSizeTier sizeTier;

    @Positive
    private Integer quantity = 1;

    private boolean fragile = false;

    // Optional
    private String photoUrl;
}
