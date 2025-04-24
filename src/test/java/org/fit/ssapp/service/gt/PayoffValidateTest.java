package org.fit.ssapp.service.gt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Validates the correctness of payoff functions (both default and per-player)
 * and related parameters in game theory problems.
 * Payoff function uses p1, p2... for properties and P1p1, P2p2... for player-property references.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PayoffValidateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test invalid default payoff function syntax.
     */
    @ParameterizedTest
    @MethodSource("invalidPayoffSyntaxProvider")
    void testInvalidDefaultPayoffSyntax(GameTheoryProblemDto dto) throws Exception {
        mockMvc.perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Test invalid parameters/arguments in default payoff functions.
     */
    @ParameterizedTest
    @MethodSource("invalidPayoffParamsProvider")
    void testInvalidDefaultPayoff(GameTheoryProblemDto dto) throws Exception {
        mockMvc.perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Test valid default aggregation functions.
     */
    @ParameterizedTest
    @MethodSource("validAggregationFunctionsProvider")
    void testValidDefaultAggregationFunctions(GameTheoryProblemDto dto) throws Exception {
        MvcResult result = this.mockMvc
                .perform(post("/api/game-theory-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(request().asyncStarted())
                .andReturn();

        this.mockMvc.perform(asyncDispatch(result))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testIndividualPlayerPayoffFunctionValidation_DivisionByZero() throws Exception {
        GameTheoryProblemDto dto = setUpTestCase();
        List<NormalPlayer> players = new ArrayList<>(dto.getNormalPlayers());
        NormalPlayer Player = players.get(0);
        Player.setPayoffFunction("p1 / 0");

        mockMvc.perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testIndividualPlayerPayoffFunctionValidation_InvalidPropertyRef() throws Exception {
        GameTheoryProblemDto dto = setUpTestCase();
        List<NormalPlayer> players = new ArrayList<>(dto.getNormalPlayers());
        NormalPlayer playerToModify = players.get(0);
        playerToModify.setPayoffFunction("p1 + p8");

        mockMvc.perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testIndividualPlayerPayoffFunctionValidation_InvalidPlayerRef() throws Exception {
        GameTheoryProblemDto dto = setUpTestCase();
        List<NormalPlayer> players = new ArrayList<>(dto.getNormalPlayers());
        NormalPlayer playerToModify = players.get(0);
        playerToModify.setPayoffFunction("P4p1 + p2");

        mockMvc.perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testValidIndividualPlayerReferencingPayoffs() throws Exception {
        GameTheoryProblemDto dto = setUpTestCase();
        List<NormalPlayer> players = new ArrayList<>(dto.getNormalPlayers());
        players.get(0).setPayoffFunction("P2p1 + P3p2");
        players.get(1).setPayoffFunction("p1 * P1p2");
        players.get(2).setPayoffFunction("MAX");

        MvcResult result = this.mockMvc
                .perform(post("/api/game-theory-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(request().asyncStarted())
                .andReturn();

        final String response = this.mockMvc.perform(asyncDispatch(result))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final JsonNode jsonNode = objectMapper.readTree(response);
        final JsonNode data = jsonNode.get("data");
        assertTrue(data.has("payoffs"));
    }

    private static Stream<Arguments> invalidPayoffSyntaxProvider() {
        return Stream.of(
            Arguments.of(createDto("++p1 + p2")),
            Arguments.of(createDto("p1 + p8")),
            Arguments.of(createDto("cos()")),
            Arguments.of(createDto("p1 ++ p2")),
            Arguments.of(createDto("p1 + ((p2 * 3")),
            Arguments.of(createDto("INVALID_FUNCTION(p1)")),
            Arguments.of(createDto("p0 + p1")),
            Arguments.of(createDto("p2 @ p1 + @@@"))
        );
    }

    private static Stream<Arguments> invalidPayoffParamsProvider() {
        return Stream.of(
            Arguments.of(createDto("pow(p1)")),
            Arguments.of(createDto("log(p1, p2)")),
            Arguments.of(createDto("sqrt(p1, p2, p3)")),
            Arguments.of(createDto("cos()"))
        );
    }

    private static Stream<Arguments> validAggregationFunctionsProvider() {
        return Stream.of(
            Arguments.of(createDto("SUM")),
            Arguments.of(createDto("AVERAGE")),
            Arguments.of(createDto("MIN")),
            Arguments.of(createDto("MAX")),
            Arguments.of(createDto("PRODUCT")),
            Arguments.of(createDto("MEDIAN")),
            Arguments.of(createDto("RANGE"))
        );
    }

    private static GameTheoryProblemDto createDto(String defaultPayoff) {
        GameTheoryProblemDto dto = setUpTestCase();
        dto.setDefaultPayoffFunction(defaultPayoff);
        if (dto.getNormalPlayers() != null) {
             dto.getNormalPlayers().forEach(p -> p.setPayoffFunction(null));
        }
        return dto;
    }

    private static GameTheoryProblemDto setUpTestCase() {
        List<NormalPlayer> players = getNormalPlayers();
        GameTheoryProblemDto dto = new GameTheoryProblemDto();
        dto.setSpecialPlayer(null);
        dto.setNormalPlayers(players);
        dto.setFitnessFunction("DEFAULT");
        dto.setDefaultPayoffFunction("DEFAULT");
        dto.setAlgorithm("NSGAII");
        dto.setMaximizing(true);
        dto.setDistributedCores("all");
        dto.setMaxTime(5000);
        dto.setGeneration(100);
        dto.setPopulationSize(100);
        return dto;
    }

    private static List<NormalPlayer> getNormalPlayers() {
        final List<Double> stratProps = new ArrayList<>(4);
        stratProps.add(1.0d);
        stratProps.add(2.0d);
        stratProps.add(4.0d);
        stratProps.add(3.0d);
        final double payoff = 10d;

        final Strategy strat = new Strategy();
        strat.setPayoff(payoff);
        strat.setProperties(stratProps);

        final List<Strategy> strats = new ArrayList<>(3);
        strats.add(strat);
        strats.add(strat);
        strats.add(strat);

        final NormalPlayer player = new NormalPlayer();
        player.setStrategies(strats);
        player.setPayoffFunction(null);

        final List<NormalPlayer> players = new ArrayList<>(3);
        players.add(player);
        players.add(player);
        players.add(player);
        return players;
    }
}
    
