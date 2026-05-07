package com.jacent.storefront.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private int locationId;
    private String locationName;
    private String locationFullname;
    private String addr1;
    private String addr2;
    private String city;
    private String state;
    private String zip;
    private String country;
}
