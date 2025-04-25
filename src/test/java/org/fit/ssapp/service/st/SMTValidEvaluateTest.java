package org.fit.ssapp.service.st;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;


import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
public class SMTValidEvaluateTest {
  @Autowired
  private Validator validator;

  @ParameterizedTest
  @MethodSource("validInvalidFunctions")
  void invalidSyntax(StableMatchingProblemDto dto, String[] expectedMessages) throws Exception {

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

  @ParameterizedTest
  @MethodSource("validFunctions")
  void validSyntax(StableMatchingProblemDto dto) throws Exception {

    Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
    assertTrue(violations.isEmpty(), "Expected no violations for valid requirements");
  }

  private static Stream<Arguments> validFunctions() {
    return Stream.of(
            Arguments.of(createDto("default")),
            Arguments.of(createDto("abs(R1 - R2) + 1")),
            Arguments.of(createDto("12^2 + log(R1) * P1 + log2(W2) + P2")),
            Arguments.of(createDto("ceil(R2) + (15 / 2) * W2")),
            Arguments.of(createDto("((P1 * W1) + R2 * W2) / (W1 + R1)"))
    );
  }

  private static Stream<Arguments> validInvalidFunctions() {
    return Stream.of(
            Arguments.of(
                    createDto("defaulttt"),
                    new String[]{"Invalid token 'defaulttt' at position 0"}
            ),
            Arguments.of(
                    createDto("R1 -* W2"),
                    new String[]{"Consecutive operators at position 3"}
            ),
            Arguments.of(
                    createDto("W2 / 4,2"),
                    new String[]{"Invalid token '4,2' at position 5"}
            ),
            Arguments.of(
                    createDto("(M1 - @2"),
                    new String[]{
                            "Invalid token 'M1' at position 1",
                            "Invalid token '@2' at position 6",
                            "Unmatched opening bracket at position 0"
                    }
            ),
            Arguments.of(
                    createDto("m1 + P2"),
                    new String[]{"Invalid token 'm1' at position 0"}
            ),
            Arguments.of(
                    createDto("P5 + Wi5"),
                    new String[]{
                            "Invalid P token: 5, at position 1. Must be between 1 and 2",
                            "Invalid token 'Wi5' at position 5"
                    }
            ),
            Arguments.of(
                    createDto("ceil(R5) + (15 / 2) * W3"),
                    new String[]{
                            "Invalid R token: 5, at position 6. Must be between 1 and 2",
                            "Invalid W token: 3, at position 23. Must be between 1 and 2"
                    }
            ),
            Arguments.of(
                    createDto("Floor(R2) + 15^2"),
                    new String[]{"Invalid token 'Floor' at position 0"}
            )
    );
  }

  private static StableMatchingProblemDto createDto(String evaluateFunction) {
    return StableMatchingProblemDto.builder()
            .problemName("Test Base Case")
            .numberOfSets(2)
            .numberOfProperty(2)
            .individualSetIndices(new int[]{0, 1, 1})
            .individualCapacities(new int[]{1, 1, 1})
            .individualRequirements(new String[][] {{ "1", "1.1" }, { "1++", "1.1"}, { "1", "1" } })
            .individualWeights(new double[][]{{1.0, 2.0}, {3.0, 4.0}, {3.0, 4.0}})
            .individualProperties(new double[][]{{5.0, 6.0}, {7.0, 8.0}, {7.0, 8.0}})
            .evaluateFunctions(new String[]{evaluateFunction, evaluateFunction})
            .fitnessFunction("default")
            .excludedPairs(null)
            .populationSize(100)
            .generation(50)
            .numberOfIndividuals(3)
            .maxTime(5000)
            .algorithm("NSGAII")
            .distributedCores("all")
            .build();
  }

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc _mock;

}