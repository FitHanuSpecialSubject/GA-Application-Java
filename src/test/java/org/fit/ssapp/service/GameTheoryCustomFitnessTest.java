package org.fit.ssapp.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Tests focused on custom fitness functions and algorithm validations in Game Theory.
 * 
 * Note: For payoff functions, the 'p' prefix is used (e.g., p1, p2).
 * For fitness functions, the 'u' prefix is used (e.g., u1, u2).
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GameTheoryCustomFitnessTest extends BaseGameTheoryTest {

    @Autowired
    private MockMvc mockMvc;

    private GameTheoryProblemDto baseDto;

    @BeforeEach
    void setUp() {
        baseDto = setUpTestCase();
    }

    @Test
    void testCustomFitnessFunction() throws Exception {
        GameTheoryProblemDto testDto = setUpTestCase();
        testDto.setFitnessFunction("(u1+u2)^2/(u3+1)");

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
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "(u1 + u2 + ) / 3 - (u4 + u5",
        "u1 + u2 * / u3",
        "u1 + u9" // Invalid payoff index
    })
    void testInvalidFitnessFunctionSyntax(String invalidFunction) throws Exception {
        GameTheoryProblemDto invalidDto = setUpTestCase();
        invalidDto.setFitnessFunction(invalidFunction);

        MvcResult result = this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
            .andDo(print())
            .andExpect(status().isBadRequest()) 
            .andReturn();
            
        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("BAD_REQUEST", responseNode.path("status").asText());
        assertTrue(responseNode.path("errors").isArray());
    }

    @Test
    void testInvalidRequests() throws Exception {
        GameTheoryProblemDto testDto = setUpTestCase();
        testDto.setMaximizing(true);
        
        MvcResult result = this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();
        
        // check error response
        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("BAD_REQUEST", responseNode.path("status").asText());
        
        // check empty request body
        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
            .andDo(print())
            .andExpect(status().isBadRequest());
        
        // check invalid json
        String invalidJson = "{" +
            "\"fitnessFunction\": \"DEFAULT\"," +
            "\"maxTime\": \"sixty\"," +
            "\"generation\": \"hundred\"" +
            "}";
            
        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    private void testAlgorithm(String algorithm, 
                              java.util.function.Consumer<GameTheoryProblemDto> configurator,
                              java.util.function.Consumer<JsonNode> additionalAssertions) throws Exception {
        GameTheoryProblemDto testDto = setUpTestCase();
        testDto.setAlgorithm(algorithm);
        
        if (configurator != null) {
            configurator.accept(testDto);
        }
        
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
        
        if (additionalAssertions != null) {
            additionalAssertions.accept(dataNode);
        }
    }

    @ParameterizedTest
    @MethodSource("gameTheoryAlgorithms")
    void testFitnessFunction2(String algorithm) throws Exception {
        testAlgorithm(algorithm, dto -> dto.setFitnessFunction("(u1 * u2) / (u3 + 1)"), null);
    }

    @ParameterizedTest
    @MethodSource("gameTheoryAlgorithms")
    void testCustomNonRelativePayoffFunction(String algorithm) throws Exception {
        GameTheoryProblemDto testDto = setupNonRelativePayoffCase(algorithm);
        
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
    void testCustomRelativePayoffFunction(String algorithm) throws Exception {
        GameTheoryProblemDto testDto = setupRelativePayoffCase(algorithm);
        
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

    private static String[] gameTheoryAlgorithms() {
        return org.fit.ssapp.constants.GameTheoryConst.ALLOWED_INSIGHT_ALGORITHMS;
    }

    private GameTheoryProblemDto setUpTestCase() {
        List<NormalPlayer> players = getNormalPlayers();
        GameTheoryProblemDto dto = new GameTheoryProblemDto();
        dto.setSpecialPlayer(null);
        dto.setNormalPlayers(players);
        dto.setFitnessFunction("default"); 
        dto.setDefaultPayoffFunction("default");
        dto.setMaximizing(true);
        dto.setDistributedCores("all");
        dto.setMaxTime(5000);
        dto.setGeneration(10);
        dto.setPopulationSize(100);
        return dto;
    }

    private GameTheoryProblemDto setupNonRelativePayoffCase(String algorithm) {
        GameTheoryProblemDto dto = setUpTestCase();
        dto.setAlgorithm(algorithm);
        dto.setDefaultPayoffFunction("p1 + p2 + p3"); // Custom non-relative payoff
        return dto;
    }
    
    private GameTheoryProblemDto setupRelativePayoffCase(String algorithm) {
        GameTheoryProblemDto dto = setUpTestCase();
        dto.setAlgorithm(algorithm);
        dto.setDefaultPayoffFunction("P2p1 + P1p2"); // Custom relative payoff 
        return dto;
    }
    
    private List<NormalPlayer> getNormalPlayers() {
        final List<Double> stratProps = new ArrayList<Double>(4);
        stratProps.add(1.0d);
        stratProps.add(2.0d);
        stratProps.add(4.0d);
        stratProps.add(3.0d);
        final double payoff = 10d;

        final Strategy strat = new Strategy();
        strat.setPayoff(payoff);
        strat.setProperties(stratProps);

        final List<Strategy> strats = new ArrayList<Strategy>(3);
        strats.add(strat);
        strats.add(strat);
        strats.add(strat);

        final NormalPlayer player = new NormalPlayer();
        player.setStrategies(strats);
        player.setPayoffFunction("default");

        final List<NormalPlayer> players = new ArrayList<NormalPlayer>(3);
        players.add(player);
        players.add(player);
        players.add(player);
        return players;
    }
} 