package com.jacent.storefront.service.impl;

import com.jacent.storefront.dto.request.ItemsFilterRequest;
import com.jacent.storefront.dto.response.FilterOptions;
import com.jacent.storefront.dto.response.ItemsResponse;
import com.jacent.storefront.entity.*;
import com.jacent.storefront.repository.CommodityRepository;
import com.jacent.storefront.repository.DivisionRepository;
import com.jacent.storefront.repository.ItemRepository;
import com.jacent.storefront.repository.StoreRepository;
import com.jacent.storefront.service.ConfigurationService;
import com.jacent.storefront.service.ItemService;
import com.jacent.storefront.service.OpenSearchService;
import com.jacent.storefront.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private final ConfigurationService configurationService;
    private final ItemRepository itemRepository;
    private final DivisionRepository divisionRepository;
    private final CommodityRepository commodityRepository;
    private final OpenSearchService openSearchService;
    private final StoreRepository storeRepository;

    ItemServiceImpl(ItemRepository itemRepository, ConfigurationService configurationService, DivisionRepository divisionRepository, CommodityRepository commodityRepository , OpenSearchService openSearchService, StoreRepository storeRepository) {
        this.itemRepository = itemRepository;
        this.configurationService = configurationService;
        this.divisionRepository = divisionRepository;
        this.commodityRepository = commodityRepository;
        this.openSearchService = openSearchService;
        this.storeRepository = storeRepository;
    }

    @Override
    public ItemsResponse getItems(Integer pageNo, Integer pageSize) {
        if(pageSize == null || pageSize <= 0){
            pageSize = configurationService.getValueAsInteger(Configuration.PAGINATION_SIZE, 25);
        }
        User user = SecurityUtils.getCurrentUser();
        long total = itemRepository.getTotalItemsCount(user.getStoreId());

        List<Item> itemList = itemRepository.getAllItemsPagination(pageNo, pageSize, user.getStoreId());
        return ItemsResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .content(itemList)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / pageSize))
                .build();
    }

    @Override
    public ItemsResponse getItemsByFilter(ItemsFilterRequest itemsFilterRequest) {
        if(itemsFilterRequest.getPageSize() == null || itemsFilterRequest.getPageSize() <= 0){
            Integer pageSize = configurationService.getValueAsInteger(Configuration.PAGINATION_SIZE, 25);
            itemsFilterRequest.setPageSize(pageSize);
        }
        User user = SecurityUtils.getCurrentUser();
        long total = itemRepository.getAllItemsCountByFilterAndPagination(user.getStoreId(), itemsFilterRequest);

        List<Item> itemList = itemRepository.getAllItemsFilterAndPagination(user.getStoreId(), itemsFilterRequest);
        return ItemsResponse.builder()
                .pageNo(itemsFilterRequest.getPageNo())
                .pageSize(itemsFilterRequest.getPageSize())
                .content(itemList)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / itemsFilterRequest.getPageSize()))
                .build();
    }

    @Override
    public List<Item> searchItems(String searchKeyword) throws IOException {
        boolean enableFullTextOpenSearch = configurationService.getValueAsBoolean(Configuration.ENABLE_FULL_TEXT_OPEN_SEARCH, false);
        if(enableFullTextOpenSearch && !StringUtils.isEmpty(searchKeyword)){
            return openSearchService.searchItems(searchKeyword.trim());
        } else {
            // Search from DB
            String escapedKeyword = escapeSearchKeyword(searchKeyword.trim());
            User user = SecurityUtils.getCurrentUser();
            Integer pageSize = configurationService.getValueAsInteger(Configuration.PAGINATION_SIZE, 25);
            return itemRepository.searchItemsByStoreIdAndSearchKeyword(user.getStoreId(), escapedKeyword, pageSize);
        }
    }

    @Override
    public void rebuildOpenSearchIndexForItems() {
        try {
            // TODO: add in bulk (maybe create separate index for store)
            User user = SecurityUtils.getCurrentUser();
            List<Item> itemList = itemRepository.getAllItemsPagination(0, 1000, user.getStoreId());
            openSearchService.bulkIndexProducts(itemList);
        } catch (Exception e){
            log.error("Error occured while bulkIndexProducts");
        }
    }

    @Override
    public FilterOptions getFilterOptions() {
        User user = SecurityUtils.getCurrentUser();
        Store store = storeRepository.findStoreByStoreId(user.getStoreId());
        Location location = storeRepository.findLocationByLocationId(store.getLocationId());
        List<Division> divisions = divisionRepository.findAllDivisionsByStoreId(store.getStoreId());
        List<Commodity> commodities = commodityRepository.findAllCommoditiesByStoreId(store.getStoreId());
        FilterOptions filterOptions = FilterOptions.builder()
                .store(store)
                .location(location)
                .divisions(divisions)
                .commodities(commodities)
                .build();
        return filterOptions;
    }

    private String escapeSearchKeyword(String keyword) {
        return keyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
