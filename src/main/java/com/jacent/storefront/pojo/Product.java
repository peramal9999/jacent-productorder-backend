package com.jacent.storefront.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private Integer productId;
    private String productName;
    private String category;
    private String division;
    private String upcCode;
    private String saleUnit;
    private BigDecimal price;
    private BigDecimal retailPrice;
    private String note;
}
