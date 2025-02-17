package org.fit.ssapp.dto.mapper;

import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.evaluator.FitnessEvaluator;
import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.ss.smt.implement.MTMProblem;
import org.fit.ssapp.ss.smt.implement.OTMProblem;
import org.fit.ssapp.ss.smt.implement.OTOProblem;
import org.fit.ssapp.ss.smt.implement.TripletOTOProblem;
import org.fit.ssapp.ss.smt.implement.var.CustomVariation;
import org.fit.ssapp.ss.smt.preference.PreferenceBuilder;
import org.fit.ssapp.ss.smt.preference.PreferenceListWrapper;
import org.fit.ssapp.ss.smt.preference.impl.provider.TripletPreferenceProvider;
import org.fit.ssapp.ss.smt.preference.impl.provider.TwoSetPreferenceProvider;
import org.fit.ssapp.ss.smt.requirement.Requirement;
import org.fit.ssapp.ss.smt.requirement.RequirementDecoder;
import org.fit.ssapp.util.EvaluatorUtils;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.spi.OperatorProvider;
import org.moeaframework.util.TypedProperties;

/**
 * Mapper layer, xử lý các công việc sau đối với từng loại matching problem: 1. map problem data từ
 * dto vào StableMatchingProblem 2. tính toán các preference list và set vào StableMatchingProblem
 */
public class StableMatchingProblemMapper {

  static {
    OperatorFactory.getInstance().addProvider(new OperatorProvider() {
      @Override
      public String getMutationHint(Problem problem) {
        return null;
      }

      @Override
      public String getVariationHint(Problem problem) {
        return null;
      }

      @Override
      public Variation getVariation(String name, TypedProperties properties, Problem problem) {
        if (name.equalsIgnoreCase("CV")) {
          double crossoverRate = properties.getDouble("cr.rate", 0.9);
          double mutationRate = properties.getDouble("mut.rate", 0.1);
          return new CustomVariation(crossoverRate, mutationRate, problem.getNumberOfVariables());
        }
        return null;
      }
    });
  }

  public static OTOProblem toOTO(StableMatchingProblemDto dto) {
    Requirement[][] requirements = RequirementDecoder.decode(dto.getIndividualRequirements());
    MatchingData data = new MatchingData(
        dto.getNumberOfIndividuals(),
        dto.getNumberOfProperty(),
        dto.getIndividualSetIndices(),
        null,
        dto.getIndividualProperties(),
        dto.getIndividualWeights(),
        requirements
    );
    data.setExcludedPairs(dto.getExcludedPairs());
    PreferenceBuilder builder = new TwoSetPreferenceProvider(
        data,
        dto.getEvaluateFunctions()
    );
    PreferenceListWrapper preferenceLists = builder.toListWrapper();
    FitnessEvaluator fitnessEvaluator = new TwoSetFitnessEvaluator(data);
    return new OTOProblem(
        dto.getProblemName(),
        dto.getNumberOfIndividuals(),
        dto.getNumberOfSets(),
        data,
        preferenceLists,
        dto.getFitnessFunction(),
        fitnessEvaluator);
  }


  public static OTMProblem toOTM(StableMatchingProblemDto request) {
    Requirement[][] requirements = RequirementDecoder.decode(request.getIndividualRequirements());
    MatchingData data = new MatchingData(request.getNumberOfIndividuals(),
        request.getNumberOfProperty(),
        request.getIndividualSetIndices(),
        request.getIndividualCapacities(),
        request.getIndividualProperties(),
        request.getIndividualWeights(),
        requirements);
    data.setExcludedPairs(request.getExcludedPairs());
    PreferenceBuilder builder = new TwoSetPreferenceProvider(data,
        request.getEvaluateFunctions());
    PreferenceListWrapper preferenceLists = builder.toListWrapper();
    FitnessEvaluator fitnessEvaluator = new TwoSetFitnessEvaluator(data);


    return new OTMProblem(
            request.getProblemName(),
            request.getNumberOfIndividuals(),
            request.getNumberOfSets(),
            data,
            preferenceLists,
            request.getFitnessFunction(),
            fitnessEvaluator
    );
  }

  public static MTMProblem toMTM(StableMatchingProblemDto request) {
    Requirement[][] requirements = RequirementDecoder.decode(request.getIndividualRequirements());
    MatchingData data = new MatchingData(request.getNumberOfIndividuals(),
        request.getNumberOfProperty(),
        request.getIndividualSetIndices(),
        request.getIndividualCapacities(),
        request.getIndividualProperties(),
        request.getIndividualWeights(),
        requirements);
    data.setExcludedPairs(request.getExcludedPairs());
    PreferenceBuilder builder = new TwoSetPreferenceProvider(data,
        request.getEvaluateFunctions());
    PreferenceListWrapper preferenceLists = builder.toListWrapper();
    FitnessEvaluator fitnessEvaluator = new TwoSetFitnessEvaluator(data);
    String fitnessFunction = EvaluatorUtils.getValidFitnessFunction(request.getFitnessFunction());
    return new MTMProblem(request.getProblemName(),
        request.getNumberOfIndividuals(),
        request.getNumberOfSets(),
        data,
        preferenceLists,
        fitnessFunction,
        fitnessEvaluator);
  }

  public static TripletOTOProblem toTripletOTO(StableMatchingProblemDto request) {
    Requirement[][] requirements = RequirementDecoder.decode(request.getIndividualRequirements());
    MatchingData data = new MatchingData(request.getNumberOfIndividuals(),
        request.getNumberOfProperty(),
        request.getIndividualSetIndices(),
        request.getIndividualCapacities(),
        request.getIndividualProperties(),
        request.getIndividualWeights(),
        requirements);
    data.setExcludedPairs(request.getExcludedPairs());
    PreferenceBuilder builder = new TripletPreferenceProvider(data,
        request.getEvaluateFunctions());
    PreferenceListWrapper preferenceLists = builder.toListWrapper();
    FitnessEvaluator fitnessEvaluator = new TwoSetFitnessEvaluator(data);
    return new TripletOTOProblem(request.getProblemName(),
        request.getNumberOfIndividuals(),
        request.getNumberOfSets(),
        data,
        preferenceLists,
        request.getFitnessFunction(),
        fitnessEvaluator);
  }


}
