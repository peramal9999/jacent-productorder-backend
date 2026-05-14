package com.jacent.storefront.repository;

import com.jacent.storefront.entity.User;
import com.jacent.storefront.exception.ResourceCreationException;
import com.jacent.storefront.query.UserQueries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Slf4j
@Repository
public class UserRepository {

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    UserQueries userQueries;

    public boolean existsByEmail(String email) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", email.trim().toLowerCase());

        try {
            Integer count = namedParameterJdbcTemplate.queryForObject(
                    userQueries.getEmailExists(),
                    params,
                    Integer.class
            );
            return count != null && count > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
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
        log.info("Creating new user with email: {}", user.getEmail());

        String userId = UUID.randomUUID().toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        params.addValue("firstName", user.getFirstName().trim());
        params.addValue("lastName", user.getLastName().trim());
        params.addValue("email", user.getEmail().toLowerCase().trim());
        params.addValue("password", user.getPassword());
        params.addValue("storeId", user.getStoreId());
        params.addValue("isEnabled", user.isEnabled() ? 1 : 0);
        params.addValue("isLocked", user.isLocked() ? 1 : 0);

        try {
            namedParameterJdbcTemplate.update(userQueries.getCreateUser(), params);
        } catch (DataIntegrityViolationException e) {
            log.warn("User creation failed - duplicate email: {}", user.getEmail());
            throw new ResourceCreationException("User with this email already exists", e);
        } catch (BadSqlGrammarException e) {
            log.error("User creation failed - SQL error", e);
            throw new ResourceCreationException("Invalid user data provided", e);
        } catch (Exception e) {
            log.error("User creation failed for email: {}", user.getEmail(), e);
            throw new ResourceCreationException("Failed to create user", e);
        }

        user.setUserId(userId);
        log.info("User created successfully with ID: {} and email: {}", userId, user.getEmail());

        return user;
    }

}
