package com.jacent.storefront.controller;

import com.jacent.storefront.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final JdbcTemplate jdbcTemplate;

    TestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/dbconnection")
    public ResponseEntity<?> checkDbConnection() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        if(result != null && result == 1){
            return ResponseEntity.ok("Database connection is successful!");
        } else {
            return ResponseEntity.status(500).body("Database connection failed!");
        }
    }
}
