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
 * Validates the correctness of payoff functions (both default and per-player)
 * and related parameters in game theory problems.
 * Payoff function uses p1, p2... for properties and P1p1, P2p2... for player-property references.
 */
@SpringBootTest
@ActiveProfiles("test")
public class PayoffValidateTest {

    @Autowired
    private Validator validator;

    @ParameterizedTest
    @MethodSource("validPayoffFunctionsProvider")
    void testValidPayoffFunctions(GameTheoryProblemDto dto) {
        Set<ConstraintViolation<GameTheoryProblemDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            System.out.println("Violations found for payoff function: " + dto.getDefaultPayoffFunction());
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
        }
        assertTrue(violations.isEmpty(), "Expected no violations for valid payoff functions");
    }

    @ParameterizedTest
    @MethodSource("invalidPayoffFunctionsProvider")
    void testInvalidPayoffFunctions(GameTheoryProblemDto dto, String[] expectedMessages) {
        Set<ConstraintViolation<GameTheoryProblemDto>> violations = validator.validate(dto);
        if (violations.size() != expectedMessages.length) {
            System.out.println("Testing payoff function: " + dto.getDefaultPayoffFunction());
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

    static Stream<Arguments> validPayoffFunctionsProvider() {
        return Stream.of(
            // Case 1: Default and special functions
            Arguments.of(createDto("SUM")),
            Arguments.of(createDto("AVERAGE")),
            Arguments.of(createDto("MIN")),
            Arguments.of(createDto("MAX")),
            Arguments.of(createDto("PRODUCT")),
            Arguments.of(createDto("MEDIAN")),
            Arguments.of(createDto("RANGE")),

            // Case 2: Simple expressions with properties
            Arguments.of(createDto("p1")),
            Arguments.of(createDto("p2^2")),
            Arguments.of(createDto("p1 + p2")),
            Arguments.of(createDto("p1 * p2")),

            // Case 3: Complex expressions
            Arguments.of(createDto("p1 * p2 / 2")),
            Arguments.of(createDto("sqrt(p1)")),

            // Case 4: Player references
            Arguments.of(createDtoWithPlayerPayoffs("P1p1")),
            Arguments.of(createDtoWithPlayerPayoffs("P2p1 + P1p2"))
        );
    }

    static Stream<Arguments> invalidPayoffFunctionsProvider() {
        return Stream.of(
            // Case 1: Invalid syntax
            Arguments.of(
                createDto("p1 ++ p2"),
                new String[]{"Invalid syntax: Two operators in a row at position 4 in 'p1 ++ p2'."}
            ),
            Arguments.of(
                createDto("p1 + ((p2 * 3"),
                new String[]{"Invalid syntax: Unclosed opening parenthesis at position 6 in 'p1 + ((p2 * 3'"}
            ),

            // Case 2: Invalid property references
            Arguments.of(
                createDto("p1 + p8"),
                new String[]{"Invalid payoff function: Property p8 at position 5 exceeds available properties. Maximum property count is 4 (valid variables are p1 to p4)."}
            ),
            Arguments.of(
                createDto("p0 + p1"),
                new String[]{"Invalid payoff function: Property p0 at position 0 exceeds available properties. Maximum property count is 4 (valid variables are p1 to p4)."}
            ),
            Arguments.of(
                createDto("sqrt(p1, p2, p3)"),
                new String[]{"Invalid payoff function syntax: Function sqrt at position 0 requires 1 argument(s), but found 3 in 'sqrt(p1, p2, p3)'"}
            ),

            // Case 3: Multiple violations in one expression
            Arguments.of(
                createDto("p0 ++ p8 + P5p1 + (p2 / 0"),
                new String[]{"Invalid payoff function: Property p0 at position 0 exceeds available properties. Maximum property count is 4 (valid variables are p1 to p4).",
                "Invalid syntax: Two operators in a row at position 4 in 'p0 ++ p8'.",
                "Invalid syntax: Unclosed opening parenthesis at position 10 in 'P5p1 + (p2 / 0'",
                "Invalid syntax: Division by zero detected at position 10 in 'P5p1 + (p2 / 0'",
                "Invalid syntax: Unclosed opening parenthesis at position 10 in 'P5p1 + (p2 / 0'"}
            ),
            
            // Case 4: Invalid player references
            Arguments.of(
                createDtoWithPlayerPayoffs("P4p1 + p2"),
                new String[]{"Invalid payoff function: Player P4 at position 0 exceeds available players. Maximum player count is 3 (valid players are P1 to P3)"}
            ),

            // Case 5: Division by zero
            Arguments.of(
                createDtoWithPlayerPayoffs("p1 / 0"),
                new String[]{"Invalid expression: Division by zero detected at position 3 in 'p1 / 0'"}
            )
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

    private static GameTheoryProblemDto createDtoWithPlayerPayoffs(String... playerPayoffs) {
        GameTheoryProblemDto dto = setUpTestCase();
        List<NormalPlayer> players = dto.getNormalPlayers();
        for (int i = 0; i < Math.min(playerPayoffs.length, players.size()); i++) {
            if (playerPayoffs[i] != null) {
                players.get(i).setPayoffFunction(playerPayoffs[i]);
            }
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
        strat.setPayoff(payoff);

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
    
