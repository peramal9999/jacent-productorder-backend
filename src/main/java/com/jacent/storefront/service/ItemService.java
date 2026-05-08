package com.jacent.storefront.service;

import com.jacent.storefront.dto.request.ItemsFilterRequest;
import com.jacent.storefront.dto.response.FilterOptions;
import com.jacent.storefront.dto.response.ItemsResponse;
import com.jacent.storefront.entity.Item;

import java.io.IOException;
import java.util.List;


public interface ItemService {

    ItemsResponse getItems(Integer pageNo, Integer pageSize);

    ItemsResponse getItemsByFilter(ItemsFilterRequest itemsFilterRequest);

    List<Item> searchItems(String searchStrings) throws IOException;

    void rebuildOpenSearchIndexForItems();

    FilterOptions getFilterOptions();

}
