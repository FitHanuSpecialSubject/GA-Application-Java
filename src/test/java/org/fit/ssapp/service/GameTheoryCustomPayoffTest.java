package org.fit.ssapp.dto.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

/**
 * Tests focused on custom payoff functions in Game Theory
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GameTheoryCustomPayoffTest extends BaseGameTheoryTest {

    @Autowired
    private MockMvc mockMvc;

    private GameTheoryProblemDto nonRelativePayoffDto;
    private GameTheoryProblemDto relativePayoffDto;

    @BeforeEach
    void setUp() {
        nonRelativePayoffDto = setUpNonRelativePayoffCase();
        relativePayoffDto = setUpRelativePayoffCase();
    }

    @Test
    void testCustomNonRelativePayoffFunction() throws Exception {
        MvcResult result = this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonRelativePayoffDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        Response response = safelyParseWithJsonNode(result, true);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void testCustomRelativePayoffFunction() throws Exception {
        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relativePayoffDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "(u1 + u2 + ) / 3 - (u4 + u5",
        "u1 + u2 * / u3",
        "(p1 + p2 +) * p3",
        "u1 + u2 + invalid"
    })
    void testInvalidPayoffFunctionSyntax(String invalidFunction) throws Exception {
        GameTheoryProblemDto invalidDto = setUpNonRelativePayoffCase();
        invalidDto.setDefaultPayoffFunction(invalidFunction);

        MvcResult result = this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        Response response = safelyParseWithJsonNode(result, false);
        assertNotNull(response);
        assertEquals(500, response.getStatus());
        assertTrue(response.getMessage().contains("Invalid"));
    }

    private GameTheoryProblemDto setUpNonRelativePayoffCase() {
        GameTheoryProblemDto dto = new GameTheoryProblemDto();
        dto.setFitnessFunction("DEFAULT");
        dto.setDefaultPayoffFunction("(u1+u2+u3)/3-(u4+u5)/2");

        List<NormalPlayer> players = Arrays.asList(
            createNormalPlayer("Player 1", new double[][]{{10.0, 5.0, 8.0, 4.0, 2.0}, {3.0, 7.0, 6.0, 1.0, 5.0}}),
            createNormalPlayer("Player 2", new double[][]{{6.0, 2.0, 9.0, 3.0, 7.0}, {4.0, 8.0, 5.0, 2.0, 6.0}})
        );

        dto.setNormalPlayers(players);
        dto.setAlgorithm("NSGAII");
        dto.setMaximizing(false);
        dto.setDistributedCores("all");
        dto.setMaxTime(5000);
        dto.setGeneration(100);
        dto.setPopulationSize(1000);
        return dto;
    }

    private GameTheoryProblemDto setUpRelativePayoffCase() {
        GameTheoryProblemDto dto = new GameTheoryProblemDto();
        dto.setFitnessFunction("DEFAULT");
        dto.setDefaultPayoffFunction("(p1+p2)/(p3+p4+1)");

        List<NormalPlayer> players = Arrays.asList(
            createNormalPlayer("Player 1", new double[][]{{9.0, 4.0, 7.0, 2.0, 6.0}, {5.0, 6.0, 8.0, 3.0, 7.0}}),
            createNormalPlayer("Player 2", new double[][]{{7.0, 3.0, 6.0, 1.0, 5.0}, {8.0, 2.0, 9.0, 4.0, 2.0}})
        );

        dto.setNormalPlayers(players);
        dto.setAlgorithm("PESA2");
        dto.setMaximizing(false);
        dto.setDistributedCores("all");
        dto.setMaxTime(5000);
        dto.setGeneration(100);
        dto.setPopulationSize(1000);
        return dto;
    }
} 
