package com.jacent.storefront.controller;

import com.jacent.storefront.dto.response.FilterOptions;
import com.jacent.storefront.dto.response.ItemsResponse;
import com.jacent.storefront.entity.Item;
import com.jacent.storefront.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequestMapping("/api/v1")
@RestController
public class ItemController {

    ItemService itemService;

    ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/filterOptions")
    public ResponseEntity<FilterOptions> getFilterOptions() {
        return ResponseEntity.ok(itemService.getFilterOptions());
    }

    @GetMapping("/items")
    public ResponseEntity<ItemsResponse> getItems(@RequestParam(defaultValue = "0") Integer pageNo,
                                                     @RequestParam(required = false) Integer pageSize) {
        return ResponseEntity.ok(itemService.getItems(pageNo, pageSize));
    }

    @GetMapping("/items/search")
    public ResponseEntity<List<Item>> searchItems(@RequestParam(required = true) String searchString) throws IOException {
        return ResponseEntity.ok(itemService.searchItems(searchString));
    }
}
