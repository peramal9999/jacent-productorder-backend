package com.jacent.storefront.query;

import com.jacent.storefront.configuration.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "user.queries")
@PropertySource(value = "classpath:queries/user-queries.yaml", factory = YamlPropertySourceFactory.class)
@Data
public class UserQueries {
    private String userByEmail;
    private String emailExists;
    private String createUser;
}
