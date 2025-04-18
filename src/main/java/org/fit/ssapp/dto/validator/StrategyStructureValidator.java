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
        if (players == null || players.size() < 2) return true; // nothing to compare

        boolean isValid = true;

        // Get the standard number of strategies from the first player
        int expectedStrategyCount = players.get(0).getStrategies() != null
                ? players.get(0).getStrategies().size()
                : 0;

        // Get the standard number of properties from the first strategy of the first player
        int expectedPropertyCount = players.get(0).getStrategies() != null &&
                !players.get(0).getStrategies().isEmpty() &&
                players.get(0).getStrategies().get(0).getProperties() != null
                ? players.get(0).getStrategies().get(0).getProperties().size()
                : 0;

        for (int i = 0; i < players.size(); i++) {
            NormalPlayer player = players.get(i);
            List<Strategy> strategies = player.getStrategies();

            // Validate strategy count
            if (strategies == null || strategies.size() != expectedStrategyCount) {
                addViolation(context,
                        String.format("normalPlayers[%d].strategies", i),
                        String.format("Expected %d strategies but found %d",
                                expectedStrategyCount, strategies == null ? 0 : strategies.size()));
                isValid = false;
                continue;
            }

            for (int j = 0; j < strategies.size(); j++) {
                Strategy strategy = strategies.get(j);
                List<Double> properties = strategy.getProperties();

                if (properties == null || properties.size() != expectedPropertyCount) {
                    addViolation(context,
                            String.format("normalPlayers[%d].strategies[%d].properties", i, j),
                            String.format("Expected %d properties but found %d",
                                    expectedPropertyCount, properties == null ? 0 : properties.size()));
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("• field: \"" + field + "\", message: \"" + message + "\"")
                .addConstraintViolation();
    }
}

