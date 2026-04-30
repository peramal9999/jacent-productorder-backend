package com.jacent.storefront.repository;

import com.jacent.storefront.entity.Commodity;
import com.jacent.storefront.query.ItemQueries;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommodityRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ItemQueries itemQueries;

    public CommodityRepository(JdbcTemplate jdbcTemplate, ItemQueries itemQueries) {
        this.jdbcTemplate = jdbcTemplate;
        this.itemQueries = itemQueries;
    }

    public List<Commodity> findAll() {
        return jdbcTemplate.query(
                itemQueries.getCommodities(),
                new BeanPropertyRowMapper<>(Commodity.class)
        );
    }
}
