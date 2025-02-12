package org.fit.ssapp.ss.smt.evaluator;

/**
 * Evaluates the default fitness function based on an array of satisfaction values.
 *
 */
public interface FitnessEvaluator {

  /**
   * TODO: Tôi nghĩ hàm này nên được lấy bên PreferenceList thì hay hơn.
   *  Thằng FitnessEvaluator chỉ cần nhận array giá trị và phệt logic vào thôi.
   */

  double defaultFitnessEvaluation(double[] satisfactions);

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  double withFitnessFunctionEvaluation(double[] satisfactions, String fnf);

}
