package com.jacent.storefront.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ItemsFilterRequest {

    @NotNull(message = "Page ID is required")
    private Integer pageNo = 0;
    private Integer pageSize = 25;
    private List<Integer> commodityIds = new ArrayList<>();
    private BigDecimal priceRangeMin;
    private BigDecimal priceRangeMax;
}
