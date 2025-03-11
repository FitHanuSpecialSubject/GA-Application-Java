package org.fit.ssapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

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
     * @param result MvcResult từ test
     * @param expectedSuccess Có expected success response hay không
     * @return Response object đã được parse
     * @throws Exception nếu có lỗi xảy ra khi xử lý
     */
    protected Response safelyParseWithJsonNode(MvcResult result, boolean expectedSuccess) throws Exception {
        String responseBody = result.getResponse().getContentAsString();

        JsonNode rootNode = objectMapper.readTree(responseBody);

        Response response = new Response();
        JsonNode statusNode = rootNode.get("status");
        JsonNode messageNode = rootNode.get("message");
        JsonNode dataNode = rootNode.get("data");

        if (statusNode != null && !statusNode.isNull() && statusNode.isInt()) {
            response.setStatus(statusNode.asInt());
        }

        if (messageNode != null && !messageNode.isNull() && messageNode.isTextual()) {
            response.setMessage(messageNode.asText());
        }

        if (dataNode != null && !dataNode.isNull()) {
            response.setData(dataNode);
        }

        if (expectedSuccess) {
            assertEquals(200, response.getStatus(), "Expected status 200 for success case");
            assertNotNull(response.getData(), "Response data should not be null for success case");
        } else {
            assertEquals(500, response.getStatus(), "Expected status 500 for error case");
            assertNotNull(response.getMessage(), "Error message should not be null for error case");
        }

        return response;
    }

    /**
     * @param result MvcResult from test
     * @param expectedSuccess expected success response or not
     * @return Response object
     * @throws Exception if there is an error when processing
     */
    protected Response safelyParseResponse(MvcResult result, boolean expectedSuccess) throws Exception {
        String responseBody = result.getResponse().getContentAsString();

        Response response = objectMapper.readValue(responseBody, Response.class);

        if (expectedSuccess) {
            assertEquals(200, response.getStatus(), "Expected status 200 for success case");
            assertNotNull(response.getData(), "Response data should not be null for success case");
        } else {
            assertEquals(500, response.getStatus(), "Expected status 500 for error case");
            assertNotNull(response.getMessage(), "Error message should not be null for error case");
        }

        return response;
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


    protected Strategy createStrategy(double... properties) {
        Strategy strategy = new Strategy();
        for (double prop : properties) {
            strategy.addProperty(prop);
        }
        return strategy;
    }
}
