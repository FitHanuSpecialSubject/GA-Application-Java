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
public class SmtRequirementSyntaxTest {

    @Autowired
    private Validator validator;

    private StableMatchingProblemDto createBaseCaseDto(String algorithm, String[][] individualRequirements) {
        return StableMatchingProblemDto.builder()
                .problemName("Test Base Case")
                .numberOfSets(2)
                .numberOfProperty(2)
                .individualSetIndices(new int[]{0, 1, 1})
                .individualCapacities(new int[]{1, 1, 1})
                .individualRequirements(individualRequirements)
                .individualWeights(new double[][]{{1.0, 2.0}, {3.0, 4.0}, {3.0, 4.0}})
                .individualProperties(new double[][]{{5.0, 6.0}, {7.0, 8.0}, {7.0, 8.0}})
                .evaluateFunctions(new String[]{"default", "default"})
                .fitnessFunction("default")
                .excludedPairs(null)
                .populationSize(100)
                .generation(50)
                .numberOfIndividuals(3)
                .maxTime(5000)
                .algorithm(algorithm)
                .distributedCores("all")
                .build();
    }

    @ParameterizedTest
    @MethodSource("provideValidRequirementInputs")
    void testValidRequirementSyntax(StableMatchingProblemDto dto) {
        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Expected no violations for valid requirements");
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRequirementInputs")
    void testInvalidRequirementSyntax(StableMatchingProblemDto dto, String[] expectedMessages) {
        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);

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

    static Stream<Arguments> provideValidRequirementInputs() {
        return Stream.of(
                Arguments.of(createDto(new String[][]{{"1", "2"}, {"3", "4"}, {"5", "6"}})),
                Arguments.of(createDto(new String[][]{{"1:2", "2:3"}, {"3:4", "4:5"}, {"5:6", "6:7"}})),
                Arguments.of(createDto(new String[][]{{"1++", "2--"}, {"3++", "4--"}, {"5++", "6--"}})),
                Arguments.of(createDto(new String[][]{{"1:2", "2++"}, {"3", "4:5"}, {"5--", "6"}})),
                Arguments.of(createDto(new String[][]{{"1.5", "2.5"}, {"3.5:4.5", "4.5"}, {"5.5", "6.5++"}}))
        );
    }

    static Stream<Arguments> provideInvalidRequirementInputs() {
        return Stream.of(
                Arguments.of(
                        createDto(new String[][]{{"", "2:3"}, {"2++", "1:2"}, {"2++", "1--"}}),
                        new String[]{"Requirement cannot be empty at row 0, column 0: ''"}
                ),
                Arguments.of(
                        createDto(new String[][]{{"1:2", "2++"}, {"3:2", "4:5"}, {"5--", "6"}}),
                        new String[]{"Invalid range logic: left bound is greater than right bound at row 1, column 0: '3:2'"}
                ),
                Arguments.of(
                        createDto(new String[][]{{"1:2", "2++"}, {"3", "4:5"}, {"5-+!", "6"}}),
                        new String[]{"Invalid individual requirement syntax - Invalid syntax at row 2, column 0: '5-+!'"}
                ),
                Arguments.of(
                        createDto(new String[][]{{"abc", "2++"}, {"3", "xyz:5"}, {"5--", "6"}}),
                        new String[]{
                                "Requirement must start with a valid number at row 0, column 0: 'abc'",
                                "Requirement must start with a valid number at row 1, column 1: 'xyz:5'"
                        }
                ),
                Arguments.of(
                        createDto(new String[][]{{"", "2:1"}, {"abc", "4:5"}, {"5:6", "++"}}),
                        new String[]{
                                "Requirement cannot be empty at row 0, column 0: ''",
                                "Invalid range logic: left bound is greater than right bound at row 0, column 1: '2:1'",
                                "Requirement must start with a valid number at row 1, column 0: 'abc'",
                                "Requirement must start with a valid number at row 2, column 1: '++'"
                        }
                )
        );
    }

    private static StableMatchingProblemDto createDto(String[][] individualRequirements) {
        return StableMatchingProblemDto.builder()
                .problemName("Test Base Case")
                .numberOfSets(2)
                .numberOfProperty(2)
                .individualSetIndices(new int[]{0, 1, 1})
                .individualCapacities(new int[]{1, 1, 1})
                .individualRequirements(individualRequirements)
                .individualWeights(new double[][]{{1.0, 2.0}, {3.0, 4.0}, {3.0, 4.0}})
                .individualProperties(new double[][]{{5.0, 6.0}, {7.0, 8.0}, {7.0, 8.0}})
                .evaluateFunctions(new String[]{"default", "default"})
                .fitnessFunction("default")
                .excludedPairs(null)
                .populationSize(100)
                .generation(50)
                .numberOfIndividuals(3)
                .maxTime(5000)
                .algorithm("SMT")
                .distributedCores("all")
                .build();
    }
}