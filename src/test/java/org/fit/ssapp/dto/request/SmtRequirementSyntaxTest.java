package org.fit.ssapp.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class SmtRequirementSyntaxTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private StableMatchingProblemDto baseDtoWith(String[][] requirements) {
        StableMatchingProblemDto dto = new StableMatchingProblemDto();
        dto.setProblemName("Stable Test");
        dto.setNumberOfSets(3);
        dto.setNumberOfProperty(2);
        dto.setIndividualSetIndices(new int[]{0, 1, 2});
        dto.setIndividualCapacities(new int[]{1, 1, 1});
        dto.setIndividualRequirements(requirements);
        dto.setIndividualWeights(new double[][]{{1.0, 2.0}, {3.0, 4.0 }, {3.0, 4.0}});
        dto.setIndividualProperties(new double[][]{{5.0, 6.0}, {7.0, 8.0 }, {7.0, 8.0}});
        dto.setEvaluateFunctions(new String[]{"default", "default", "default"});
        dto.setFitnessFunction("default");
        return dto;
    }

    @Test
    void testValidRequirementSyntax_shouldPassValidation() {
        String[][] valid = {
                {"1.0", "2.0:3.0", "4.5++"},
                {"0.5--", "1.2:2.4", "3.3"},
                {"5.0", "6.0++", "7.8--"}
        };

        StableMatchingProblemDto dto = baseDtoWith(valid);
        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Expected no violations");
    }

    @Test
    void testEmptyRequirement_shouldFail() {
        String[][] invalid = {
                {"", "2.0", "3.0"},
                {"4.0", "5.0", "6.0"},
                {"7.0", "8.0", "9.0"}
        };

        StableMatchingProblemDto dto = baseDtoWith(invalid);
        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Requirement cannot be empty"));
    }

    @Test
    void testInvalidRangeOrder_shouldFail() {
        String[][] invalid = {
                {"1.0:0.5", "2.0", "3.0"}, // left > right
                {"4.0", "5.0", "6.0"},
                {"7.0", "8.0", "9.0"}
        };

        StableMatchingProblemDto dto = baseDtoWith(invalid);
        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Invalid range logic"));
    }

    @Test
    void testInvalidSuffix_shouldFail() {
        String[][] invalid = {
                {"1.0", "2.0", "3.0++"},
                {"4.0", "5.0++extra", "6.0"}, // Invalid suffix format
                {"7.0", "8.0", "9.0"}
        };

        StableMatchingProblemDto dto = baseDtoWith(invalid);
        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Invalid suffix format"));
    }

    @Test
    void testInvalidNumberFormat_shouldFail() {
        String[][] invalid = {
                {"abc", "2.0", "3.0"}, // Invalid number
                {"4.0", "5.0", "6.0"},
                {"7.0", "8.0", "9.0"}
        };

        StableMatchingProblemDto dto = baseDtoWith(invalid);
        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Requirement must start with a valid number"));
    }

    @Test
    void testMultipleErrors_shouldDetectAll() {
        String[][] invalid = {
                {"", "2.0", "bad++"},              // row 0: empty + bad++
                {"4.0", "5.0:2.0", "oops"},         // row 1: range error + invalid number
                {"7.0", "bad--stuff", "9.0"}        // row 2: invalid suffix
        };

        StableMatchingProblemDto dto = baseDtoWith(invalid);
        Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
        assertEquals(5, violations.size()); // 5 errors expected
    }
}

