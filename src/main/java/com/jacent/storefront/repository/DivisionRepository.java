package com.jacent.storefront.repository;

import com.jacent.storefront.entity.Division;
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
public class DivisionRepository {

    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final ItemQueries itemQueries;

    private static final BeanPropertyRowMapper<Division> DIVISION_ROW_MAPPER =
            new BeanPropertyRowMapper<>(Division.class);

    public DivisionRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, ItemQueries itemQueries) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.itemQueries = itemQueries;
    }

    public List<Division> findAllDivisionsByStoreId(int storeId) {
        log.debug("Fetching divisions for store ID: {}", storeId);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("storeId", storeId);

        try {
            List<Division> divisions = namedParameterJdbcTemplate.query(
                    itemQueries.getDivisionsByStoreId(),
                    params,
                    DIVISION_ROW_MAPPER
            );

            if (divisions.isEmpty()) {
                log.info("No divisions found for store ID: {}", storeId);
            } else {
                log.debug("Retrieved {} divisions for store ID: {}", divisions.size(), storeId);
            }

            return divisions;
        } catch (DataAccessException e) {
            log.error("Database error while retrieving divisions for store ID: {}", storeId, e);
            throw new ResourceRetrievalException("Failed to retrieve divisions for store ID: " + storeId, e);
        }
    }
}
