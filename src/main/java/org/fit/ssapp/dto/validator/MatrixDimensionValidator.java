package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;

public class MatrixDimensionValidator implements ConstraintValidator<ValidMatrixDimension, StableMatchingProblemDto> {

    @Override
    public boolean isValid(StableMatchingProblemDto dto, ConstraintValidatorContext context) {
        boolean valid = true;
        int expectedRows = dto.getIndividualSetIndices() != null ? dto.getIndividualSetIndices().length : 0;
        int expectedCols = dto.getNumberOfProperty();

        context.disableDefaultConstraintViolation();

        // Validate individualRequirements
        if (!validateMatrixSize("individualRequirements", dto.getIndividualRequirements(), expectedRows, expectedCols, context)) {
            valid = false;
        }

        // Validate individualWeights
        if (!validateMatrixSize("individualWeights", dto.getIndividualWeights(), expectedRows, expectedCols, context)) {
            valid = false;
        }

        // Validate individualProperties
        if (!validateMatrixSize("individualProperties", dto.getIndividualProperties(), expectedRows, expectedCols, context)) {
            valid = false;
        }

        return valid;
    }

    private boolean validateMatrixSize(String fieldName, String[][] matrix, int expectedRows, int expectedCols, ConstraintValidatorContext context) {
        boolean valid = true;

        if (matrix == null) return true;

        if (matrix.length != expectedRows) {
            context.buildConstraintViolationWithTemplate(
                    String.format("Matrix '%s' has %d rows, but expected %d individuals", fieldName, matrix.length, expectedRows)
            ).addPropertyNode(fieldName).addConstraintViolation();
            valid = false;
        }

        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) continue;
            if (matrix[i].length != expectedCols) {
                context.buildConstraintViolationWithTemplate(
                        String.format("Row %d in '%s' has %d columns, but expected %d properties", i + 1, fieldName, matrix[i].length, expectedCols)
                ).addPropertyNode(fieldName).addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }

    private boolean validateMatrixSize(String fieldName, double[][] matrix, int expectedRows, int expectedCols, ConstraintValidatorContext context) {
        boolean valid = true;

        if (matrix == null) return true;

        if (matrix.length != expectedRows) {
            context.buildConstraintViolationWithTemplate(
                    String.format("Matrix '%s' has %d rows, but expected %d individuals", fieldName, matrix.length, expectedRows)
            ).addPropertyNode(fieldName).addConstraintViolation();
            valid = false;
        }

        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) continue;
            if (matrix[i].length != expectedCols) {
                context.buildConstraintViolationWithTemplate(
                        String.format("Row %d in '%s' has %d columns, but expected %d properties", i + 1, fieldName, matrix[i].length, expectedCols)
                ).addPropertyNode(fieldName).addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}