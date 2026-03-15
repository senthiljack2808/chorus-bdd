package org.example.reportportalspringbootapp.service;

/**
 * Service interface for test suite operations
 */
public interface TestSuiteService {

    /**
     * Gets the owner name for a given test suite
     * @param suiteName The test suite name
     * @return Owner name or null if not found
     * @throws RuntimeException if database operation fails
     */
    String getOwnerName(String suiteName);
}
