package com.jacent.storefront.repository;

import com.jacent.storefront.pojo.User;
import com.jacent.storefront.query.UserQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserQueries userQueries;

    public boolean existsByEmail(String email) {
        Boolean exists = jdbcTemplate.queryForObject(
                userQueries.getEmailExists(),
                Boolean.class,
                email
        );
        return Boolean.TRUE.equals(exists);
    }

    public User findByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject(
                    userQueries.getUserByEmail(),
                    new BeanPropertyRowMapper<>(User.class),
                    email
            );
        } catch (EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
    }

    public User createUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    userQueries.getCreateUser(),
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setBoolean(5, user.isEnabled());
            ps.setBoolean(6, user.isLocked());
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new RuntimeException("Failed to retrieve generated user ID");
        }

        user.setUserId(generatedId.intValue());
        return user;
    }

}
