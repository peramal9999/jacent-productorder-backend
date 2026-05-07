package com.jacent.storefront.repository;

import com.jacent.storefront.entity.Commodity;
import com.jacent.storefront.query.ItemQueries;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommodityRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ItemQueries itemQueries;

    public CommodityRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, ItemQueries itemQueries) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.itemQueries = itemQueries;
    }

    public List<Commodity> findAllCommoditiesByStoreId(int storeId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("storeId", storeId);

        return namedParameterJdbcTemplate.query(
                itemQueries.getCommoditiesByStoreId(),
                params,
                new BeanPropertyRowMapper<>(Commodity.class)
        );
    }
}
