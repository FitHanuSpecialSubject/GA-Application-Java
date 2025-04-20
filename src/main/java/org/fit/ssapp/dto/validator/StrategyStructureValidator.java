package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;

import java.util.List;

public class StrategyStructureValidator implements ConstraintValidator<ValidStrategyStructure, GameTheoryProblemDto> {

    @Override
    public boolean isValid(GameTheoryProblemDto dto, ConstraintValidatorContext context) {
        List<NormalPlayer> players = dto.getNormalPlayers();
        if (players == null || players.isEmpty()) return true;

        boolean isValid = true;

        int expectedStrategyCount = getExpectedStrategyCount(players);
        int expectedPropertyCount = getExpectedPropertyCount(players);

        for (int i = 0; i < players.size(); i++) {
            NormalPlayer player = players.get(i);
            List<Strategy> strategies = player.getStrategies();

            // Validate strategy count
            if (!isStrategyCountValid(strategies, expectedStrategyCount, i, context)) {
                isValid = false;
            } else {
                // Validate property count per strategy
                if (!arePropertiesValid(strategies, expectedPropertyCount, i, context)) {
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private int getExpectedStrategyCount(List<NormalPlayer> players) {
        List<Strategy> strategies = players.get(0).getStrategies();
        return strategies != null ? strategies.size() : 0;
    }

    private int getExpectedPropertyCount(List<NormalPlayer> players) {
        List<Strategy> strategies = players.get(0).getStrategies();
        if (strategies != null && !strategies.isEmpty()) {
            List<Double> properties = strategies.get(0).getProperties();
            return properties != null ? properties.size() : 0;
        }
        return 0;
    }

    private boolean isStrategyCountValid(List<Strategy> strategies, int expectedCount, int playerIndex, ConstraintValidatorContext context) {
        int actualCount = strategies != null ? strategies.size() : 0;
        if (actualCount != expectedCount) {
            addViolation(context,
                    "normalPlayers",
                    String.format("At player index %d, expected %d strategies but found %d", playerIndex, expectedCount, actualCount));
            return false;
        }
        return true;
    }

    private boolean arePropertiesValid(List<Strategy> strategies, int expectedPropertyCount, int playerIndex, ConstraintValidatorContext context) {
        boolean valid = true;
        for (int j = 0; j < strategies.size(); j++) {
            Strategy strategy = strategies.get(j);
            List<Double> properties = strategy.getProperties();
            int actualCount = properties != null ? properties.size() : 0;

            if (actualCount != expectedPropertyCount) {
                addViolation(context,
                        "normalPlayers",
                        String.format("At player index %d, strategy index %d, expected %d properties but found %d",
                                playerIndex, j, expectedPropertyCount, actualCount));
                valid = false;
            }
        }
        return valid;
    }

    private void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }

}




