package org.fit.ssapp.dto.mapper;

import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.exception.BadRequestException;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.MatchingProblem;
import org.fit.ssapp.ss.smt.evaluator.FitnessEvaluator;
import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.ss.smt.implement.MTMProblem;
import org.fit.ssapp.ss.smt.implement.OTMProblem;
import org.fit.ssapp.ss.smt.implement.OTOProblem;
import org.fit.ssapp.ss.smt.implement.PsoCompatMtmProblem;
import org.fit.ssapp.ss.smt.implement.TripletOTOProblem;
import org.fit.ssapp.ss.smt.preference.PreferenceBuilder;
import org.fit.ssapp.ss.smt.preference.PreferenceList;
import org.fit.ssapp.ss.smt.preference.PreferenceListWrapper;
import org.fit.ssapp.ss.smt.preference.impl.list.TripletPreferenceList;
import org.fit.ssapp.ss.smt.preference.impl.list.TwoSetPreferenceList;
import org.fit.ssapp.ss.smt.preference.impl.provider.TripletPreferenceProvider;
import org.fit.ssapp.ss.smt.preference.impl.provider.TwoSetPreferenceProvider;
import org.fit.ssapp.ss.smt.requirement.Requirement;
import org.fit.ssapp.ss.smt.requirement.RequirementDecoder;
import org.fit.ssapp.util.EvaluatorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper layer, xử lý các công việc sau đối với từng loại matching problem: 1. map problem data từ
 * dto vào StableMatchingProblem 2. tính toán các preference list và set vào StableMatchingProblem
 */
public class StableMatchingProblemMapper {


  /**
   * Map from request to problem.
   *
   * @param dto StableMatchingProblemDto
   * @return OTOProblem
   */
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
    validateUniformPreferences(preferenceLists);

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

  /**
   * Map from request to problem.
   *
   * @param request StableMatchingProblemDto
   * @return OTMProblem
   */
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
    validateUniformPreferences(preferenceLists);
    FitnessEvaluator fitnessEvaluator = new TwoSetFitnessEvaluator(data);

    return new OTMProblem(
            request.getProblemName(),

            request.getNumberOfIndividuals(),
            request.getNumberOfSets(),
            data,
            preferenceLists,
            request.getFitnessFunction(),
            fitnessEvaluator);
  }

  /**
   * Map from request to problem.
   *
   * @param request StableMatchingProblemDto
   * @return MTMProblem
   */
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
    validateUniformPreferences(preferenceLists);

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

  /**
   * Map from request to problem.
   *
   * @param request StableMatchingProblemDto
   * @return TripletOTOProblem
   */
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
    validateUniformPreferences(preferenceLists);

    FitnessEvaluator fitnessEvaluator = new TwoSetFitnessEvaluator(data);
    return new TripletOTOProblem(request.getProblemName(),
            request.getNumberOfIndividuals(),
            request.getNumberOfSets(),
            data,
            preferenceLists,
            request.getFitnessFunction(),
            fitnessEvaluator);
  }


  public static MatchingProblem toPsoCompat(StableMatchingProblemDto request) {
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
    return new PsoCompatMtmProblem(request.getProblemName(),
        request.getNumberOfIndividuals(),
        request.getNumberOfSets(),
        data,
        preferenceLists,
        fitnessFunction,
        fitnessEvaluator);
  }

  private static void validateUniformPreferences(PreferenceListWrapper wrapper) {
    List<Integer> invalidAgents = new ArrayList<>();

    int i = 0;
    for (PreferenceList list : wrapper.getLists()) {
      if ((list instanceof TwoSetPreferenceList twoSet && twoSet.isUniformPreference()) ||
              (list instanceof TripletPreferenceList triplet && triplet.isUniformPreference())) {
        invalidAgents.add(i);
      }
      i++;
    }

    if (!invalidAgents.isEmpty()) {
      String msg = "Uniform preference detected for agents: " + invalidAgents;
      throw new BadRequestException(msg);
    }
  }



}

