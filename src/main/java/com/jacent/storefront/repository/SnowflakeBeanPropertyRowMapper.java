package com.jacent.storefront.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.beans.PropertyDescriptor;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SnowflakeBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {

    public SnowflakeBeanPropertyRowMapper(Class<T> mappedClass) {
        super(mappedClass);
    }

    @Override
    protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException, SQLException {
        if (LocalDateTime.class == pd.getPropertyType()) {
            Timestamp ts = rs.getTimestamp(index);
            return ts != null ? ts.toLocalDateTime() : null;
        }
        if (LocalDate.class == pd.getPropertyType()) {
            Date d = rs.getDate(index);
            return d != null ? d.toLocalDate() : null;
        }
        return super.getColumnValue(rs, index, pd);
    }
}