package com.jacent.storefront.service.impl;

import com.jacent.storefront.entity.AppConfig;
import com.jacent.storefront.entity.Configuration;
import com.jacent.storefront.repository.ConfigurationRepository;
import com.jacent.storefront.service.ConfigurationService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository appConfigRepository;
    private List<AppConfig> configs = new ArrayList<>();

    public ConfigurationServiceImpl(ConfigurationRepository appConfigRepository) {
        this.appConfigRepository = appConfigRepository;
    }

    @PostConstruct
    public void load() {
//        configs = appConfigRepository.findAll();
        log.info("Loaded {} app configs from DB", configs.size());
    }

    @Override
    public List<AppConfig> getAll() {
        return configs;
    }

    @Override
    public String getValueAsString(Configuration configuration, String defaultValue) {
        return getByName(configuration.getKey())
                .map(AppConfig::getValue)
                .orElse(defaultValue);
    }

    @Override
    public Integer getValueAsInteger(Configuration configuration, Integer defaultValue) {
        return getByName(configuration.getKey())
                .map(c -> Integer.parseInt(c.getValue()))
                .orElse(defaultValue);
    }

    @Override
    public Boolean getValueAsBoolean(Configuration configuration, Boolean defaultValue) {
        return getByName(configuration.getKey())
                .map(c -> Boolean.parseBoolean(c.getValue()))
                .orElse(defaultValue);
    }

    // Force reload from DB without restart
    @Override
    public void reload() {
        configs = appConfigRepository.findAll();
        log.info("Reloaded {} app configs from DB", configs.size());
    }

    private Optional<AppConfig> getByName(String name) {
        return configs.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst();
    }
}
