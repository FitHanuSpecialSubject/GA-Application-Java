package org.fit.ssapp.service.gt;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates the correctness of fitness functions in game theory problems.
 * Fitness function uses u1, u2, u3... for utility values of different players.
 */
@SpringBootTest
@ActiveProfiles("test")
public class FitnessValidateTest {

    @Autowired
    private Validator validator;

    @ParameterizedTest
    @MethodSource("validFitnessFunctionProvider")
    void testValidFitnessFunctions(GameTheoryProblemDto dto) {
        Set<ConstraintViolation<GameTheoryProblemDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            System.out.println("Violations found for fitness function: " + dto.getFitnessFunction());
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
        }
        assertTrue(violations.isEmpty(), "Expected no violations for valid fitness functions");
    }

    @ParameterizedTest
    @MethodSource("invalidFitnessFunctionProvider")
    void testInvalidFitnessFunctions(GameTheoryProblemDto dto, String[] expectedMessages) {
        Set<ConstraintViolation<GameTheoryProblemDto>> violations = validator.validate(dto);
        if (violations.size() != expectedMessages.length) {
            System.out.println("Testing fitness function: " + dto.getFitnessFunction());
            System.out.println("Expected messages: " + Arrays.toString(expectedMessages));
            System.out.println("Actual violations:");
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
        }

        assertEquals(expectedMessages.length, violations.size(),
                "Expected " + expectedMessages.length + " violations, but found " + violations.size());

        String[] actualMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toArray(String[]::new);
        Arrays.sort(expectedMessages);

        assertTrue(Arrays.equals(expectedMessages, actualMessages),
                "Expected messages: " + Arrays.toString(expectedMessages) +
                        ", but got: " + Arrays.toString(actualMessages));
    }

    static Stream<Arguments> validFitnessFunctionProvider() {
        return Stream.of(
            // Case 1: Default and special functions
            Arguments.of(createDto("PRODUCT")),
            Arguments.of(createDto("MAX")),
            Arguments.of(createDto("MIN")),
            Arguments.of(createDto("AVERAGE")),
            Arguments.of(createDto("MEDIAN")),
            Arguments.of(createDto("RANGE")),
            
            // Case 2: Simple expressions with utilities
            Arguments.of(createDto("u1", 1)),
            Arguments.of(createDto("u1 + u2", 2)),
            Arguments.of(createDto("u1 * u2", 2)),
            
            // Case 3: Complex expressions
            Arguments.of(createDto("u1 * u2 / 2", 2)),
            Arguments.of(createDto("pow(u1, 2)", 1)),
            Arguments.of(createDto("sqrt(u1)", 1)),
            Arguments.of(createDto("u1 * 2 + u3 / 4", 3)),
            Arguments.of(createDto("log(u1) + sqrt(u2)", 2)),
            Arguments.of(createDto("abs(u1 - u2)", 2))
        );
    }

    static Stream<Arguments> invalidFitnessFunctionProvider() {
        return Stream.of(
            // Case 1: Invalid syntax
            Arguments.of(
                createDto("u1 + (u2 * 3", 2),
                new String[]{"Invalid syntax: Unclosed opening parenthesis at position 5 in 'u1 + (u2 * 3'"}
            ),
            // Test for multiple opening parentheses 
            Arguments.of(
                createDto("u1 + ((u2 * 3", 2),
                new String[]{"Invalid syntax: Unclosed opening parenthesis at position 5 in 'u1 + ((u2 * 3'",
                            "Invalid syntax: Unclosed opening parenthesis at position 6 in 'u1 + ((u2 * 3'"}
            ),

            // Case 2: Invalid utility references
            Arguments.of(
                createDto("u1 + u10", 2),
                new String[]{"Invalid fitness function: Variable u10 at position 5 refers to non-existent player. The request contains only 2 players."}
            ),
            Arguments.of(
                createDto("u12 + u2", 2),
                new String[]{"Invalid fitness function: Variable u12 at position 0 refers to non-existent player. The request contains only 2 players."}
            ),
            
            // Case 3: Invalid function parameters
            Arguments.of(
                createDto("unknownFunc(u1)", 1),
                new String[]{"Invalid function: Function 'unknownFunc' at position 0 does not exist in 'unknownFunc(u1)'"}
            ),
            Arguments.of(
                createDto("log()", 1),
                new String[]{"Invalid function syntax: Missing argument for log function at position 0 in 'log()'"}
            ),

            Arguments.of(
                createDto("u1 ++ u21 - ((u2 + u4 ", 2),
                new String[]{"Invalid syntax: Two operators in a row at position 4 in 'u1 ++ u21 - ((u2 + u4 '.",
                    "Invalid fitness function: Variables u4 and u21 refer to non-existent players. The request contains only 2 players.",
                    "Invalid syntax: Unclosed opening parenthesis at position 5 in 'u1 ++ u21 - ((u2 + u4 '"}
            ),
            
            // Case A: Invalid expressions
            Arguments.of(
                createDto("u1 + u2 + @@@", 2),
                new String[]{"Invalid character: Unrecognized character '@' at position 10 in 'u1 + u2 + @@@'"}
            ),
            
            // Case 5: Division by zero
            Arguments.of(
                createDto("u1 / 0", 1),
                new String[]{"Invalid syntax: Division by zero detected at position 3 in 'u1 / 0'"}
            )
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
