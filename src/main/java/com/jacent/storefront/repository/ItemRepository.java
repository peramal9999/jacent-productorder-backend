package com.jacent.storefront.repository;

import com.jacent.storefront.query.ItemQueries;
import com.jacent.storefront.entity.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ItemRepository {

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    ItemQueries itemQueries;

    public List<Item> getAllItemsPagination(int page, int size, Integer storeId) {
        int offset = page * size;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("storeId", storeId);
        params.addValue("size", size);
        params.addValue("offset", offset);

        return namedParameterJdbcTemplate.query(
                itemQueries.getAllItemsByStoreId(),
                params,
                new BeanPropertyRowMapper<>(Item.class)
        );
    }

    public int getTotalItemsCount(Integer storeId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("storeId", storeId);
        return namedParameterJdbcTemplate.queryForObject(
                itemQueries.getItemCountByStoreId(),
                params,
                Integer.class
        );
    }

    public List<Item> searchItemsByStoreIdAndSearchKeyword(Integer storeId, String keyword, Integer pageSize) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("storeId", storeId);
        params.addValue("search", "%" + keyword + "%");
        params.addValue("size", pageSize);

        return namedParameterJdbcTemplate.query(
                itemQueries.getSearchItemsByStoreIdAndSearchKeyword(),
                params,
                new BeanPropertyRowMapper<>(Item.class)
        );
    }
}
