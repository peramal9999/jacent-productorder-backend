package com.jacent.storefront.repository;

import com.jacent.storefront.dto.request.ItemsFilterRequest;
import com.jacent.storefront.exception.ResourceRetrievalException;
import com.jacent.storefront.query.ItemQueries;
import com.jacent.storefront.entity.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class ItemRepository {

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final BeanPropertyRowMapper<Item> ITEM_ROW_MAPPER =
            new BeanPropertyRowMapper<>(Item.class);

    @Autowired
    ItemQueries itemQueries;

    public List<Item> getAllItemsPagination(int page, int size, Integer storeId) {
        log.debug("Fetching items for store: {} - page: {}, size: {}", storeId, page, size);

        long offset = (long) page * size;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("storeId", storeId);
        StringBuilder sql = new StringBuilder(itemQueries.getAllItemsByStoreId());
        appendPageSizeAndOffset(sql, offset, size, params);

        try {
            List<Item> items = namedParameterJdbcTemplate.query(
                    sql.toString(),
                    params,
                    ITEM_ROW_MAPPER
            );
            log.debug("Retrieved {} items for store: {}", items.size(), storeId);
            return items;
        } catch (DataAccessException e) {
            log.error("Failed to retrieve items for store: {}", storeId, e);
            throw new ResourceRetrievalException("Failed to retrieve items", e);
        }
    }

    public int getAllItemsCountByFilterAndPagination(Integer storeId, ItemsFilterRequest itemsFilterRequest) {
        log.debug("Fetching total item count with filter  for store: {}", storeId);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("storeId", storeId);
        StringBuilder sql = new StringBuilder(itemQueries.getItemCountByStoreId());
        appendCommodityAndPriceFilterQuery(sql, itemsFilterRequest, params);
        try {
            Integer count = namedParameterJdbcTemplate.queryForObject(
                    sql.toString(),
                    params,
                    Integer.class
            );
            int result = count != null ? count : 0;
            log.debug("Total item count for store {} is: {}", storeId, result);
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.debug("No items found for store: {}", storeId);
            return 0;
        } catch (DataAccessException e) {
            log.error("Failed to retrieve item count for store: {}", storeId, e);
            throw new ResourceRetrievalException("Failed to retrieve item count", e);
        }
    }

    public List<Item> getAllItemsFilterAndPagination(Integer storeId, ItemsFilterRequest itemsFilterRequest) {
        log.debug("Fetching items with filter for store: {} - page: {}, size: {}", storeId, itemsFilterRequest.getPageNo(), itemsFilterRequest.getPageSize());

        long offset = (long) itemsFilterRequest.getPageNo() * itemsFilterRequest.getPageSize();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("storeId", storeId);

        StringBuilder sql = new StringBuilder(itemQueries.getAllItemsByStoreId());
        appendCommodityAndPriceFilterQuery(sql, itemsFilterRequest, params);
        appendPageSizeAndOffset(sql, offset, itemsFilterRequest.getPageSize(), params);

        try {
            List<Item> items = namedParameterJdbcTemplate.query(
                    sql.toString(),
                    params,
                    ITEM_ROW_MAPPER
            );
            log.debug("Retrieved {} items for store: {}", items.size(), storeId);
            return items;
        } catch (DataAccessException e) {
            log.error("Failed to retrieve items for store: {}", storeId, e);
            throw new ResourceRetrievalException("Failed to retrieve items", e);
        }
    }

    private static void appendPageSizeAndOffset(StringBuilder sql, long offset, int size, MapSqlParameterSource params) {
        sql.append("LIMIT :size OFFSET :offset");
        params.addValue("size", size);
        params.addValue("offset", (int) offset);
    }

    private static void appendCommodityAndPriceFilterQuery(StringBuilder sql, ItemsFilterRequest itemsFilterRequest, MapSqlParameterSource params) {
        if (itemsFilterRequest.getCommodityIds() != null && !itemsFilterRequest.getCommodityIds().isEmpty()) {
            sql.append(" AND COMMODITY_ID IN (:commodityIds) ");
            params.addValue("commodityIds", itemsFilterRequest.getCommodityIds());
        }

        if (itemsFilterRequest.getPriceRangeMin() != null && itemsFilterRequest.getPriceRangeMin() != null && itemsFilterRequest.getPriceRangeMin().intValue() > 0 && itemsFilterRequest.getPriceRangeMax().intValue() > 0) {
            sql.append(" AND RETAIL_PRICE BETWEEN :minPrice AND :maxPrice ");
            params.addValue("minPrice", itemsFilterRequest.getPriceRangeMin());
            params.addValue("maxPrice", itemsFilterRequest.getPriceRangeMax());
        }
    }

    public int getTotalItemsCount(Integer storeId) {
        log.debug("Fetching total item count for store: {}", storeId);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("storeId", storeId);

        try {
            Integer count = namedParameterJdbcTemplate.queryForObject(
                    itemQueries.getItemCountByStoreId(),
                    params,
                    Integer.class
            );
            int result = count != null ? count : 0;
            log.debug("Total item count for store {} is: {}", storeId, result);
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.debug("No items found for store: {}", storeId);
            return 0;
        } catch (DataAccessException e) {
            log.error("Failed to retrieve item count for store: {}", storeId, e);
            throw new ResourceRetrievalException("Failed to retrieve item count", e);
        }
    }

    public List<Item> searchItemsByStoreIdAndSearchKeyword(Integer storeId, String keyword, Integer pageSize) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("storeId", storeId);
        params.addValue("search", "%" + keyword + "%");
        params.addValue("size", pageSize);

        try {
            List<Item> items = namedParameterJdbcTemplate.query(
                    itemQueries.getSearchItemsByStoreIdAndSearchKeyword(),
                    params,
                    ITEM_ROW_MAPPER
            );
            log.debug("Search returned {} items for keyword: {}", items.size(), keyword);
            return items;
        } catch (DataAccessException e) {
            log.error("Search failed for keyword: {} in store: {}", keyword, storeId, e);
            throw new ResourceRetrievalException("Search operation failed", e);
        }
    }
}
