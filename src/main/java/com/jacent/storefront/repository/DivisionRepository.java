package com.jacent.storefront.repository;

import com.jacent.storefront.entity.Division;
import com.jacent.storefront.query.ItemQueries;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DivisionRepository {

    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final ItemQueries itemQueries;

    public DivisionRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, ItemQueries itemQueries) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.itemQueries = itemQueries;
    }

    public List<Division> findAllDivisionsByStoreId(int storeId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("storeId", storeId);

        return namedParameterJdbcTemplate.query(
                itemQueries.getDivisionsByStoreId(),
                params,
                new BeanPropertyRowMapper<>(Division.class)
        );
    }
}
