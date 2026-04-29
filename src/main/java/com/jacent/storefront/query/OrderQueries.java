package com.jacent.storefront.query;

import com.jacent.storefront.configuration.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "order.queries")
@PropertySource(value = "classpath:queries/order-queries.yaml", factory = YamlPropertySourceFactory.class)
@Data
public class OrderQueries {
    private String orderByOrderId;
    private String orderItemsByOrderId;
    private String ordersByUserId;
    private String createOrder;
    private String addItemToOrder;
}
