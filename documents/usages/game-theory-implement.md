# Game Theory Implementation Documentation

## Table of Contents
1. [Game Theory Players](#game-theory-players)
2. [Strategies](#strategies)
3. [Game Problem Solution](#game-problem-solution)
4. [Evaluation Process](#evaluation-process)
5. [Running a Game Theory Problem](#running-a-game-theory-problem)
6. [Conclusion](#conclusion)

## Game Theory Players

### Player Overview
The `NormalPlayer` class represents a player in the game theory model. Each player contains:
- **Name** (`name`)
- **List of available strategies** (`strategies`)
- **Payoff values** (`payoffValues`)
- **Previous strategy index** (`prevStrategyIndex`)
- **Payoff calculation function** (`payoffFunction`)
- **Current payoff value** (`payoff`)

#### Key Capabilities:
- Retrieve strategy by index
- Remove strategies
- Find dominant strategy
- Calculate payoff based on other players' choices

### Player Class Implementation
```java
@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NormalPlayer implements Serializable {
  private String name;
  private List<Strategy> strategies;
  private List<BigDecimal> payoffValues;
  private int prevStrategyIndex = -1;
  private String payoffFunction;
  private BigDecimal payoff;

  public Strategy getStrategyAt(int index) {
    return strategies.get(index);
  }

  public void removeStrategiesAt(int index) {
    strategies.set(index, null);
  }

  public int getDominantStrategyIndex() {
    List<Double> payoffs = strategies.stream()
        .map(Strategy::getPayoff)
        .toList();
    double maxPayoffValue = payoffs.stream()
            .max(Double::compareTo)
            .orElse(0D);
    return payoffs.indexOf(maxPayoffValue);
  }

  public void evaluatePayoff(List<NormalPlayer> normalPlayers, int[] chosenStrategyIndices) {
    if (payoffFunction != null && !payoffFunction.isBlank()) {
      this.payoff = StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers(
          this.getStrategyAt(chosenStrategyIndices[this.strategies.indexOf(this)]),
          payoffFunction,
          normalPlayers,
          chosenStrategyIndices);
    } else {
      this.payoff = StringExpressionEvaluator.calculateByDefault(
          this.getStrategyAt(chosenStrategyIndices[this.strategies.indexOf(this)]).getProperties(),
          null);
    }
  }
}
```

## Strategies

### Strategy Overview
Each player maintains a set of possible strategies. Each `Strategy` contains:
- **Name** (`name`)
- **List of properties** (`properties`)
- **Payoff value** (`payoff`)

#### Key Capabilities:
- Evaluate payoff expressions
- Add new properties
- String representation

### Strategy Class Implementation
```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Strategy implements Serializable {
  private String name;
  private List<Double> properties = new ArrayList<>();
  private double payoff;

  public double evaluateStringExpression(String expression,
                                      List<NormalPlayer> normalPlayers,
                                      int[] chosenStrategyIndices) {
    return StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers(
      this, expression, normalPlayers, chosenStrategyIndices).doubleValue();
  }

  public void addProperty(double property) {
    properties.add(property);
  }
}
```

## Game Problem Solution

### Solution Overview
The `GameSolution` class represents an evolved solution containing:
- **Fitness value** (`fitnessValue`)
- **List of players** (`players`)
- **Algorithm used** (`algorithm`)
- **Runtime information** (`runtime`)
- **Computer specifications** (`computerSpecs`)

Each solution contains the strategies selected during the evolutionary process.

### Solution Class Implementation
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSolution {
  private double fitnessValue;
  private List<Player> players;
  private String algorithm;
  private double runtime;
  private ComputerSpecs computerSpecs;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Player {
    private String playerName;
    private String strategyName;
    private double payoff;
  }
}
```

## Evaluation Process

### Evaluation Overview
The `StringExpressionEvaluator` class handles all evaluation operations:
- Payoff evaluation with relative players
- Payoff evaluation without relative players
- Fitness value calculation
- Default functions: `SUM`, `AVERAGE`, `MIN`, `MAX`, `PRODUCT`, `MEDIAN`, `RANGE`

### Key Evaluation Methods
```java
public static BigDecimal evaluatePayoffFunctionWithRelativeToOtherPlayers(
    Strategy strategy,
    String payoffFunction,
    List<NormalPlayer> normalPlayers,
    int[] chosenStrategyIndices) {
  
  String expression = payoffFunction;
  Pattern generalPattern = Pattern.compile("(P[0-9]+)?" + nonRelativePattern.pattern());
  
  // Process expression and replace variables
  // ...
  
  double val = evaluateExpression(expression);
  return new BigDecimal(val).setScale(10, RoundingMode.HALF_UP);
}

public static BigDecimal evaluateFitnessValue(double[] payoffs, String fitnessFunction) {
  // Implementation details...
}

private static double evaluateExpression(String expression) {
  // Implementation details...
}
```

## Running a Game Theory Problem

### Execution Workflow
1. Initialize players with their respective strategies
2. Define payoff functions for each player
3. Configure MOEA framework parameters
4. Run evolutionary algorithm
5. Evaluate and select optimal solutions

### Example Implementation
```java
// 1. Initialize players and strategies
NormalPlayer player1 = new NormalPlayer();
player1.setName("Player1");
player1.setStrategies(Arrays.asList(
    new Strategy("Strat1", Arrays.asList(1.0, 2.0), 0),
    new Strategy("Strat2", Arrays.asList(1.5, 1.0), 0)
));
player1.setPayoffFunction("p1 + p2");

// 2. Set up problem
GameProblem problem = new GameProblem(Arrays.asList(player1, player2));

// 3. Configure and run algorithm
NSGAII algorithm = new NSGAII(problem);
algorithm.setInitialPopulation(100);
algorithm.run(10000); // Run for 10,000 generations

// 4. Retrieve results
List<GameSolution> solutions = algorithm.getResult();
```

## Conclusion
This implementation provides a flexible framework for game theory analysis using evolutionary algorithms. Key features include:
- Modular player and strategy definitions
- Customizable payoff functions
- Multiple evaluation methods
- Integration with the MOEA framework

