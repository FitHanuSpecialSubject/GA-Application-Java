package org.fit.ssapp.service.gt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.containsString;

import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for payoff function validation
 * 
 * validate the correctness of payoff functions in game theory problems.
 * Payoff function uses p1, p2, P1p1, P2p2...
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PayoffValidateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * test invalid payoff functions
     */
    @ParameterizedTest
    @CsvSource({
        "++p1 + p2",
        "p1 + p8",
        "cos()",
        "p1 ++ p2",
        "p1 + ((p2 * 3",
        "INVALID_FUNCTION(p1)",
        "p0 + p1",
        "p2 @ p1 + @@@"
    })
    void testInvalidPayoffFunctions(String function) throws Exception {
        GameTheoryProblemDto dto = setUpTestCase();
        dto.setDefaultPayoffFunction(function);

        mockMvc.perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }


    @ParameterizedTest
    @CsvSource({
        "pow(p1)",
        "log(p1, p2)",
        "sqrt(p1, p2, p3)",
        "cos()"
    })
    void testInvalidFunctionParameters(String function) throws Exception {
        GameTheoryProblemDto dto = setUpTestCase();
        dto.setDefaultPayoffFunction(function);

        mockMvc.perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }


    @ParameterizedTest
    @CsvSource({
        "SUM",
        "AVERAGE",
        "MIN",
        "MAX",
        "PRODUCT",
        "MEDIAN",
        "RANGE"
    })
    void testValidAggregationFunctions(String function) throws Exception {
        GameTheoryProblemDto dto = setUpTestCase();
        dto.setDefaultPayoffFunction(function);
        
        mockMvc.perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    void testIndividualPlayerPayoffFunctionValidation() throws Exception {
    GameTheoryProblemDto dto = setUpTestCase();
    NormalPlayer playerWithInvalidPayoff = dto.getNormalPlayers().get(0);
    playerWithInvalidPayoff.setPayoffFunction("p1 / 0"); 

    List<NormalPlayer> players = dto.getNormalPlayers();
    players.set(0, playerWithInvalidPayoff);
    dto.setNormalPlayers(players);
    mockMvc.perform(post("/api/game-theory-solver")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message", containsString("Invalid payoff function")));
    }

   // test invalid payoff function with a valid function
   @Test
   void invalidCustomPayoff() throws Exception {
    GameTheoryProblemDto dto = setUpTestCase();
    NormalPlayer playerWithInvalidPayoff = dto.getNormalPlayers().get(0);
    playerWithInvalidPayoff.setPayoffFunction("p1 + p8");
    List<NormalPlayer> players = dto.getNormalPlayers();
    players.set(0, playerWithInvalidPayoff);
    dto.setNormalPlayers(players);
    
    mockMvc.perform(post("/api/game-theory-solver")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message", containsString("exceeds available properties")));
   }

    // test invalid payoff function with a player reference that does not exist
    @Test
    void testInvalidPlayerReferencePayoffFunction() throws Exception {
    GameTheoryProblemDto dto = setUpTestCase();
    NormalPlayer playerWithInvalidPayoff = dto.getNormalPlayers().get(0);
    playerWithInvalidPayoff.setPayoffFunction("P4p1 + p2");
    List<NormalPlayer> players = dto.getNormalPlayers();
    players.set(0, playerWithInvalidPayoff);
    dto.setNormalPlayers(players);
    
    mockMvc.perform(post("/api/game-theory-solver")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message", containsString("exceeds available players")));
    }

    @Test
    void testPlayerReferencingPayoffFunctions() throws Exception {
    GameTheoryProblemDto dto = setUpTestCase();
    List<NormalPlayer> players = dto.getNormalPlayers();
    players.get(0).setPayoffFunction("P2p1 + P3p2");
    players.get(1).setPayoffFunction("p1 * P1p2");
    players.get(2).setPayoffFunction("MAX");
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
    
