package org.fit.ssapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.fit.ssapp.dto.request.functioncheck.GtFunctionCheckRequest;
import org.fit.ssapp.dto.request.functioncheck.SmEvaluateCheckRequest;
import org.fit.ssapp.dto.request.functioncheck.SmFitnessCheckRequest;
import org.fit.ssapp.dto.response.functioncheck.FunctionCheckResult;
import org.fit.ssapp.dto.response.functioncheck.GtFunctionCheckResponse;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.Test;

/**
 * Tests for the function-check feature (no MOEA, no Spring context needed).
 */
class FunctionCheckServiceTest {

  private final FunctionCheckService service = new FunctionCheckService();

  private static NormalPlayer player(double... props) {
    Strategy strategy = new Strategy();
    List<Double> list = new ArrayList<>();
    for (double p : props) {
      list.add(p);
    }
    strategy.setProperties(list);
    NormalPlayer player = new NormalPlayer();
    player.setStrategies(new ArrayList<>(List.of(strategy)));
    return player;
  }

  @Test
  void gtFitnessWithFewPlayersMatchesReference() {
    GtFunctionCheckRequest request = new GtFunctionCheckRequest();
    request.setPayoffs(new double[] {2, 3, 4});
    request.setFitnessFunction("u1 + u2 * u3");

    GtFunctionCheckResponse response = service.checkGameTheory(request);

    assertTrue(response.getFitness().isSuccess());
    assertEquals(14.0, response.getFitness().getValue(), 1e-9);
    assertEquals(14.0, response.getFitness().getReferenceValue(), 1e-9);
    assertFalse(response.getFitness().isMismatch());
  }

  @Test
  void gtFitnessAccuratelyEvaluatesU1PlusU10WithoutPrefixCorruption() {
    // Verifies the bug reported by the student is now fully resolved:
    // "u1 + u10" evaluates correctly to 5 + 20 = 25.0, matching reference calculation.
    double[] payoffs = new double[10];
    payoffs[0] = 5;
    payoffs[9] = 20;
    GtFunctionCheckRequest request = new GtFunctionCheckRequest();
    request.setPayoffs(payoffs);
    request.setFitnessFunction("u1 + u10");

    FunctionCheckResult fitness = service.checkGameTheory(request).getFitness();

    assertEquals(25.0, fitness.getReferenceValue(), 1e-9);
    assertFalse(fitness.isMismatch());
    assertEquals(25.0, fitness.getValue(), 1e-9);
  }

  @Test
  void gtDatasetModeComputesPayoffsThenFitness() {
    GtFunctionCheckRequest request = new GtFunctionCheckRequest();
    request.setNormalPlayers(List.of(player(1, 2), player(3, 4)));
    request.setDefaultPayoffFunction("p1 * p2");
    request.setFitnessFunction("u1 + u2");
    request.setMaximizing(true);

    GtFunctionCheckResponse response = service.checkGameTheory(request);

    assertEquals(2.0, response.getPlayers().get(0).getValue(), 1e-9);
    assertEquals(12.0, response.getPlayers().get(1).getValue(), 1e-9);
    assertEquals(14.0, response.getFitness().getValue(), 1e-9);
    assertEquals(-14.0, response.getObjective(), 1e-9);
  }

  @Test
  void gtRelativePayoffReadsOtherPlayers() {
    GtFunctionCheckRequest request = new GtFunctionCheckRequest();
    request.setNormalPlayers(List.of(player(1, 2), player(3, 4)));
    request.setDefaultPayoffFunction("p1 + P2p2");
    request.setFitnessFunction("default");

    GtFunctionCheckResponse response = service.checkGameTheory(request);

    assertEquals(5.0, response.getPlayers().get(0).getValue(), 1e-9);
    assertEquals(7.0, response.getPlayers().get(1).getValue(), 1e-9);
    assertTrue(response.getFitness().isSuccess());
  }

  @Test
  void gtInvalidFunctionReportsErrorInsteadOfThrowing() {
    GtFunctionCheckRequest request = new GtFunctionCheckRequest();
    request.setPayoffs(new double[] {1, 2});
    request.setFitnessFunction("u1 + * u2");

    FunctionCheckResult fitness = service.checkGameTheory(request).getFitness();

    assertFalse(fitness.isSuccess());
    assertNotNull(fitness.getError());
  }

  @Test
  void smEvaluateManualVariables() {
    SmEvaluateCheckRequest request = new SmEvaluateCheckRequest();
    request.setFunction("P1 * W1 + R1");
    request.setVariables(Map.of("P1", 3.0, "W1", 2.0, "R1", 1.0));

    FunctionCheckResult result = service.checkSmEvaluate(request);

    assertTrue(result.isSyntaxValid());
    assertTrue(result.isSuccess());
    assertEquals(7.0, result.getValue(), 1e-9);
  }

  @Test
  void smEvaluateSyntaxOnlyAndMissingVariables() {
    SmEvaluateCheckRequest syntaxOnly = new SmEvaluateCheckRequest();
    syntaxOnly.setFunction("P1 + W2");
    FunctionCheckResult ok = service.checkSmEvaluate(syntaxOnly);
    assertTrue(ok.isSyntaxValid());
    assertFalse(ok.isSuccess());
    assertEquals(2, ok.getRequiredVariables().size());

    SmEvaluateCheckRequest missing = new SmEvaluateCheckRequest();
    missing.setFunction("P1 + W2");
    missing.setVariables(Map.of("P1", 1.0));
    assertTrue(service.checkSmEvaluate(missing).getError().contains("W2"));

    SmEvaluateCheckRequest broken = new SmEvaluateCheckRequest();
    broken.setFunction("P1 + * W2");
    assertFalse(service.checkSmEvaluate(broken).isSyntaxValid());
  }

  @Test
  void smFitnessManualSatisfactions() {
    SmFitnessCheckRequest request = new SmFitnessCheckRequest();
    request.setFunction("M1 + M2 * M3");
    request.setSatisfactions(new double[] {1, 2, 3, 4});

    FunctionCheckResult result = service.checkSmFitness(request);

    assertTrue(result.isSuccess(), result.getError());
    assertEquals(7.0, result.getValue(), 1e-9);
  }

  @Test
  void smFitnessNeedsEitherProblemOrSatisfactions() {
    FunctionCheckResult result = service.checkSmFitness(new SmFitnessCheckRequest());
    assertFalse(result.isSuccess());
    assertNotNull(result.getError());
  }
}
