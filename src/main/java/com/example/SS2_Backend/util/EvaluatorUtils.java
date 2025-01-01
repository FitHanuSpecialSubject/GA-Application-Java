package com.example.SS2_Backend.util;

import com.example.SS2_Backend.constants.AppConst;
import com.example.SS2_Backend.constants.MatchingConst;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;

import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;

public class EvaluatorUtils {

    /**
     * get length of ??CONTENT?? inside SIGMA{??CONTENT??}
     *
     * @param function function String
     * @param startIndex "{" start bracket index
     * @return length
     */
    public static int getSigmaFunctionExpressionLength(String function, int startIndex) {
        int num = 0;
        for (int i = startIndex; i < function.charAt(i); i++) {
            char ch = function.charAt(i);
            if (ch == '}') {
                return num;
            } else {
                num++;
            }
        }
        return num;
    }

    /**
     * @param satisfactions - Double array contains satisfactions of the whole population sequentially (0, 1, 2, ... , n)
     * @param expression    - Mathematical String that Express how each of the value calculated. Example: S0/2, S1^3
     *                      <i>
     *                      Cases:
     *                                       <ol>
     *                                       	<li>
     *                                          		<i>S1</i>represents satisfactions of set 1 (array)
     *                                       	</li>
     *                                       	<li>
     *                                      		<i>S2</i>represents satisfactions of set 2 (array)
     *                                      	 </li>
     *                                       </ol>
     *                      </i>
     * @return double value - Sum of satisfactions of the whole set sequentially
     */
    public static double sigmaCalculate(double[] satisfactions, String expression, int numberOfIndividuals, int numberOfIndividualOfSet0) {
        System.out.println("sigma calculating...");
        double[] streamValue = null;
        String regex = null;
        for (int i = 0; i < expression.length() - 1; i++) {
            char ch = expression.charAt(i);
            if (ch == 'S') {
                char set = expression.charAt(i + 1);
                switch (set) {
                    case '1':
                        streamValue = getSatisfactoryOfASetByDefault(satisfactions, 0, numberOfIndividuals, numberOfIndividualOfSet0);
                        regex = "S1";
                        break;
                    case '2':
                        streamValue = getSatisfactoryOfASetByDefault(satisfactions, 1, numberOfIndividuals, numberOfIndividualOfSet0);
                        regex = "S2";
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Illegal value after S regex in sigma calculation: " + expression);
                }
            }
        }
        if (regex == null) {
            return 0;
        }
        Expression exp = new ExpressionBuilder(expression)
                .variables(regex)
                .build();
        String finalRegex = regex;
        DoubleUnaryOperator calculator = x -> {
            exp.setVariable(finalRegex, x);
            return exp.evaluate();
        };
        return DoubleStream
                .of(streamValue)
                .map(calculator)
                .sum();
    }

    public static double[] getSatisfactoryOfASetByDefault(double[] Satisfactions, int set, int numberOfIndividual, int numberOfIndividualOfSet0) {
        double[] setSatisfactions;
        if (set == 0) {
            setSatisfactions = new double[numberOfIndividualOfSet0];
            System.arraycopy(Satisfactions, 0, setSatisfactions, 0, numberOfIndividualOfSet0);
        } else {
            setSatisfactions = new double[numberOfIndividual - numberOfIndividualOfSet0];
            if (numberOfIndividual - numberOfIndividualOfSet0 >= 0) {
                int idx = 0;
                for (int i = numberOfIndividualOfSet0; i < numberOfIndividual; i++) {
                    setSatisfactions[idx] = Satisfactions[i];
                    idx++;
                }
            }
        }
        return setSatisfactions;
    }


    /**
     * temp
     * @param func
     * @return
     */
    public static String getValidEvaluationFunction(String func) {
        func = func.trim();
        if (func.equals(MatchingConst.DEFAULT_EVALUATE_FUNC)) {
            return "";
        }
        return func;
    }

    /**
     * temp
     * @param func
     * @return
     */
    public static String getIfDefaultFunction(String func) {
        if (AppConst.DEFAULT_FUNC.equalsIgnoreCase(func)) {
            return "";
        }
        return func;
    }

    /**
     * temp
     * @param func
     * @return
     */
    public static String getValidFitnessFunction(String func) {
        func = func.trim();
        if (StringUtils.isEmptyOrNull(func) ||
                func.equalsIgnoreCase(MatchingConst.DEFAULT_FITNESS_FUNC)) {
            return "";
        }
        return func;
    }



    public static void main(String[] args) {
        String[] vars = new String[] {
                "u", "u12", "u21", "u202"
        };
        String[] functions = new String[] {
//                "u+1",
                "u202 + 1 + 2",
                "(u12 + 1) * 2",
                "abs(u12 - u21) / 2"
        };
        for (String func : functions ) {
            Expression e = new ExpressionBuilder(func)
                    .variables(vars)
                    .build();
            Set<String> extractedVars = PreferenceProviderUtils.getVariables(func);
            for (String var : extractedVars) {
                e.setVariable(var,1d);
            }
            ValidationResult res = e.validate();
            printValidateRes(res);
        }

    }

    private static void printValidateRes(ValidationResult valRes) {
        System.out.println("Validation Result: " + valRes.isValid() + ", errors: " + valRes.getErrors());
    }
}
