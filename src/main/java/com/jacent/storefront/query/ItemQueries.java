package com.jacent.storefront.query;

import com.jacent.storefront.configuration.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@ConfigurationProperties(prefix = "item.queries")
@PropertySource(value = "classpath:queries/item-queries.yaml", factory = YamlPropertySourceFactory.class)
@Data
public class ItemQueries {
    private String itemById;
    private String allItemsByIdIn;
    private String divisionsByStoreId;
    private String commoditiesByStoreId;
    private String itemCountByStoreId;
    private String allItemsByStoreId;
    private String searchItemsByStoreIdAndSearchKeyword;
}
