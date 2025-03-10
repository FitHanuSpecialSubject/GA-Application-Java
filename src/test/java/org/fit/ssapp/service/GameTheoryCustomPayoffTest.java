package org.fit.ssapp.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

/**
 * Tests focused on custom payoff functions in Game Theory.
 * 
 * Variable syntax for payoff functions:
 * - pi: reference to property i of the current player's strategy (e.g., p1, p2, p3)
 * - Pjpi: reference to property i of player j's strategy (e.g., P1p2 for property 2 of player 1's strategy)
 * 
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GameTheoryCustomPayoffTest extends BaseGameTheoryTest {

    @Autowired
    private MockMvc mockMvc;

    private GameTheoryProblemDto nonRelativePayoffDto;
    private GameTheoryProblemDto relativePayoffDto;
    private GameTheoryProblemDto explicitRelativePayoffDto;

    @BeforeEach
    void setUp() {
        nonRelativePayoffDto = setUpNonRelativePayoffCase();
        relativePayoffDto = setUpRelativePayoffCase();
        explicitRelativePayoffDto = setUpExplicitRelativePayoffCase();
    }

    @Test
    void testCustomNonRelativePayoffFunction() throws Exception {
        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonRelativePayoffDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
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
    
    @Test
    void testExplicitRelativePlayerPropertiesPayoffFunction() throws Exception { //special case the syntax of the payoff function
        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(explicitRelativePayoffDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
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

        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    private GameTheoryProblemDto setUpNonRelativePayoffCase() {
        GameTheoryProblemDto dto = new GameTheoryProblemDto();
        dto.setFitnessFunction("DEFAULT");
        dto.setDefaultPayoffFunction("(P1p1+P2p2+P3p3)/3-(P4p4+P5p5)/2");

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
        dto.setDefaultPayoffFunction("(P2p1+P1p2)/(P2p3+P1p4+1)");

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
    
    private GameTheoryProblemDto setUpExplicitRelativePayoffCase() {
        GameTheoryProblemDto dto = new GameTheoryProblemDto();
        dto.setFitnessFunction("DEFAULT");
        dto.setDefaultPayoffFunction("p1*P2p1+p2*P1p2");

        List<NormalPlayer> players = Arrays.asList(
            createNormalPlayer("Player 1", new double[][]{{5.0, 3.0, 7.0, 2.0, 6.0}, {4.0, 2.0, 8.0, 3.0, 7.0}}),
            createNormalPlayer("Player 2", new double[][]{{6.0, 4.0, 5.0, 2.0, 3.0}, {7.0, 1.0, 9.0, 4.0, 2.0}})
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
} 
