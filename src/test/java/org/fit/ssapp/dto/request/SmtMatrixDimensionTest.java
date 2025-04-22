package org.fit.ssapp.dto.request;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

public class SmtMatrixDimensionTest {

    private final Validator validator;

    public SmtMatrixDimensionTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Utility method to create a valid DTO
    private StableMatchingProblemDto createValidDto() {
        return new StableMatchingProblemDto(
                "Valid Problem",
                3, // numberOfSets
                2, // numberOfProperty
                new int[]{1, 2, 3}, // individualSetIndices (3 elements)
                new int[]{10, 10, 10}, // individualCapacities
                new String[][]{{"a", "b"}, {"c", "d"}, {"e", "f"}}, // requirements 3x2
                new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}}, // weights 3x2
                new double[][]{{0.1, 0.2}, {0.3, 0.4}, {0.5, 0.6}}, // properties 3x2
                new String[]{"func1", "func2"},
                "fitnessFunc",
                null,
                100,
                50,
                3,
                60,
                "GA",
                "1,2,3"
        );
    }

    // Test Case 1: Valid Input Test
    @Test
    void whenAllMatricesHaveCorrectDimensions_shouldPassValidation() {
        StableMatchingProblemDto dto = createValidDto();

        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    // Test Case 2: Invalid Row Count Test (5 variations)
    @ParameterizedTest
    @MethodSource("provideInvalidRowCountCases")
    void whenMatrixHasWrongRowCount_shouldFailValidation(
            String[][] requirements, double[][] weights, double[][] properties, int expectedErrors) {

        StableMatchingProblemDto dto = createValidDto();
        dto.setIndividualRequirements(requirements);
        dto.setIndividualWeights(weights);
        dto.setIndividualProperties(properties);

        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(expectedErrors);

        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        assertThat(messages).anyMatch(msg -> msg.contains("rows, but expected"));
    }

    private static Stream<Arguments> provideInvalidRowCountCases() {
        return Stream.of(
                // requirements invalid, others valid
                Arguments.of(
                        new String[][]{{"a", "b"}, {"c", "d"}}, // 2 rows
                        new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}},
                        new double[][]{{0.1, 0.2}, {0.3, 0.4}, {0.5, 0.6}},
                        1
                ),
                // weights invalid, others valid
                Arguments.of(
                        new String[][]{{"a", "b"}, {"c", "d"}, {"e", "f"}},
                        new double[][]{{1.0, 2.0}, {3.0, 4.0}}, // 2 rows
                        new double[][]{{0.1, 0.2}, {0.3, 0.4}, {0.5, 0.6}},
                        1
                ),
                // properties invalid, others valid
                Arguments.of(
                        new String[][]{{"a", "b"}, {"c", "d"}, {"e", "f"}},
                        new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}},
                        new double[][]{{0.1, 0.2}, {0.3, 0.4}}, // 2 rows
                        1
                ),
                // all matrices invalid
                Arguments.of(
                        new String[][]{{"a", "b"}}, // 1 row
                        new double[][]{{1.0, 2.0}}, // 1 row
                        new double[][]{{0.1, 0.2}}, // 1 row
                        3
                ),
                // requirements and weights invalid
                Arguments.of(
                        new String[][]{{"a", "b"}, {"c", "d"}}, // 2 rows
                        new double[][]{{1.0, 2.0}}, // 1 row
                        new double[][]{{0.1, 0.2}, {0.3, 0.4}, {0.5, 0.6}},
                        2
                )
        );
    }

    // Test Case 3: Invalid Column Count Test (5 variations)
    @ParameterizedTest
    @MethodSource("provideInvalidColumnCountCases")
    void whenMatrixHasWrongColumnCount_shouldFailValidation(
            String[][] requirements, double[][] weights, double[][] properties, int expectedErrors) {

        StableMatchingProblemDto dto = createValidDto();
        dto.setIndividualRequirements(requirements);
        dto.setIndividualWeights(weights);
        dto.setIndividualProperties(properties);

        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(expectedErrors);

        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        assertThat(messages).anyMatch(msg -> msg.contains("columns, but expected"));
    }

    private static Stream<Arguments> provideInvalidColumnCountCases() {
        return Stream.of(
                // requirements has one row with wrong columns
                Arguments.of(
                        new String[][]{{"a"}, {"c", "d"}, {"e", "f"}}, // row 0 has 1 column
                        new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}},
                        new double[][]{{0.1, 0.2}, {0.3, 0.4}, {0.5, 0.6}},
                        1
                ),
                // weights has middle row with wrong columns
                Arguments.of(
                        new String[][]{{"a", "b"}, {"c", "d"}, {"e", "f"}},
                        new double[][]{{1.0, 2.0}, {3.0}, {5.0, 6.0}}, // row 1 has 1 column
                        new double[][]{{0.1, 0.2}, {0.3, 0.4}, {0.5, 0.6}},
                        1
                ),
                // properties has last row with wrong columns
                Arguments.of(
                        new String[][]{{"a", "b"}, {"c", "d"}, {"e", "f"}},
                        new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}},
                        new double[][]{{0.1, 0.2}, {0.3, 0.4}, {0.5}}, // row 2 has 1 column
                        1
                ),
                // all matrices have some rows with wrong columns
                Arguments.of(
                        new String[][]{{"a"}, {"c", "d"}, {"e"}}, // rows 0,2 have 1 column
                        new double[][]{{1.0}, {3.0, 4.0}, {5.0, 6.0}}, // row 0 has 1 column
                        new double[][]{{0.1, 0.2}, {0.3}, {0.5, 0.6}}, // row 1 has 1 column
                        3
                ),
                // requirements and properties have wrong columns
                Arguments.of(
                        new String[][]{{"a", "b", "c"}, {"d", "e"}, {"f", "g"}}, // row 0 has 3 columns
                        new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}},
                        new double[][]{{0.1}, {0.3, 0.4}, {0.5}}, // rows 0,2 have 1 column
                        2
                )
        );
    }

    // Test Case 4: Mixed Row and Column Errors Test (5 variations)
    @ParameterizedTest
    @MethodSource("provideMixedErrorCases")
    void whenMatricesHaveMixedRowAndColumnErrors_shouldFailValidationWithMultipleMessages(
            String[][] requirements, double[][] weights, double[][] properties, int expectedErrors) {

        StableMatchingProblemDto dto = createValidDto();
        dto.setIndividualRequirements(requirements);
        dto.setIndividualWeights(weights);
        dto.setIndividualProperties(properties);

        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(expectedErrors);

        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        assertThat(messages).anyMatch(msg -> msg.contains("rows"));
        assertThat(messages).anyMatch(msg -> msg.contains("columns"));
    }

    private static Stream<Arguments> provideMixedErrorCases() {
        return Stream.of(
                // requirements has wrong rows, weights has wrong columns
                Arguments.of(
                        new String[][]{{"a", "b"}, {"c", "d"}}, // 2 rows
                        new double[][]{{1.0}, {3.0, 4.0}, {5.0, 6.0}}, // row 0 has 1 column
                        new double[][]{{0.1, 0.2}, {0.3, 0.4}, {0.5, 0.6}},
                        2
                ),
                // weights has wrong rows, properties has wrong columns
                Arguments.of(
                        new String[][]{{"a", "b"}, {"c", "d"}, {"e", "f"}},
                        new double[][]{{1.0, 2.0}, {3.0, 4.0}}, // 2 rows
                        new double[][]{{0.1}, {0.3, 0.4}, {0.5}}, // rows 0,2 have 1 column
                        2
                ),
                // all matrices have mixed errors
                Arguments.of(
                        new String[][]{{"a", "b", "c"}, {"d", "e"}}, // row 0 has 3 cols, only 2 rows
                        new double[][]{{1.0}, {3.0, 4.0}, {5.0}}, // rows 0,2 have 1 column
                        new double[][]{{0.1, 0.2}, {0.3}}, // only 2 rows, row 1 has 1 column
                        4
                ),
                // requirements has wrong rows and columns
                Arguments.of(
                        new String[][]{{"a", "b", "c"}, {"d", "e"}}, // row 0 has 3 cols, only 2 rows
                        new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}},
                        new double[][]{{0.1, 0.2}, {0.3, 0.4}, {0.5, 0.6}},
                        2
                ),
                // weights and properties have mixed errors
                Arguments.of(
                        new String[][]{{"a", "b"}, {"c", "d"}, {"e", "f"}},
                        new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0}}, // row 0 has 3 cols, only 2 rows
                        new double[][]{{0.1}, {0.3, 0.4}, {0.5}}, // rows 0,2 have 1 column
                        3
                )
        );
    }
}
