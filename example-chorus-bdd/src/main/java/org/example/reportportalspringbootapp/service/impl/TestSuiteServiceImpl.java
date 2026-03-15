package org.example.reportportalspringbootapp.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.reportportalspringbootapp.exception.DatabaseServiceException;
import org.example.reportportalspringbootapp.service.TestSuiteService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Implementation of TestSuite service operations
 */
@Service
@Slf4j
public class TestSuiteServiceImpl implements TestSuiteService {

    private final JdbcTemplate jdbcTemplate;

    public TestSuiteServiceImpl(@Qualifier("externalJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getOwnerName(String suiteName) {
        if (suiteName == null || suiteName.trim().isEmpty()) {
            throw new IllegalArgumentException("Suite name cannot be null or empty");
        }

        String query = """
            SELECT uname 
            FROM users 
            WHERE id = (
                SELECT Owner_id 
                FROM TestSuites 
                WHERE TestSuite_Name = ?
            )
        """;

        try {
            log.debug("Fetching owner for test suite: {}", suiteName);

            return jdbcTemplate.queryForObject(query, String.class, suiteName);

        } catch (DataAccessException e) {
            log.error("Database error while fetching owner for suite: {}", suiteName, e);
            return null; // Return null if not found or error occurred
        } catch (Exception e) {
            log.error("Unexpected error while fetching owner for suite: {}", suiteName, e);
            throw new DatabaseServiceException("Failed to fetch owner for test suite: " + suiteName, e);
        }
    }
}
