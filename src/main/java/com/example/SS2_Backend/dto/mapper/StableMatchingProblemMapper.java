package com.example.SS2_Backend.dto.mapper;

import com.example.SS2_Backend.dto.request.StableMatchingProblemDTO;
import com.example.SS2_Backend.ss.smt.MatchingData;
import com.example.SS2_Backend.ss.smt.evaluator.FitnessEvaluator;
import com.example.SS2_Backend.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import com.example.SS2_Backend.ss.smt.implement.MTMProblem;
import com.example.SS2_Backend.ss.smt.implement.OTMProblem;
import com.example.SS2_Backend.ss.smt.implement.OTOProblem;
import com.example.SS2_Backend.ss.smt.implement.TripletOTOProblem;
import com.example.SS2_Backend.ss.smt.preference.PreferenceBuilder;
import com.example.SS2_Backend.ss.smt.preference.PreferenceListWrapper;
import com.example.SS2_Backend.ss.smt.preference.impl.provider.TripletPreferenceProvider;
import com.example.SS2_Backend.ss.smt.preference.impl.provider.TwoSetPreferenceProvider;
import com.example.SS2_Backend.ss.smt.requirement.Requirement;
import com.example.SS2_Backend.ss.smt.requirement.RequirementDecoder;
import com.example.SS2_Backend.util.EvaluatorUtils;

/**
 * Mapper layer, xử lý các công việc sau đối với từng loại matching problem:
 * 1. map problem data từ dto vào StableMatchingProblem
 * 2. tính toán các preference list và set vào StableMatchingProblem
 */
public class StableMatchingProblemMapper {

    public static OTOProblem toOTO(StableMatchingProblemDTO dto) {
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


    public static OTMProblem toOTM(StableMatchingProblemDTO request) {
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
        return new OTMProblem(request.getProblemName(),
                request.getNumberOfIndividuals(),
                request.getNumberOfSets(),
                data,
                preferenceLists,
                request.getFitnessFunction(),
                fitnessEvaluator);
    }

    public static MTMProblem toMTM(StableMatchingProblemDTO request) {
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
    public static TripletOTOProblem toTripletOTO(StableMatchingProblemDTO request) {
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
