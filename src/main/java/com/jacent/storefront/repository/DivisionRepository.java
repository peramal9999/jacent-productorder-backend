package com.jacent.storefront.repository;

import com.jacent.storefront.entity.Division;
import com.jacent.storefront.query.ItemQueries;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DivisionRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ItemQueries itemQueries;

    public DivisionRepository(JdbcTemplate jdbcTemplate, ItemQueries itemQueries) {
        this.jdbcTemplate = jdbcTemplate;
        this.itemQueries = itemQueries;
    }

    public List<Division> findAll() {
        return jdbcTemplate.query(
                itemQueries.getDivisions(),
                new BeanPropertyRowMapper<>(Division.class)
        );
    }
}
