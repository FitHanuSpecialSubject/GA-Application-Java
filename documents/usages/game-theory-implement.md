# Game Theory Implementation Documentation

## Table of Contents
1. [Overview](#overview)
2. [Game Model](#game-model)
3. [Players and Strategies](#players-and-strategies)
4. [Game Solution](#game-solution)
5. [Evaluation Process](#evaluation-process)
6. [Running a Game Theory Problem](#running-a-game-theory-problem)
7. [Conclusion](#conclusion)

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
- **Default operations** include summation, averaging, and other common statistical computations.

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

## Conclusion
This implementation provides a robust framework for modeling coordination games using game theory and evolutionary algorithms. Key features include:
- **Modular player and strategy representation**
- **Flexible and customizable payoff evaluation**
- **Optimized solution discovery using MOEA**

