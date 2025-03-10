package org.fit.ssapp.dto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.UnsupportedEncodingException;

import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;

/**
 * Base class for Game Theory tests with common helper methods
 */
public abstract class BaseGameTheoryTest {
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    /**
     * Safely parse response body using JsonNode approach
     * @param result The MvcResult from test
     * @param expectedSuccess Whether we expect a successful response
     * @return The parsed Response object or null if empty/invalid
     */
    protected Response safelyParseWithJsonNode(MvcResult result, boolean expectedSuccess) {
        String responseBody;
        try {
            responseBody = result.getResponse().getContentAsString();
        } catch (UnsupportedEncodingException e) {
            fail("Failed to get response content: " + e.getMessage());
            return null;
        }
        
        // For empty response body
        if (responseBody == null || responseBody.isEmpty()) {
            int statusCode = result.getResponse().getStatus();
            if (expectedSuccess) {
                fail("Expected non-empty success response body but got empty response with status " + statusCode);
            } else {
                assertTrue(statusCode >= 400, "HTTP status should indicate an error when no response body");
            }
            return null;
        }
        
        try {
            // Parse to JsonNode first instead of directly to Response
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            // Check if we have a valid JSON response
            if (rootNode == null || rootNode.isEmpty()) {
                fail("Response body is not valid JSON: " + responseBody);
                return null;
            }
            
            // Access fields safely
            JsonNode statusNode = rootNode.get("status");
            JsonNode messageNode = rootNode.get("message");
            JsonNode dataNode = rootNode.get("data");
            
            // Create a Response object manually
            Response response = new Response();
            
            if (statusNode != null && !statusNode.isNull()) {
                response.setStatus(statusNode.asInt());
            }
            
            if (messageNode != null && !messageNode.isNull()) {
                response.setMessage(messageNode.asText());
            }
            
            if (dataNode != null && !dataNode.isNull()) {
                // For data, we can keep it as JsonNode or convert it
                response.setData(dataNode);
            }
            
            // Validate based on expected outcome
            if (expectedSuccess) {
                assertEquals(200, response.getStatus());
                assertNotNull(response.getData(), "Response data should not be null for success case");
            } else {
                assertEquals(500, response.getStatus());
            }
            
            return response;
        } catch (Exception e) {
            fail("Failed to parse response body: " + e.getMessage() + ", Response body: " + responseBody);
            return null;
        }
    }
    
    /**
     * Safely parse response body and handle potential exceptions
     * @param result The MvcResult from test
     * @param expectedSuccess Whether we expect a successful response
     * @return The parsed Response object or null if empty/invalid
     */
    protected Response safelyParseResponse(MvcResult result, boolean expectedSuccess) {
        String responseBody;
        try {
            responseBody = result.getResponse().getContentAsString();
        } catch (UnsupportedEncodingException e) {
            fail("Failed to get response content: " + e.getMessage());
            return null;
        }
        
        // Handle empty response
        if (responseBody == null || responseBody.isEmpty()) {
            int statusCode = result.getResponse().getStatus();
            if (expectedSuccess) {
                fail("Expected non-empty success response body but got empty response with status " + statusCode);
                return null;
            } else {
                // For error cases, we're okay with HTTP error status without body
                assertTrue(statusCode >= 400, "HTTP status should indicate an error when no response body");
                return null;
            }
        }
        
        // Handle JSON parsing with try-catch
        try {
            Response response = objectMapper.readValue(responseBody, Response.class);
            if (expectedSuccess) {
                assertEquals(200, response.getStatus());
                assertNotNull(response.getData(), "Response data should not be null for success case");
            } else {
                assertEquals(500, response.getStatus()); 
            }
            return response;
        } catch (JsonProcessingException e) {
            fail("Failed to parse response body: " + e.getMessage() + ", Response body: " + responseBody);
            return null;
        }
    }
    
    /**
     * Helper method to create a normal player with strategies
     */
    protected NormalPlayer createNormalPlayer(String name, double[][] strategyData) {
        NormalPlayer player = new NormalPlayer();
        player.setName(name);
        player.setStrategies(Arrays.asList(
            createStrategy(strategyData[0]),
            createStrategy(strategyData[1])
        ));
        return player;
    }

    /**
     * Helper method to create a strategy with properties
     */
    protected Strategy createStrategy(double... properties) {
        Strategy strategy = new Strategy();
        for (double prop : properties) {
            strategy.addProperty(prop);
        }
        return strategy;
    }
} 
