package com.jacent.storefront.repository;

import com.jacent.storefront.entity.Commodity;
import com.jacent.storefront.exception.ResourceRetrievalException;
import com.jacent.storefront.query.ItemQueries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class CommodityRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ItemQueries itemQueries;

    private static final BeanPropertyRowMapper<Commodity> COMMODITY_ROW_MAPPER =
            new BeanPropertyRowMapper<>(Commodity.class);

    public CommodityRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, ItemQueries itemQueries) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.itemQueries = itemQueries;
    }

    public List<Commodity> findAllCommoditiesByStoreId(int storeId) {
        log.debug("Fetching commodities for store ID: {}", storeId);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("storeId", storeId);

        try {
            List<Commodity> commodities = namedParameterJdbcTemplate.query(
                    itemQueries.getCommoditiesByStoreId(),
                    params,
                    COMMODITY_ROW_MAPPER
            );

            if (commodities.isEmpty()) {
                log.info("No commodities found for store ID: {}", storeId);
            } else {
                log.debug("Retrieved {} commodities for store ID: {}", commodities.size(), storeId);
            }

            return commodities;
        } catch (DataAccessException e) {
            log.error("Database error while retrieving commodities for store ID: {}", storeId, e);
            throw new ResourceRetrievalException("Failed to retrieve commodities for store ID: " + storeId, e);
        }
    }
}
