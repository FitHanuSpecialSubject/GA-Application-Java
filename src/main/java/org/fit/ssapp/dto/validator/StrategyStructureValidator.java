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
        if (players == null || players.size() < 2) return true;

        boolean isValid = true;

        int expectedStrategyCount = players.get(0).getStrategies() != null
                ? players.get(0).getStrategies().size()
                : 0;

        int expectedPropertyCount = players.get(0).getStrategies() != null &&
                !players.get(0).getStrategies().isEmpty() &&
                players.get(0).getStrategies().get(0).getProperties() != null
                ? players.get(0).getStrategies().get(0).getProperties().size()
                : 0;

        for (int i = 0; i < players.size(); i++) {
            NormalPlayer player = players.get(i);
            List<Strategy> strategies = player.getStrategies();

            // Check strategy count
            if (strategies == null || strategies.size() != expectedStrategyCount) {
                addViolation(context,
                        "normalPlayers.strategies",
                        String.format("At player index %d, expected %d strategies but found %d",
                                i, expectedStrategyCount, strategies == null ? 0 : strategies.size()));
                isValid = false;
            }

            // Check property count inside each strategy (if strategies != null)
            if (strategies != null) {
                for (int j = 0; j < strategies.size(); j++) {
                    Strategy strategy = strategies.get(j);
                    List<Double> properties = strategy.getProperties();

                    if (properties == null || properties.size() != expectedPropertyCount) {
                        addViolation(context,
                                "normalPlayers.strategies.properties",
                                String.format("At player index %d, strategy index %d, expected %d properties but found %d",
                                        i, j, expectedPropertyCount, properties == null ? 0 : properties.size()));
                        isValid = false;
                    }
                }
            }
        }

        return isValid;
    }

    private void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                String.format("• field: \"%s\", message: \"%s\"", field, message)
        ).addConstraintViolation();
    }
}



