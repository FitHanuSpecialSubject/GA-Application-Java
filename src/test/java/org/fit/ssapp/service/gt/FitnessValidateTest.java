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
 * Integration test for validating FitnessFunction in GameTheory.
 *
 * This test focuses on checking for validation errors with invalid input
 * and ensuring that the system returns the appropriate error message,
 * as well as validating the response structure for valid inputs.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class FitnessValidateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // test case invalid fitness
    @ParameterizedTest
    @MethodSource("invalidFitnessFunctionProvider")
    void testInvalidFitnessFunctions(GameTheoryProblemDto dto) throws Exception {
        mockMvc.perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // test for valid fitness function
    @ParameterizedTest
    @MethodSource("validFitnessFunctionProvider")
    void testValidFitnessFunctions(GameTheoryProblemDto dto) throws Exception {
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
        assertTrue(jsonNode.has("data"));
        final JsonNode data = jsonNode.get("data");
        assertTrue(data.has("fitnessValue"));        
    }

    private static Stream<Arguments> invalidFitnessFunctionProvider() {
        return Stream.of(
            Arguments.of(createDto("++u1 + u2")),
            Arguments.of(createDto("u1 + u10")),
            Arguments.of(createDto("((u1+ u3")),
            Arguments.of(createDto("u1 / 0")),
            Arguments.of(createDto("u12 + u2")),
            Arguments.of(createDto("u1 + (u2 * 3")),
            Arguments.of(createDto("unknownFunc(u1)")),
            Arguments.of(createDto("log()")),
            Arguments.of(createDto("u1 + u2 + @@@"))
        );
    }

    private static Stream<Arguments> validFitnessFunctionProvider() {
        GameTheoryProblemDto dto_u1u2u3 = setUpTestCase();
        List<NormalPlayer> threePlayers = new ArrayList<>();
        threePlayers.add(getNormalPlayers().get(0));
        threePlayers.add(getNormalPlayers().get(0));
        threePlayers.add(getNormalPlayers().get(0));
        dto_u1u2u3.setNormalPlayers(threePlayers);

        return Stream.of(
            Arguments.of(createDto("PRODUCT")),
            Arguments.of(createDto("MAX")),
            Arguments.of(createDto("MIN")),
            Arguments.of(createDto("AVERAGE")),
            Arguments.of(createDto("MEDIAN")),
            Arguments.of(createDto("RANGE")),
            Arguments.of(createDto("u1 + u2", 2)),
            Arguments.of(createDto("u1 * 2 + u3 / 4", 3)),
            Arguments.of(createDto("log(u1) + sqrt(u2)", 2)),
            Arguments.of(createDto("abs(u1 - u2)", 2))
        );
    }

    private static GameTheoryProblemDto createDto(String fitnessFunction) {
        GameTheoryProblemDto dto = setUpTestCase();
        dto.setFitnessFunction(fitnessFunction);
        return dto;
    }

    private static GameTheoryProblemDto createDto(String fitnessFunction, int numberOfPlayers) {
        GameTheoryProblemDto dto = setUpTestCase();
        dto.setFitnessFunction(fitnessFunction);
        List<NormalPlayer> players = new ArrayList<>();
        NormalPlayer basePlayer = getNormalPlayers().isEmpty() ? new NormalPlayer() : getNormalPlayers().get(0);
        for (int i = 0; i < numberOfPlayers; i++) {
            players.add(basePlayer);
        }
        dto.setNormalPlayers(players);
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
        player.setPayoffFunction("SUM");

        final List<NormalPlayer> players = new ArrayList<>(3);
        players.add(player);
        players.add(player);
        players.add(player);
        return players;
    }
}
