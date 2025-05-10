package org.fit.ssapp.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class StrategyStructureValidatorTest {

    @Autowired
    private Validator validator;

    private GameTheoryProblemDto setUpTestCase(List<NormalPlayer> players) {
        GameTheoryProblemDto dto = new GameTheoryProblemDto();
        dto.setSpecialPlayer(null);
        dto.setNormalPlayers(players);
        dto.setFitnessFunction("SUM");
        dto.setDefaultPayoffFunction("SUM");
        dto.setMaximizing(true);
        dto.setDistributedCores("all");
        dto.setMaxTime(1000);
        dto.setGeneration(50);
        dto.setPopulationSize(50);
        dto.setAlgorithm("GT");
        return dto;
    }

    private static NormalPlayer createPlayer(String name, List<Strategy> strategies) {
        NormalPlayer player = new NormalPlayer();
        player.setName(name);
        player.setStrategies(strategies);
        player.setPayoffFunction("SUM");
        player.setPayoffValues(Arrays.asList(BigDecimal.ZERO, BigDecimal.ZERO));
        player.setPayoff(null);
        player.setPrevStrategyIndex(-1);
        return player;
    }

    private static Strategy createStrategy(String name, List<Double> properties) {
        Strategy strategy = new Strategy();
        strategy.setName(name);
        strategy.setProperties(properties != null ? properties : new ArrayList<>());
        return strategy;
    }

    @ParameterizedTest
    @MethodSource("provideValidInputs")
    void testValidStrategyStructure(GameTheoryProblemDto dto, String caseName) {
        System.out.println("Testing valid case: " + caseName + ", DTO: " + dto);
        Set<ConstraintViolation<GameTheoryProblemDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            violations.forEach(v -> System.out.println("Violation in " + caseName + ": " + v.getMessage()));
        }
        assertTrue(violations.isEmpty(), "Expected no violations for valid strategy structure in " + caseName);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidInputs")
    void testInvalidStrategyStructure(GameTheoryProblemDto dto, String[] expectedMessages, String caseName) {
        System.out.println("Testing invalid case: " + caseName + ", DTO: " + dto);
        Set<ConstraintViolation<GameTheoryProblemDto>> violations = validator.validate(dto);
        if (violations.size() != expectedMessages.length) {
            violations.forEach(v -> System.out.println("Violation in " + caseName + ": " + v.getMessage()));
        }

        assertEquals(expectedMessages.length, violations.size(),
                "Expected " + expectedMessages.length + " violations, but found " + violations.size() + " in " + caseName);

        String[] actualMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toArray(String[]::new);
        Arrays.sort(expectedMessages);

        assertTrue(Arrays.equals(expectedMessages, actualMessages),
                "Expected messages in " + caseName + ": " + Arrays.toString(expectedMessages) +
                        ", but got: " + Arrays.toString(actualMessages));
    }

    static Stream<Arguments> provideValidInputs() {
        return Stream.of(
                Arguments.of(createValidDto(
                        List.of(
                                createPlayer("P1", List.of(
                                        createStrategy("S1", List.of(1.0, 2.0)),
                                        createStrategy("S2", List.of(3.0, 4.0))
                                )),
                                createPlayer("P2", List.of(
                                        createStrategy("S1", List.of(5.0, 6.0)),
                                        createStrategy("S2", List.of(7.0, 8.0))
                                )),
                                createPlayer("P3", List.of(
                                        createStrategy("S1", List.of(9.0, 10.0)),
                                        createStrategy("S2", List.of(11.0, 12.0))
                                ))
                        )
                ), "Case 1: 3 players, 2 strategies, 2 properties"),

                Arguments.of(createValidDto(
                        List.of(
                                createPlayer("P1", List.of(
                                        createStrategy("S1", List.of(1.0)),
                                        createStrategy("S2", List.of(2.0)),
                                        createStrategy("S3", List.of(3.0))
                                )),
                                createPlayer("P2", List.of(
                                        createStrategy("S1", List.of(4.0)),
                                        createStrategy("S2", List.of(5.0)),
                                        createStrategy("S3", List.of(6.0))
                                ))
                        )
                ), "Case 2: 2 players, 3 strategies, 1 property"),

                Arguments.of(createValidDto(
                        List.of(
                                createPlayer("P1", List.of(
                                        createStrategy("S1", List.of())
                                ))
                        )
                ), "Case 3: 1 player, 1 strategy, 0 properties"),

                Arguments.of(createValidDto(
                        List.of(
                                createPlayer("P1", List.of(
                                        createStrategy("S1", List.of(1.0, 2.0, 3.0)),
                                        createStrategy("S2", List.of(4.0, 5.0, 6.0))
                                )),
                                createPlayer("P2", List.of(
                                        createStrategy("S1", List.of(7.0, 8.0, 9.0)),
                                        createStrategy("S2", List.of(10.0, 11.0, 12.0))
                                )),
                                createPlayer("P3", List.of(
                                        createStrategy("S1", List.of(13.0, 14.0, 15.0)),
                                        createStrategy("S2", List.of(16.0, 17.0, 18.0))
                                )),
                                createPlayer("P4", List.of(
                                        createStrategy("S1", List.of(19.0, 20.0, 21.0)),
                                        createStrategy("S2", List.of(22.0, 23.0, 24.0))
                                ))
                        )
                ), "Case 4: 4 players, 2 strategies, 3 properties"),

                Arguments.of(createValidDto(
                        List.of(
                                createPlayer("P1", List.of()),
                                createPlayer("P2", List.of())
                        )
                ), "Case 5: Empty strategies")
        );
    }

    static Stream<Arguments> provideInvalidInputs() {
        return Stream.of(
                Arguments.of(
                        createValidDto(null),
                        new String[]{
                                "Normal players are required",
                                "Normal players list cannot be empty"
                        },
                        "Case 0: Null normalPlayers"
                ),

                Arguments.of(
                        createValidDto(List.of()),
                        new String[]{
                                "Normal players list cannot be empty",
                                "At least one normal player is required"
                        },
                        "Case 1: Empty normalPlayers"
                ),

                Arguments.of(
                        createValidDto(
                                List.of(
                                        createPlayer("P1", List.of(
                                                createStrategy("S1", List.of(1.0, 2.0)),
                                                createStrategy("S2", List.of(3.0, 4.0))
                                        )),
                                        createPlayer("P2", List.of(
                                                createStrategy("S1", List.of(5.0, 6.0))
                                        )),
                                        createPlayer("P3", List.of(
                                                createStrategy("S1", List.of(7.0, 8.0)),
                                                createStrategy("S2", List.of(9.0, 10.0))
                                        ))
                                )
                        ),
                        new String[]{
                                "At player index 1, expected 2 strategies but found 1"
                        },
                        "Case 2: Mismatched strategy count (middle player)"
                ),

                Arguments.of(
                        createValidDto(
                                List.of(
                                        createPlayer("P1", List.of(
                                                createStrategy("S1", List.of(1.0, 2.0)),
                                                createStrategy("S2", List.of(3.0))
                                        )),
                                        createPlayer("P2", List.of(
                                                createStrategy("S1", List.of(4.0, 5.0)),
                                                createStrategy("S2", List.of(6.0, 7.0))
                                        ))
                                )
                        ),
                        new String[]{
                                "At player index 0, strategy index 1, expected 2 properties but found 1"
                        },
                        "Case 3: Mismatched property count (first player, second strategy)"
                ),

                Arguments.of(
                        createValidDto(
                                List.of(
                                        createPlayer("P1", List.of(
                                                createStrategy("S1", List.of(1.0, 2.0)),
                                                createStrategy("S2", List.of(3.0, 4.0))
                                        )),
                                        createPlayer("P2", List.of(
                                                createStrategy("S1", List.of(5.0, 6.0)),
                                                createStrategy("S2", List.of(7.0, 8.0))
                                        )),
                                        createPlayer("P3", List.of(
                                                createStrategy("S1", List.of(9.0))
                                        ))
                                )
                        ),
                        new String[]{
                                "At player index 2, expected 2 strategies but found 1",
                                "At player index 2, strategy index 0, expected 2 properties but found 1"
                        },
                        "Case 4: Mismatched strategy and property count (last player)"
                ),

                Arguments.of(
                        createValidDto(
                                List.of(
                                        createPlayer("P1", List.of(
                                                createStrategy("S1", List.of(1.0, 2.0, 3.0)),
                                                createStrategy("S2", List.of(4.0, 5.0, 6.0))
                                        )),
                                        createPlayer("P2", List.of(
                                                createStrategy("S1", List.of(7.0, 8.0)),
                                                createStrategy("S2", List.of(9.0, 10.0))
                                        )),
                                        createPlayer("P3", List.of(
                                                createStrategy("S1", List.of(11.0)),
                                                createStrategy("S2", List.of(12.0))
                                        ))
                                )
                        ),
                        new String[]{
                                "At player index 1, strategy index 0, expected 3 properties but found 2",
                                "At player index 1, strategy index 1, expected 3 properties but found 2",
                                "At player index 2, strategy index 0, expected 3 properties but found 1",
                                "At player index 2, strategy index 1, expected 3 properties but found 1"
                        },
                        "Case 5: Multiple property count mismatches"
                ),

                Arguments.of(
                        createValidDto(
                                List.of(
                                        createPlayer("P1", List.of(
                                                createStrategy("S1", List.of(1.0, 2.0)),
                                                createStrategy("S2", List.of(3.0, 4.0)),
                                                createStrategy("S3", List.of(5.0, 6.0))
                                        )),
                                        createPlayer("P2", List.of(
                                                createStrategy("S1", List.of(7.0, 8.0)),
                                                createStrategy("S2", List.of(9.0, 10.0))
                                        )),
                                        createPlayer("P3", List.of(
                                                createStrategy("S1", List.of(11.0, 12.0))
                                        ))
                                )
                        ),
                        new String[]{
                                "At player index 1, expected 3 strategies but found 2",
                                "At player index 2, expected 3 strategies but found 1"
                        },
                        "Case 6: Mismatched strategy counts across all players"
                )
        );
    }

    private static GameTheoryProblemDto createValidDto(List<NormalPlayer> players) {
        return new StrategyStructureValidatorTest().setUpTestCase(players);
    }
}