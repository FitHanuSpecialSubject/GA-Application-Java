package org.fit.ssapp.service.st;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@AutoConfigureMockMvc
public class SMTValidFitnessTest {
  @Autowired
  private Validator validator;

  @ParameterizedTest
  @MethodSource("malformedFunctions")
  void invalidSyntax(StableMatchingProblemDto dto, String[] expectedMessages) throws Exception {
    Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);

    assertEquals(expectedMessages.length, violations.size(),
            "Expected " + expectedMessages.length + " violations, but found " + violations.size());

    String[] actualMessages = violations.stream()
            .map(ConstraintViolation::getMessage)
            .sorted()
            .toArray(String[]::new);
    Arrays.sort(expectedMessages);

    Assertions.assertTrue(Arrays.equals(expectedMessages, actualMessages),
            "Expected messages: " + Arrays.toString(expectedMessages) +
                    ", but got: " + Arrays.toString(actualMessages));
  }

  @ParameterizedTest
  @MethodSource("invalidSigmaFunctions")
  void invalidSigma(StableMatchingProblemDto dto, String[] expectedMessages) throws Exception {

    Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);

    assertEquals(expectedMessages.length, violations.size(),
            "Expected " + expectedMessages.length + " violations, but found " + violations.size());

    String[] actualMessages = violations.stream()
            .map(ConstraintViolation::getMessage)
            .sorted()
            .toArray(String[]::new);
    Arrays.sort(expectedMessages);

    Assertions.assertTrue(Arrays.equals(expectedMessages, actualMessages),
            "Expected messages: " + Arrays.toString(expectedMessages) +
                    ", but got: " + Arrays.toString(actualMessages));
  }

  @ParameterizedTest
  @MethodSource("validFunctions")
  void validSyntax(StableMatchingProblemDto dto) throws Exception {

    Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
    Assertions.assertTrue(violations.isEmpty(), "Expected no violations for valid requirements");
  }


  @ParameterizedTest
  @MethodSource("validSigmaFunctions")
  void validSigma(StableMatchingProblemDto dto) throws Exception {

    Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
    Assertions.assertTrue(violations.isEmpty(), "Expected no violations for valid requirements");
  }


  private static Stream<Arguments> validFunctions() {
    return Stream.of(
            Arguments.of(createDto("default")),
            Arguments.of(createDto("S1 + S2")),
            Arguments.of(createDto("( M1 + M2 )- M3 ")),
            Arguments.of(createDto("M1 - M2")),
            Arguments.of(createDto("M1 / 2"))
    );
  }

  private static Stream<Arguments> validSigmaFunctions() {
    return Stream.of(
            Arguments.of(createDto("SIGMA{S1} + SIGMA{S2}")),
            Arguments.of(createDto("SIGMA{S1} + 3")),
            Arguments.of(createDto("( SIGMA{S2} + M1 )"))
    );
  }

  private static Stream<Arguments> invalidSigmaFunctions() {
    return Stream.of(
            Arguments.of(
                    createDto("SIGMA{S1} + INVALID"),
                    new String[]{"Invalid token 'INVALID' at position 12"}
            ),
            Arguments.of(
                    createDto("SIGMA{S1} * / SIGMA{S2}"),
                    new String[]{"Consecutive operators at position 10"}
            ),
            Arguments.of(
                    createDto("SIGMA{S1} + INVALID_VARIABLE"),
                    new String[]{"Invalid token 'INVALID_VARIABLE' at position 12"}
            ),
            Arguments.of(
                    createDto("SIGMA{S10} + M1"),
                    new String[]{"Invalid after SIGMA index: 10, at position 7. Must be between 1 and 2"}
            ),
            Arguments.of(
                    createDto("SIGMA{S1+} + SIGMA{1}"),
                    new String[]{
                            "Invalid inner Sigma format: SIGMA{1}, at position 19",
                            "Invalid inner Sigma format: SIGMA{S1+}, at position 6"
                    }
            ),
            Arguments.of(
                    createDto("SIGMA{S1} / 0 "),
                    new String[]{"Division by zero at position 11"}
            )
    );
  }

  private static Stream<Arguments> malformedFunctions() {
    return Stream.of(
            Arguments.of(
                    createDto("defaulttt"),
                    new String[]{"Invalid token 'defaulttt' at position 0"}
            ),
            Arguments.of(
                    createDto("S3 + S4"),
                    new String[]{
                            "Invalid S token: 4, at position 6. Must be between 1 and 2",
                            "Invalid S token: 3, at position 1. Must be between 1 and 2"
                    }
            ),
            Arguments.of(
                    createDto("(M1 + M2 - M3"),
                    new String[]{"Unmatched opening bracket at position 0"}
            ),
            Arguments.of(
                    createDto("M1 -/ M2"),
                    new String[]{"Consecutive operators at position 3"}
            ),
            Arguments.of(
                    createDto("abc - abc"),
                    new String[]{
                            "Invalid token 'abc' at position 6",
                            "Invalid token 'abc' at position 0"
                    }
            ),
            Arguments.of(
                    createDto("M1 / 0"),
                    new String[]{"Division by zero at position 4"}
            )
    );
  }

  private static StableMatchingProblemDto createDto(String fitnessFunction) {
    return StableMatchingProblemDto.builder()
            .problemName("Test Base Case")
            .numberOfSets(2)
            .numberOfProperty(2)
            .individualSetIndices(new int[]{0, 1, 1})
            .individualCapacities(new int[]{1, 1, 1})
            .individualRequirements(new String[][] {{ "1", "1.1" }, { "1++", "1.1"}, { "1", "1" } })
            .individualWeights(new double[][]{{1.0, 2.0}, {3.0, 4.0}, {3.0, 4.0}})
            .individualProperties(new double[][]{{5.0, 6.0}, {7.0, 8.0}, {7.0, 8.0}})
            .evaluateFunctions(new String[]{"default", "default"})
            .fitnessFunction(fitnessFunction)
            .excludedPairs(null)
            .populationSize(100)
            .generation(50)
            .numberOfIndividuals(3)
            .maxTime(5000)
            .algorithm("NSGAII")
            .distributedCores("all")
            .build();
  }
}
