package org.fit.ssapp.dto.request;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class GameTheoryProblemDtoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private GameTheoryProblemDto nonRelativePayoffDto;
    private GameTheoryProblemDto relativePayoffDto;

    @BeforeEach
    void setUp() {
        nonRelativePayoffDto = setUpNonRelativePayoffCase();
        relativePayoffDto = setUpRelativePayoffCase();
    }

    @Test
    void testCustomNonRelativePayoffFunction() throws Exception {
        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonRelativePayoffDto)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void testCustomRelativePayoffFunction() throws Exception {
        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relativePayoffDto)))
            .andDo(print())
            .andExpect(status().isOk());
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
        dto.setAlgorithm("PSO");
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
        dto.setDefaultPayoffFunction("(u1+u2+u3)/3-(u4+u5)/2");

        List<NormalPlayer> players = Arrays.asList(
            createNormalPlayer("Player 1", new double[][]{{9.0, 4.0, 7.0, 2.0, 6.0}, {5.0, 6.0, 8.0, 3.0, 7.0}}),
            createNormalPlayer("Player 2", new double[][]{{7.0, 3.0, 6.0, 1.0, 5.0}, {8.0, 2.0, 9.0, 4.0, 2.0}})
        );

        dto.setNormalPlayers(players);
        dto.setAlgorithm("PSO");
        dto.setMaximizing(false);
        dto.setDistributedCores("all");
        dto.setMaxTime(5000);
        dto.setGeneration(100);
        dto.setPopulationSize(1000);
        return dto;
    }

    private NormalPlayer createNormalPlayer(String name, double[][] strategyData) {
        NormalPlayer player = new NormalPlayer();
        player.setName(name);
        player.setStrategies(Arrays.asList(
            createStrategy(strategyData[0]),
            createStrategy(strategyData[1])
        ));
        return player;
    }

    private Strategy createStrategy(double... properties) {
        Strategy strategy = new Strategy();
        for (double prop : properties) {
            strategy.addProperty(prop);
        }
        return strategy;
    }
}
