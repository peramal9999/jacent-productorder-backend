package com.jacent.storefront.dto.response;

import com.jacent.storefront.entity.Commodity;
import com.jacent.storefront.entity.Division;
import com.jacent.storefront.entity.Location;
import com.jacent.storefront.entity.Store;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class FilterOptions {
    private Store store;
    private Location location;
    private List<Division> divisions;
    private List<Commodity> commodities;
}
