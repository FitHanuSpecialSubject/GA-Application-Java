package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.fit.ssapp.config.ValidationConfig;
import org.fit.ssapp.constants.MessageConst;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.springframework.beans.factory.annotation.Autowired;

public class StableMatchingValidator implements ConstraintValidator<ValidStableMatching, StableMatchingProblemDto> {

    @Autowired
    private ValidationConfig validationConfig;

    @Override
    public boolean isValid(StableMatchingProblemDto dto, ConstraintValidatorContext context) {
        boolean isValid = true;
        int threshold = validationConfig.getThreshold();
        int threshold = validationConfig.getThreshold();

        // if (dto.getPopulationSize() > validationConfig.getMaxPopulation()) {
        //     context.disableDefaultConstraintViolation();
        //     context.buildConstraintViolationWithTemplate("Population size must not exceed " + validationConfig.getMaxPopulation())
        //             .addPropertyNode("populationSize")
        //             .addConstraintViolation();
        //     isValid = false;
        // }

        // if (dto.getGeneration() > validationConfig.getMaxGeneration()) {
        //     context.disableDefaultConstraintViolation();
        //     context.buildConstraintViolationWithTemplate("Generation must not exceed " + validationConfig.getMaxGeneration())
        //             .addPropertyNode("generation")
        //             .addConstraintViolation();
        //     isValid = false;
        // }

        if (dto.getPopulationSize() * dto.getGeneration() > threshold) {
        // if (dto.getPopulationSize() > validationConfig.getMaxPopulation()) {
        //     context.disableDefaultConstraintViolation();
        //     context.buildConstraintViolationWithTemplate("Population size must not exceed " + validationConfig.getMaxPopulation())
        //             .addPropertyNode("populationSize")
        //             .addConstraintViolation();
        //     isValid = false;
        // }

        // if (dto.getGeneration() > validationConfig.getMaxGeneration()) {
        //     context.disableDefaultConstraintViolation();
        //     context.buildConstraintViolationWithTemplate("Generation must not exceed " + validationConfig.getMaxGeneration())
        //             .addPropertyNode("generation")
        //             .addConstraintViolation();
        //     isValid = false;
        // }

        if (dto.getPopulationSize() * dto.getGeneration() > threshold) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Threshold must not exceed " + threshold)
                    .addPropertyNode("generation")
                    .addPropertyNode("populationSize")
                    .addConstraintViolation();
            isValid = false;
        }

        if (dto.getNumberOfIndividuals() < validationConfig.getMinIndividuals()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Number of individuals must be at least " + validationConfig.getMinIndividuals())
                    .addPropertyNode("numberOfIndividuals")
                    .addConstraintViolation();
            isValid = false;
        }

        if (dto.getPopulationSize() < 1) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(MessageConst.ErrMessage.POPULATION_SIZE)
                    .addPropertyNode("populationSize")
                    .addConstraintViolation();
            isValid = false;
        }

        if (dto.getGeneration() < 1) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(MessageConst.ErrMessage.GENERATION)
                    .addPropertyNode("generation")
                    .addConstraintViolation();
            isValid = false;
        }

        if (dto.getNumberOfIndividuals() < 3) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(MessageConst.ErrMessage.MES_002)
                    .addPropertyNode("numberOfIndividuals")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
