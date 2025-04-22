package org.fit.ssapp.dto.request;

import jakarta.validation.*;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.SpecialPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

public class ValidateStrategyTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private GameTheoryProblemDto createValidDto(int strategyCount, int propertyCount) {
        List<Strategy> strategies = new ArrayList<>();
        for (int i = 0; i < strategyCount; i++) {
            List<Double> props = new ArrayList<>();
            for (int j = 0; j < propertyCount; j++) {
                props.add((double) j);
            }
            strategies.add(new Strategy("Strategy" + i, props, 0.0));
        }

        NormalPlayer normalPlayer = new NormalPlayer("Player1", strategies, new ArrayList<>(), -1, "payoffFunction", BigDecimal.ZERO);

        GameTheoryProblemDto dto = new GameTheoryProblemDto();
        dto.setNormalPlayers(Arrays.asList(
                new NormalPlayer(normalPlayer.getName(), new ArrayList<>(normalPlayer.getStrategies()), normalPlayer.getPayoffValues(), normalPlayer.getPrevStrategyIndex(), normalPlayer.getPayoffFunction(), normalPlayer.getPayoff()),
                new NormalPlayer(normalPlayer.getName(), new ArrayList<>(normalPlayer.getStrategies()), normalPlayer.getPayoffValues(), normalPlayer.getPrevStrategyIndex(), normalPlayer.getPayoffFunction(), normalPlayer.getPayoff()),
                new NormalPlayer(normalPlayer.getName(), new ArrayList<>(normalPlayer.getStrategies()), normalPlayer.getPayoffValues(), normalPlayer.getPrevStrategyIndex(), normalPlayer.getPayoffFunction(), normalPlayer.getPayoff())
        ));
        dto.setFitnessFunction("fitnessFunc()");
        dto.setDefaultPayoffFunction("payoffFunc()");
        dto.setAlgorithm("genetic");
        dto.setMaxTime(100);
        dto.setGeneration(50);
        dto.setPopulationSize(30);
        dto.setSpecialPlayer(new SpecialPlayer(0, new ArrayList<>(), new ArrayList<>(), 0.0));

        return dto;
    }

    @Test
    void testValidStrategiesAndProperties() {
        GameTheoryProblemDto dto = createValidDto(3, 2);
        Set<ConstraintViolation<GameTheoryProblemDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "There should be no violations for valid input");
    }

    @Test
    void testMismatchedStrategyCountAtVariousPositions() {
        GameTheoryProblemDto dto = createValidDto(3, 2);

        // 2nd player has only 2 strategies instead of 3
        List<Strategy> invalidStrategies = new ArrayList<>(dto.getNormalPlayers().get(1).getStrategies());
        invalidStrategies.remove(0); // remove one strategy
        dto.getNormalPlayers().get(1).setStrategies(invalidStrategies);

        Set<ConstraintViolation<GameTheoryProblemDto>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        ConstraintViolation<GameTheoryProblemDto> violation = violations.iterator().next();
        assertTrue(violation.getMessage().contains("expected 3 strategies but found 2"));
    }

    @Test
    void testMismatchedPropertyCountAtStartMiddleEnd() {
        GameTheoryProblemDto dto = createValidDto(2, 3);

        // Modify first player's first strategy
        dto.getNormalPlayers().get(0).getStrategies().get(0).setProperties(Arrays.asList(1.0)); // 1 property only

        // Modify second player's second strategy
        dto.getNormalPlayers().get(1).getStrategies().get(1).setProperties(Arrays.asList(1.0, 2.0)); // 2 properties

        // Modify third player's first strategy
        dto.getNormalPlayers().get(2).getStrategies().get(0).setProperties(new ArrayList<>()); // 0 properties

        Set<ConstraintViolation<GameTheoryProblemDto>> violations = validator.validate(dto);

        assertEquals(3, violations.size(), "Expected 3 property-related violations");

        long propertyCountErrors = violations.stream()
                .filter(v -> v.getMessage().contains("expected 3 properties"))
                .count();

        assertEquals(3, propertyCountErrors);
    }

    @Test
    void testAllEmptyStrategies() {
        GameTheoryProblemDto dto = createValidDto(2, 2);

        for (NormalPlayer player : dto.getNormalPlayers()) {
            player.setStrategies(new ArrayList<>());
        }

        Set<ConstraintViolation<GameTheoryProblemDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("expected 2 strategies but found 0"));
    }

    @Test
    void testMixedValidAndInvalidPlayers() {
        GameTheoryProblemDto dto = createValidDto(3, 2);

        // Middle player has 1 strategy with 1 property instead of 2
        List<Strategy> brokenStrategies = Arrays.asList(new Strategy("BrokenStrategy", Arrays.asList(1.0), 0.0));
        dto.getNormalPlayers().set(1, new NormalPlayer("ErrorPlayer", brokenStrategies, new ArrayList<>(), -1, "payoffFunction", BigDecimal.ZERO));

        Set<ConstraintViolation<GameTheoryProblemDto>> violations = validator.validate(dto);

        assertEquals(2, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("expected 3 strategies")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("expected 2 properties")));
    }
}


