package org.fit.ssapp.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.dto.response.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests focused on custom payoff functions in Game Theory.
 * 
 * Examples:
 * - Non-relative: "(p1+p2+p3)/3-(p4+p5)/2" uses only properties of the current player
 * - Relative: "(p1+P1p2)/(p3+1)" uses both current player properties and other player properties
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GameTheoryCustomPayoffTest extends BaseGameTheoryTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @MethodSource("gameTheoryAlgorithms")
    void testCustomNonRelativePayoffFunction(String algorithm) throws Exception {
    
        GameTheoryProblemDto testDto = setUpNonRelativePayoffCase();
        testDto.setAlgorithm(algorithm);
        
        MvcResult result = this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
            
        Response response = safelyParseWithJsonNode(result, true);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getData());
        
        // Verify algorithm in response matches requested algorithm
        JsonNode dataNode = (JsonNode) response.getData();
        assertEquals(algorithm, dataNode.path("insights").path("algorithmName").asText());
    }

    @ParameterizedTest
    @MethodSource("gameTheoryAlgorithms")
    void testCustomRelativePayoffFunction(String algorithm) throws Exception {
        // Create DTO with relative payoff function
        GameTheoryProblemDto testDto = setUpRelativePayoffCase();
        testDto.setAlgorithm(algorithm);
        
        MvcResult result = this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
            
        Response response = safelyParseWithJsonNode(result, true);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getData());
        
        JsonNode dataNode = (JsonNode) response.getData();
        assertEquals(algorithm, dataNode.path("insights").path("algorithmName").asText());
    }

    @ParameterizedTest
    @MethodSource("gameTheoryAlgorithms")
    void testExplicitRelativePlayerPropertiesPayoffFunction(String algorithm) throws Exception {
        // Create DTO with explicit relative payoff function
        GameTheoryProblemDto testDto = setUpExplicitRelativePayoffCase();
        testDto.setAlgorithm(algorithm);
        
        MvcResult result = this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
            
        Response response = safelyParseWithJsonNode(result, true);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getData());
        
        // Verify algorithm in response matches requested algorithm
        JsonNode dataNode = (JsonNode) response.getData();
        assertEquals(algorithm, dataNode.path("insights").path("algorithmName").asText());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "(p1 + p2 + ) / 3 - (p4 + p5",
        "p1 + p2 * / p3",
        "(p1 + p2 +) * p3",
        "p1 + p2 + invalid",
        "p1 + P9p9" // Invalid player index
    })
    void testInvalidPayoffFunctionSyntax(String invalidFunction) throws Exception {
        GameTheoryProblemDto invalidDto = setUpNonRelativePayoffCase();
        invalidDto.setDefaultPayoffFunction(invalidFunction);

        MvcResult result = this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();
            
        String responseBody = result.getResponse().getContentAsString();
        JsonNode responseNode = objectMapper.readTree(responseBody);
        
        assertEquals("BAD_REQUEST", responseNode.path("status").asText());
        assertTrue(responseNode.path("message").isTextual());
        assertTrue(responseNode.path("errors").isArray());
        assertTrue(responseNode.path("errors").size() > 0);
        
        // Verify that the error message contains "payoff"
        String firstError = responseNode.path("errors").get(0).asText();
        assertTrue(firstError.contains("payoff"), "Error should mention payoff function");
    }

    
    private static String[] gameTheoryAlgorithms() {
        return org.fit.ssapp.constants.GameTheoryConst.ALLOWED_INSIGHT_ALGORITHMS;
    }

    private GameTheoryProblemDto setUpNonRelativePayoffCase() {
        GameTheoryProblemDto dto = new GameTheoryProblemDto();
        dto.setFitnessFunction("default");
        // Non-relative payoff function
        dto.setDefaultPayoffFunction("(p1+p2+p3)/3-(p4+p5)/2");
        
        List<NormalPlayer> players = Arrays.asList(
            createNormalPlayer("Player 1", new double[][]{{10.0, 5.0, 8.0, 4.0, 2.0}, {3.0, 7.0, 6.0, 1.0, 5.0}}),
            createNormalPlayer("Player 2", new double[][]{{6.0, 2.0, 9.0, 3.0, 7.0}, {4.0, 8.0, 5.0, 2.0, 6.0}})
        );

        dto.setNormalPlayers(players);
        dto.setMaximizing(true);
        dto.setDistributedCores("all");
        dto.setMaxTime(5000);
        dto.setGeneration(100);
        dto.setPopulationSize(1000);
        return dto;
    }

    private GameTheoryProblemDto setUpRelativePayoffCase() {
        GameTheoryProblemDto dto = new GameTheoryProblemDto();
        dto.setFitnessFunction("default");
        // Relative payoff function
        dto.setDefaultPayoffFunction("(P2p1+P1p2)/(p3+1)");

        List<NormalPlayer> players = Arrays.asList(
            createNormalPlayer("Player 1", new double[][]{{10.0, 5.0, 8.0, 4.0, 2.0}, {3.0, 7.0, 6.0, 1.0, 5.0}}),
            createNormalPlayer("Player 2", new double[][]{{6.0, 2.0, 9.0, 3.0, 7.0}, {4.0, 8.0, 5.0, 2.0, 6.0}})
        );

        dto.setNormalPlayers(players);
        dto.setMaximizing(true);
        dto.setDistributedCores("all");
        dto.setMaxTime(5000);
        dto.setGeneration(100);
        dto.setPopulationSize(1000);
        return dto;
    }
    
    private GameTheoryProblemDto setUpExplicitRelativePayoffCase() {
        GameTheoryProblemDto dto = new GameTheoryProblemDto();
        dto.setFitnessFunction("default");
        // Explicit relative payoff function
        dto.setDefaultPayoffFunction("P2p1*P1p2");

        List<NormalPlayer> players = Arrays.asList(
            createNormalPlayer("Player 1", new double[][]{{10.0, 5.0, 8.0, 4.0, 2.0}, {3.0, 7.0, 6.0, 1.0, 5.0}}),
            createNormalPlayer("Player 2", new double[][]{{6.0, 2.0, 9.0, 3.0, 7.0}, {4.0, 8.0, 5.0, 2.0, 6.0}})
        );

        dto.setNormalPlayers(players);
        dto.setMaximizing(true);
        dto.setDistributedCores("all");
        dto.setMaxTime(5000);
        dto.setGeneration(100);
        dto.setPopulationSize(1000);
        return dto;
    }
} 