package com.example.SS2_Backend.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class StableMatchingProblemDTOTest {

    private StableMatchingProblemDTO dto;
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();


    }

    @Test
    void testValidDTO() {
        dto = new StableMatchingProblemDTO(
            "Stable Matching Problem",
            2,
            3,
            3,
            new int[]{1, 2, 1},
            new int[]{1, 2, 1},
            new String[][]{{"1", "1.1", "1--"}, {"1++", "1.1", "1.1"}, {"1", "1", "2"}},
            new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}},
            new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}},
            new String[]{"10*(P1*W1) + 5*(P2*W2) + (P6*W6) + (P7*W7))", "(sqrt(P8*W8)) + 2*(P9*W9…(e))*W10) + 5*(P11*W11)"},
            "default",
            new int[][]{{1, 2}, {2, 3}},
            500,
            50,
            3600,
            "Genetic Algorithm",
            "4"
        );

        Set<ConstraintViolation<StableMatchingProblemDTO>> violations = validator.validate(dto);
        violations.forEach(violation -> System.out.println(violation.getMessage()));
    }

}