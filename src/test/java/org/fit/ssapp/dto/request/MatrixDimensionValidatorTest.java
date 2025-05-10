package org.fit.ssapp.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class MatrixDimensionValidatorTest {

    @Autowired
    private Validator validator;

    @ParameterizedTest
    @MethodSource("provideValidMatrixInputs")
    void testValidMatrixDimensions(StableMatchingProblemDto dto) {
        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            violations.forEach(v -> System.out.println("Violation: " + v.getMessage()));
        }
        assertTrue(violations.isEmpty(), "Expected no violations for valid matrix dimensions");
    }

    @ParameterizedTest
    @MethodSource("provideInvalidMatrixInputs")
    void testInvalidMatrixDimensions(StableMatchingProblemDto dto, String[] expectedMessages) {
        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        if (violations.size() != expectedMessages.length) {
            violations.forEach(v -> System.out.println("Violation: " + v.getMessage()));
        }

        assertEquals(expectedMessages.length, violations.size(),
                "Expected " + expectedMessages.length + " violations, but found " + violations.size());

        String[] actualMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toArray(String[]::new);
        Arrays.sort(expectedMessages);

        assertTrue(Arrays.equals(expectedMessages, actualMessages),
                "Expected messages: " + Arrays.toString(expectedMessages) +
                        ", but got: " + Arrays.toString(actualMessages));
    }

    static Stream<Arguments> provideValidMatrixInputs() {
        return Stream.of(
                // Case 1: 3 individuals, 2 properties
                Arguments.of(createDto(
                        new String[][]{{"1", "2"}, {"3", "4"}, {"5", "6"}},
                        new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}},
                        new double[][]{{7.0, 8.0}, {9.0, 10.0}, {11.0, 12.0}},
                        new int[]{0, 1, 1},
                        2
                )),

                // Case 2: 4 individuals, 3 properties
                Arguments.of(createDto(
                        new String[][]{{"1", "2", "3"}, {"4", "5", "6"}, {"7", "8", "9"}, {"10", "11", "12"}},
                        new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}, {10.0, 11.0, 12.0}},
                        new double[][]{{13.0, 14.0, 15.0}, {16.0, 17.0, 18.0}, {19.0, 20.0, 21.0}, {22.0, 23.0, 24.0}},
                        new int[]{0, 1, 1, 0},
                        3
                )),

                // Case 3: 5 individuals, 2 properties
                Arguments.of(createDto(
                        new String[][]{{"1", "2"}, {"3", "4"}, {"5", "6"}, {"7", "8"}, {"9", "10"}},
                        new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}, {7.0, 8.0}, {9.0, 10.0}},
                        new double[][]{{11.0, 12.0}, {13.0, 14.0}, {15.0, 16.0}, {17.0, 18.0}, {19.0, 20.0}},
                        new int[]{0, 1, 1, 0, 1},
                        2
                )),

                // Case 4: 3 individuals, 1 property
                Arguments.of(createDto(
                        new String[][]{{"1"}, {"2"}, {"3"}},
                        new double[][]{{1.0}, {2.0}, {3.0}},
                        new double[][]{{4.0}, {5.0}, {6.0}},
                        new int[]{0, 1, 1},
                        1
                ))
        );
    }

    static Stream<Arguments> provideInvalidMatrixInputs() {
        return Stream.of(
                // Case 1: Wrong number of columns in individualRequirements (middle row)
                Arguments.of(
                        createDto(
                                new String[][]{{"1", "2"}, {"3"}, {"5", "6"}}, // Row 2 has 1 column
                                new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}},
                                new double[][]{{7.0, 8.0}, {9.0, 10.0}, {11.0, 12.0}},
                                new int[]{0, 1, 1},
                                2
                        ),
                        new String[]{
                                "Row 2 in 'individualRequirements' has 1 columns, but expected 2 properties",
                                "individualRequirements[1].length must be 2"
                        }
                ),

                // Case 2: Wrong number of columns in individualWeights (last row)
                Arguments.of(
                        createDto(
                                new String[][]{{"1", "2"}, {"3", "4"}, {"5", "6"}},
                                new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0}}, // Row 3 has 1 column
                                new double[][]{{7.0, 8.0}, {9.0, 10.0}, {11.0, 12.0}},
                                new int[]{0, 1, 1},
                                2
                        ),
                        new String[]{
                                "Row 3 in 'individualWeights' has 1 columns, but expected 2 properties",
                                "individualWeights[2].length must be 2"
                        }
                ),

                // Case 3: Wrong number of columns in individualProperties (first and middle rows)
                Arguments.of(
                        createDto(
                                new String[][]{{"1", "2"}, {"3", "4"}, {"5", "6"}},
                                new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}},
                                new double[][]{{7.0}, {9.0}, {11.0, 12.0}}, // Rows 1 and 2 have 1 column
                                new int[]{0, 1, 1},
                                2
                        ),
                        new String[]{
                                "Row 1 in 'individualProperties' has 1 columns, but expected 2 properties",
                                "Row 2 in 'individualProperties' has 1 columns, but expected 2 properties",
                                "individualProperties[0].length must be 2",
                                "individualProperties[1].length must be 2"
                        }
                ),

                // Case 4: Wrong number of columns in individualRequirements and individualWeights
                Arguments.of(
                        createDto(
                                new String[][]{{"1"}, {"3", "4"}, {"5", "6"}}, // Row 1 has 1 column
                                new double[][]{{1.0}, {3.0, 4.0}, {5.0, 6.0}}, // Row 1 has 1 column
                                new double[][]{{7.0, 8.0}, {9.0, 10.0}, {11.0, 12.0}},
                                new int[]{0, 1, 1},
                                2
                        ),
                        new String[]{
                                "Row 1 in 'individualRequirements' has 1 columns, but expected 2 properties",
                                "Row 1 in 'individualWeights' has 1 columns, but expected 2 properties",
                                "individualRequirements[0].length must be 2",
                                "individualWeights[0].length must be 2"
                        }
                ),

                // Case 5: Wrong number of columns in all matrices (distributed errors)
                Arguments.of(
                        createDto(
                                new String[][]{{"1"}, {"3", "4"}, {"5"}}, // Rows 1 and 3 have 1 column
                                new double[][]{{1.0}, {3.0, 4.0}, {5.0}}, // Rows 1 and 3 have 1 column
                                new double[][]{{7.0}, {9.0, 10.0}, {11.0}}, // Rows 1 and 3 have 1 column
                                new int[]{0, 1, 1},
                                2
                        ),
                        new String[]{
                                "Row 1 in 'individualRequirements' has 1 columns, but expected 2 properties",
                                "Row 3 in 'individualRequirements' has 1 columns, but expected 2 properties",
                                "Row 1 in 'individualWeights' has 1 columns, but expected 2 properties",
                                "Row 3 in 'individualWeights' has 1 columns, but expected 2 properties",
                                "Row 1 in 'individualProperties' has 1 columns, but expected 2 properties",
                                "Row 3 in 'individualProperties' has 1 columns, but expected 2 properties",
                                "individualRequirements[0].length must be 2",
                                "individualRequirements[2].length must be 2",
                                "individualWeights[0].length must be 2",
                                "individualWeights[2].length must be 2",
                                "individualProperties[0].length must be 2",
                                "individualProperties[2].length must be 2"
                        }
                )
        );
    }

    private static StableMatchingProblemDto createDto(String[][] individualRequirements, double[][] individualWeights, double[][] individualProperties, int[] individualSetIndices, int numberOfProperty) {
        int[] capacities = new int[individualSetIndices.length];
        Arrays.fill(capacities, 1); // Valid capacities
        return StableMatchingProblemDto.builder()
                .problemName("Test Matrix Dimension")
                .numberOfSets(2)
                .numberOfProperty(numberOfProperty)
                .individualSetIndices(individualSetIndices)
                .individualCapacities(capacities)
                .individualRequirements(individualRequirements)
                .individualWeights(individualWeights)
                .individualProperties(individualProperties)
                .evaluateFunctions(new String[]{"default", "default"})
                .fitnessFunction("default")
                .excludedPairs(null)
                .populationSize(100)
                .generation(50)
                .numberOfIndividuals(individualSetIndices.length)
                .maxTime(5000)
                .algorithm("SMT")
                .distributedCores("all")
                .build();
    }
}