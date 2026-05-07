package com.jacent.storefront.repository;

import com.jacent.storefront.entity.User;
import com.jacent.storefront.query.UserQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    UserQueries userQueries;

    public boolean existsByEmail(String email) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", email);
        Boolean exists = namedParameterJdbcTemplate.queryForObject(
                userQueries.getEmailExists(),
                params,
                Boolean.class
        );
        return Boolean.TRUE.equals(exists);
    }

    public User findByEmail(String email) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("email", email);
            return namedParameterJdbcTemplate.queryForObject(
                    userQueries.getUserByEmail(),
                    params,
                    new BeanPropertyRowMapper<>(User.class)
            );
        } catch (EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
    }

    public User createUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("firstName", user.getFirstName());
        params.addValue("lastName", user.getLastName());
        params.addValue("email", user.getEmail());
        params.addValue("password", user.getPassword());
        params.addValue("storeId", user.getStoreId());
        params.addValue("isEnabled", user.isEnabled());
        params.addValue("isLocked", user.isLocked());

        namedParameterJdbcTemplate.update(
                userQueries.getCreateUser(),
                params,
                keyHolder
        );

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new RuntimeException("Failed to retrieve generated user ID");
        }

        user.setUserId(generatedId.intValue());
        return user;
    }

}
