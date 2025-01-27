package org.fit.ssapp.ss.smt.evaluator;

/**
 * FitnessEvaluator in tơ phịt
 */
public interface FitnessEvaluator {

  /**
   * TODO: Tôi nghĩ hàm này nên được lấy bên PreferenceList thì hay hơn.
   *  Thằng FitnessEvaluator chỉ cần nhận array giá trị và phệt logic vào thôi.
   * @param satisfactions double[]
   * @return double
   */
//    double[] getAllSatisfactions(Matches matches, List<PreferenceList> preferenceLists);

  double defaultFitnessEvaluation(double[] satisfactions);

  /**
   * @param satisfactions double[]
   * @param fnf           String
   * @return Matches
   */
  double withFitnessFunctionEvaluation(double[] satisfactions, String fnf);

}
