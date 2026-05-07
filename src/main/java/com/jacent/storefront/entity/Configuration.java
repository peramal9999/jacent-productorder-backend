package com.jacent.storefront.entity;

public enum Configuration {
    PAGINATION_SIZE("pagination.max-size"),
    ENABLE_FULL_TEXT_OPEN_SEARCH("enable-full-text-opensearch"),
    DISPLAY_PAST_ORDERS_MAX_LIMIT("display-past-orders-max-limit");

    private final String key;

    Configuration(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
