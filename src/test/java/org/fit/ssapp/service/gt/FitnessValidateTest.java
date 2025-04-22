package org.fit.ssapp.service.gt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.containsString;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test cho việc kiểm tra lỗi validation của FitnessFunction trong GameTheory.
 * 
 * Test này tập trung vào việc kiểm tra các lỗi validation với input không hợp lệ
 * và đảm bảo rằng hệ thống trả về thông báo lỗi phù hợp.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class FitnessValidateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // test case invalid fitness function
    @ParameterizedTest
    @CsvSource({
        "++u1 + u2",
        "u1 + u10",
        "((u1+ u3",
        "u1 / 0",
        "u12 + u2",
        "u1 + (u2 * 3",
        "code qua du",
        "log()",
        "u1 + u2 + @@@"
    })
    void testInvalidFitnessFunctions(String function) throws Exception {
        GameTheoryProblemDto dto = setUpTestCase();
        dto.setFitnessFunction(function);

        mockMvc.perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // test for valid fitness function
    @ParameterizedTest
    @CsvSource({
        "PRODUCT",
        "MAX",
        "MIN",
        "AVERAGE",
        "MEDIAN",
        "RANGE",
        "u1 + u2",
        "u1 * 2 + u3 / 4",
        "log(u1) + sqrt(u2)",
        "abs(u1 - u2)"
    })
    void testValidFitnessFunctions(String function) throws Exception {
        GameTheoryProblemDto dto = setUpTestCase();
        dto.setFitnessFunction(function);

        List<NormalPlayer> players = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            players.add(getNormalPlayers().get(0));
        }
        dto.setNormalPlayers(players);

        mockMvc.perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andDo(print());
    }


    private GameTheoryProblemDto setUpTestCase() {
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
        player.setPayoffFunction("SUM");

        final List<NormalPlayer> players = new ArrayList<NormalPlayer>(3);
        players.add(player);
        players.add(player);
        players.add(player);
        return players;
    }
}