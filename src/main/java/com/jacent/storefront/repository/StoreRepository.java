package com.jacent.storefront.repository;

import com.jacent.storefront.entity.Location;
import com.jacent.storefront.entity.Store;
import com.jacent.storefront.query.StoreQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class StoreRepository {
    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    StoreQueries storeQueries;

    public Store findStoreByStoreId(int storeId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("storeId", storeId);
            return namedParameterJdbcTemplate.queryForObject(
                    storeQueries.getStoreById(),
                    params,
                    new BeanPropertyRowMapper<>(Store.class)
            );
        } catch (EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("Store not found for storeId: " + storeId);
        }
    }

    public Location findLocationByLocationId(int locationId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("locationId", locationId);
            return namedParameterJdbcTemplate.queryForObject(
                    storeQueries.getLocationByLocationId(),
                    params,
                    new BeanPropertyRowMapper<>(Location.class)
            );
        } catch (EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("Location not found for locationId: " + locationId);
        }
    }
}
