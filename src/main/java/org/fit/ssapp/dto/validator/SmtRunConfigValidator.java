package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.fit.ssapp.config.ValidationConfig;
import org.fit.ssapp.constants.MessageConst.ErrMessage;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class SmtRunConfigValidator
    implements ConstraintValidator<ValidStableMatchingConfig, StableMatchingProblemDto> {

  @Autowired
  private ValidationConfig validationConfig;

  @Override
  public boolean isValid(StableMatchingProblemDto dto, ConstraintValidatorContext context) {
    boolean isValid = true;

    if (dto.getPopulationSize() > validationConfig.getMaxPopulation()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "Population size must not exceed " + validationConfig.getMaxPopulation())
          .addPropertyNode("populationSize")
          .addConstraintViolation();
      isValid = false;
    }

    if (dto.getGeneration() > validationConfig.getMaxGeneration()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "Generation must not exceed " + validationConfig.getMaxGeneration())
          .addPropertyNode("generation")
          .addConstraintViolation();
      isValid = false;
    }

    if (dto.getNumberOfIndividuals() < validationConfig.getMinIndividualCount()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "Number of individuals must be at least "
                  + validationConfig.getMinIndividualCount())
          .addPropertyNode("numberOfIndividuals")
          .addConstraintViolation();
      isValid = false;
    }

    if (dto.getPopulationSize() < 1) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(ErrMessage.POPULATION_SIZE)
          .addPropertyNode("populationSize")
          .addConstraintViolation();
      isValid = false;
    }

    if (dto.getGeneration() < 1) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(ErrMessage.GENERATION)
          .addPropertyNode("generation")
          .addConstraintViolation();
      isValid = false;
    }

    if (dto.getNumberOfIndividuals() < 3) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(ErrMessage.MES_002)
          .addPropertyNode("numberOfIndividuals")
          .addConstraintViolation();
      isValid = false;
    }

    // Validate Run Count per Algorithm (for insight run)

    int runCount = dto.getRunCountPerAlgorithm();

      // lower bound
    int minRunCount = validationConfig.getMinRunCountPerAlgorithm();
    if (runCount < minRunCount) {
      String field = "runCountPerAlgorithm";
      addErrorToContext(context,
          field,
          StringUtils.getMsg(ErrMessage.MIN_RUN_COUNT, field, minRunCount));
      isValid = false;
    }

      //upper bound
    int maxRunCount = validationConfig.getMaxRunCountPerAlgorithm();
    if (runCount > maxRunCount) {
      String field = "runCountPerAlgorithm";
      addErrorToContext(context,
          field,
          StringUtils.getMsg(ErrMessage.MAX_RUN_COUNT, field, maxRunCount)
      );
      isValid = false;
    }

    return isValid;
  }

  /**
   * Add validation error to context
   *
   * @param ctx        ConstraintValidatorContext
   * @param fieldName  DTO field name
   * @param errMessage Error message
   */
  public void addErrorToContext(ConstraintValidatorContext ctx,
      String fieldName,
      String errMessage) {
    ctx.disableDefaultConstraintViolation();
    ctx.buildConstraintViolationWithTemplate(errMessage)
        .addPropertyNode(fieldName)
        .addConstraintViolation();
  }

}
