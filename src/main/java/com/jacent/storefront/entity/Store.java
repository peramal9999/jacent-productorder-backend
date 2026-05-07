package com.jacent.storefront.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    private int storeId;
    private String companyName;
    private int storeNumber;
    private int locationId;
    private int divisionNameId;
}
