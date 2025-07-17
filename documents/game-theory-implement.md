
# Game Theory Implementation Documentation

## Table of Contents
1. [Overview](#overview)
2. [Game Model](#game-model)
3. [Players and Strategies](#players-and-strategies)
4. [Game Solution](#game-solution)
5. [Evaluation Process](#evaluation-process)
6. [Running a Game Theory Problem](#running-a-game-theory-problem)
7. [Conflict](#conflict)
8. [Strategy](#strategy)
9. [MOEA Problem Integration](#moea-problem-integration)
10. [Executor Configuration](#executor-configuration)
11. [Conclusion](#conclusion)

## Overview
This project implements a coordination game using game theory principles and evolutionary algorithms. A coordination game is a type of game where players benefit from making the same or complementary decisions. A well-known example is the **Prisoner’s Dilemma**, where cooperation leads to the best collective outcome, but individual incentives might lead to defection.

This implementation provides:
- **A structured game model** with players and strategies.
- **Customizable payoff functions** for evaluating strategy effectiveness.
- **An evolutionary approach** using MOEA to optimize strategies.

## Game Model
The game consists of multiple players, each with a set of strategies. A player’s choice of strategy determines their payoff, which is computed based on a defined function. The system supports:
- **Multiple players** with independent or interdependent payoffs.
- **Flexible strategy definitions** with variable properties.
- **Evolutionary optimization** for discovering optimal strategies.

## Players and Strategies

### Players
A player is represented by:
- **A selected strategy** from a predefined set.
- **A payoff value**, calculated using a mathematical function.

Each player optimizes their strategy to maximize their payoff.

### Strategies
A strategy consists of:
- **A name** to distinguish between different strategies.
- **A set of properties** that define the strategy’s characteristics.
- **A payoff function**, which determines its effectiveness in the game.

Payoff calculations utilize **exp4j**, a mathematical expression parser, to evaluate formula-based payoffs efficiently.

## Game Solution
The `GameSolution` class encapsulates:
- **Fitness value**: A measure of the strategy’s effectiveness.
- **Selected strategies**: The chosen strategies for each player.
- **Algorithm used**: The optimization method applied.
- **Runtime information**: Performance data of the computation.

## Evaluation Process
Evaluation involves computing payoffs and fitness values:
- **Payoffs are computed using exp4j**, based on strategy properties and other players' choices.
- **Fitness functions evaluate overall strategy performance**, optimizing for the best set of strategies.
- **Conflict constraints are checked**, and if a conflict is detected, a penalty is subtracted from the fitness.
- **Final fitness values** are then assigned as objectives to the solution.

### Example evaluate() Method

```java
@Override
public void evaluate(GameSolution solution) {
    double[] payoffs = computePayoffs(solution);
    double fitness = computeFitness(payoffs);

    // Conflict penalty
    if (hasConflict(solution)) {
        fitness -= conflictPenalty;
    }

    solution.setObjective(0, -fitness); // Minimization objective
}
```

## Running a Game Theory Problem

### Execution Workflow
1. **Initialize players and define strategies.**
2. **Assign payoff functions for each player.**
3. **Configure the evolutionary algorithm.**
4. **Run optimization to determine the best strategies.**
5. **Analyze the resulting solutions.**

### Example Execution

```java
// Initialize players and strategies
NormalPlayer player1 = new NormalPlayer("Player1", strategiesList, "p1 + p2");
NormalPlayer player2 = new NormalPlayer("Player2", strategiesList, "p1 - p2");

// Setup game problem
GameProblem problem = new GameProblem(List.of(player1, player2));

// Run optimization
NSGAII algorithm = new NSGAII(problem);
algorithm.run(10000);

// Retrieve results
List<GameSolution> solutions = algorithm.getResult();
```

## Conflict

The `Conflict` class represents a **constraint**, ensuring that two players do **not choose a specific pair of strategies in the same round**. It is not necessarily due to competition.

### Attributes:
- `leftPlayer`: ID of the first player.
- `rightPlayer`: ID of the second player.
- `leftPlayerStrategy`: Strategy index restricted for the first player.
- `rightPlayerStrategy`: Strategy index restricted for the second player.

### Functionality:
- Prevents both players from selecting the specified strategies in the same round.
- The game evaluation checks all conflicts, and if any are violated, a penalty is applied to the solution’s fitness.
- Provides a `toString()` method to display readable conflict descriptions.

### Example:

```java
Conflict conflict = new Conflict(0, 1, 2, 3);
System.out.println(conflict.toString());
// Output: Player: 1, Strategy: 3, Player: 2, Strategy: 4
```

## Strategy

The `Strategy` class defines a strategy with properties and a payoff function.

### Attributes:
- `name`: The name of the strategy.
- `properties`: A list of numerical properties associated with the strategy.
- `payoff`: The computed payoff value for this strategy.

### Functionality:
- Supports adding new properties dynamically.
- `evaluateStringExpression()` method evaluates the payoff function using input expressions and other players' strategies.
- A `toString()` method formats the strategy's details for output.

### Example:

```java
Strategy strategy = new Strategy("Aggressive", Arrays.asList(0.5, 1.2), 0.0);
strategy.addProperty(0.8);
System.out.println(strategy.toString());
// Output: [ 0.5, 1.2, 0.8 ]
```

## MOEA Problem Integration

The system integrates a Multi-Objective Evolutionary Algorithm (MOEA) by defining a `Problem` class that structures the optimization process.

### `newSolution()` Method

Creates a new `GameSolution` object for the evolutionary process. Initializes variables such as:

- **Player strategies**: Randomly assigned at the start.
- **Initial fitness values**: Set to default before evaluation.

```java
@Override
public GameSolution newSolution() {
    GameSolution solution = new GameSolution(players.size());
    for (int i = 0; i < players.size(); i++) {
        solution.setVariable(i, randomStrategy(players.get(i)));
    }
    return solution;
}
```

### `evaluate()` Method

```java
@Override
public void evaluate(GameSolution solution) {
    double[] payoffs = computePayoffs(solution);
    double fitness = computeFitness(payoffs);

    if (hasConflict(solution)) {
        fitness -= conflictPenalty;
    }

    solution.setObjective(0, -fitness);
}
```

## Executor Configuration

The `Executor` class is responsible for configuring and running the evolutionary optimization process.

### Configuration Steps
1. **Instantiate the problem.**
2. **Select an optimization algorithm** (e.g., NSGA-II).
3. **Set algorithm parameters** (e.g., mutation rate, population size).
4. **Run the algorithm for a defined number of generations.**
5. **Extract and analyze solutions.**

### Example:

```java
GameProblem problem = new GameProblem(players);
Algorithm<GameSolution> algorithm = new NSGAII<>(
    problem,
    new SBXCrossover(1.0, 5),
    new PolynomialMutation(1.0 / players.size(), 10.0)
);
Executor<GameSolution> executor = new Executor<>(algorithm);
executor.run();
```

## Conclusion

This implementation provides a robust framework for modeling coordination games using game theory and evolutionary algorithms. Key features include:

- **Modular player and strategy representation**
- **Flexible and customizable payoff evaluation**
- **Optimized solution discovery using MOEA**
