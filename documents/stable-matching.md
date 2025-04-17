# Overview of SMT (Stable Matching)
## 1. Overview

In mathematics, economics, and computer science, the stable marriage problem (also stable matching problem) is the problem of finding a stable matching between two equally sized sets of elements given an ordering of preferences for each element. A matching is a bijection from the elements of one set to the elements of the other set. A matching is not stable if:

1. There is an element A of the first matched set which prefers some given element B of the second matched set over the element to which A is already matched, and
2. B also prefers A over the element to which B is already matched.

In other words, a matching is stable when there does not exist any pair (A, B) which both prefer each other to their current partner under the matching.

_Extracted from [Wikipedia](https://en.wikipedia.org/wiki/Stable_marriage_problem). This problem is conventionally referred as Stable marriage problem._

This problem (Stable Matching Problem) is often illustrated using the example of pairing men and women in a dating scenario, but it has broader applications, including student-internship assignments, hospital-resident placements, and job-market matchmaking.

## Stable Matching Problem's Variants

### Input

A StableMatchingProblemDto object, sent from the front-end via API and converted using @RequestBody. All of the work is automatic, the only thing you have to do is import the source file (`.xlsx`).

```java
@Autowired
private StableMatchingService stableMatchingSolver;

// Running `solve()` with the input being the StableMatchingProblemDto object - the input data sent from the front-end, converted to StableMatchingProblemDto using @RequestBody.

@PostMapping("/stable-matching-solver")
public CompletableFuture<ResponseEntity<Response>> solveStableMatching(
        @RequestBody @Valid StableMatchingProblemDto object) {
    return CompletableFuture.completedFuture(stableMatchingSolver.solve(object));
}
```

### Stable Matching Problem's Types

- One-to-One (OTO) - The classical Gale-Shapley problem: Each entity in the first set can only be matched with exactly one entity in the second set.
    - Example: A group of students is matched with a group of universities, where each student can only enroll in one university.
    - The Gale-Shapley algorithm ensures that no two entities can improve their situation by switching partners.
- One-to-Many (OTM) - An extension of OTO, where one entity can have multiple matches, but each entity in the other set has only one match.
    - Example: A hospital can accept multiple interns, but each intern can only work at one hospital.
    - This requires the algorithm to consider the capacity of each entity during the matching process.
- Many-to-Many (MTM) - Both sets can have multiple matches.
    - Example: Freelancers can work with multiple companies, and each company can hire multiple freelancers.
    - This is a complex extension of the Gale-Shapley algorithm, where preference lists must be managed in both directions.
- Triplet Matching (TripletOTO) - Instead of just two entities, each match involves three entities.
    - Example: In the medical field, a surgery may require a surgeon, an anesthesiologist, and a nurse.
    - Determining stability becomes more complex, as it must be ensured that all three entities are satisfied with their choices.

## 3. Libraries

The API for handling the Stable Matching problem is built using Java with Spring Boot, leveraging powerful technologies to ensure performance, scalability, and maintainability. One of the most important components of the system is the MOEA Framework, which is used to optimize the search for optimal pairing solutions. Below are the main technologies used in the system:

### Spring Boot - API Development Platform

Spring Boot helps build Java applications quickly and easily with less configuration. In this API, Spring is used to handle the logic for the Web Server - building a RESTful API that processes requests from clients.

### MOEA (Multi-Objective Evolutionary Algorithm)

The MOEA Framework is a multi-objective evolutionary optimization library that helps find optimal solutions in complex problems like Stable Matching. Instead of just applying the traditional Gale-Shapley algorithm, the system leverages optimization algorithms to find the most beneficial pairing solutions based on multiple criteria. The MOEA Framework supports:

- Global solution search: Avoiding local minima of the Gale-Shapley algorithm.
- Multi-objective optimization: Balancing multiple factors in matching, such as individual preferences and capacity limits.
- High performance: Handling large-scale problems with complex constraints.

Spring Boot to build the API facilitates rapid development, easy scaling, and maintenance while MOEA Framework helps optimize the pairing process with more efficient solutions compared to traditional matching algorithms. Combined with other supporting libraries, the system ensures high performance and the ability to flexibly handle various variants of the Stable Matching problem.

### Reusing MOEA Framework’s `Problem`

#### Defining the `MatchingProblem` Interface

The `MOEA Framework` provides an abstract `Problem` class that defines the structure for optimization problems. To adapt it for **Stable Matching**, we introduce the `MatchingProblem` interface, which extends `Problem` and serves as a foundation for different matching problem types.

The `MatchingProblem` interface acts as a generic contract for all matching-related problems. It ensures that all derived classes implement core functionalities required for solving the **Stable Matching Problem** using MOEA's optimization techniques.

#### Interface definition

```java
import org.moeaframework.core.Problem;

public interface MatchingProblem extends Problem
```

#### Adding core methods
```java
  /**
   * Get Matching type name.
   */
  String getMatchingTypeName();

  /**
   * Get problem's matching data.
   */
  MatchingData getMatchingData();
```
- `getMatchingTypeName()`: Returns a string representing the type of matching problem (e.g., One-to-One, One-to-Many, Many-to-Many).
- `getMatchingData()`: Provides access to the structured data required for the matching process.

```java
  /**
   * Main matching logic for Stable Matching Problem Types.
   */
  Matches stableMatching(Variable var);

  /**
   * Get all satisfactions of matches result.
   */
  double[] getMatchesSatisfactions(Matches matches);
```
- `stableMatching(Variable var)`: Implements the matching logic for different **Stable Matching** problem types.
- `getMatchesSatisfactions(Matches matches)`: Evaluates and returns an array of satisfaction values for each match.

### Implementing different matching problems

By defining `MatchingProblem`, we allow different types of Stable Matching problems to reuse the same structure while implementing problem-specific logic.

```java
public class OTOProblem implements MatchingProblem {
    // Implements stable matching logic for One-to-One problem(s)
}
```

```java
public class OTMProblem implements MatchingProblem {
    // Implements stable matching logic for One-to-Many problem(s)
}
```

```java
public class MTMProblem implements MatchingProblem {
    // Implements stable matching logic for Many-to-Many problem(s)
}
```

## 4. Core Components of Stable Matching Abstraction

### Individual

In the context of Stable Matching within this system, an `Individual` represents a single entity participating in the matching process. Each individual possesses several key properties that influence the matching outcome. These properties define the characteristics and constraints of each entity.

The `Individual` is not explicitly defined as a class in the traditional sense within the Java code. Instead, its properties are represented using arrays within the `StableMatchingProblemDto`. This approach allows for a more flexible and data-driven representation of individuals.

Here's how the properties of an `Individual` are structured within `StableMatchingProblemDto`:

```java
  @Min(value = 2, message = ErrMessage.MES_001)
  private int numberOfSets;

  @Min(value = 1, message = ErrMessage.MES_003)
  private int numberOfProperty;

  @Size(min = 1, message = ErrMessage.MES_004)
  private int[] individualSetIndices;

  @Size(min = 1, message = ErrMessage.MES_004)
  private int[] individualCapacities;

  @Size(min = 3, message = ErrMessage.MES_002)
  @ValidRequirementSyntax
  private String[][] individualRequirements;

  @Size(min = 3, message = ErrMessage.MES_002)
  private double[][] individualWeights;

  @Size(min = 3, message = ErrMessage.MES_002)
  private double[][] individualProperties;
```

-   **Set indices**: `individualSetIndices` array stores the identifier of each individual within its respective set. This helps in distinguishing between different groups of entities involved in the matching.
-   **Capacity**: `individualCapacities` array defines the maximum number of matches an individual can have. This is particularly relevant for One-to-Many and Many-to-Many matching problems.
-   **PWR (Properties)**: This encompasses three aspects of an individual:
    -   **Value**: Represented by the `individualProperties` array, this indicates the inherent value or suitability of an individual.
    -   **Weight**: Stored in the `individualWeights` array, this denotes the priority or importance of an individual in the matching process.
    -   **Requirement**: Defined in the `individualRequirements` array, this specifies the minimum conditions that must be met for an individual to be considered a valid match.

While the `Individual` is currently represented using arrays for efficiency and flexibility, a future implementation might involve a dedicated `Individual` class to encapsulate these properties more explicitly.

### Preference List

The preference list is a crucial component in Stable Matching as it defines the order in which an entity prefers other entities. This ordering guides the matching algorithm to find stable pairings where individuals are as satisfied as possible with their matches based on their preferences.

In this system, preference lists are created based on the properties of the individuals. The exact mechanism for generating these lists might vary depending on the specific matching problem type and the criteria defined.

The structure of a preference list typically involves a mapping where each entity is associated with an ordered list of other entities, ranked according to its preference.

Here's a code snippet illustrating a basic structure for a `PreferenceList` (For illustrating purposes only):

```java
public class PreferenceList {
    private Map<Integer, List<Integer>> preferences;

    public void addPreference(int entity, List<Integer> orderedPreferences) {
        preferences.put(entity, orderedPreferences);
    }
}
```

This `PreferenceList` class uses a `Map` to store the preferences for each entity. The key of the map is the entity's identifier, and the value is a `List` of identifiers representing the other entities, ordered from most preferred to least preferred.


### SMT Problem

The Stable Matching Problem, as implemented within this system, leverages the MOEA Framework to find optimal solutions. Instead of relying solely on traditional algorithms like Gale-Shapley, this approach uses evolutionary computation to explore a wider range of potential matchings and optimize based on defined objectives.

#### Solution

In the context of the MOEA Framework, a `Solution` represents a single candidate solution to the Stable Matching problem. It's essentially a potential way of matching the entities involved. The `newSolution()` method is responsible for creating these candidate solutions.

Here's a conceptual code snippet illustrating how a new `Solution` is generated:

```java
  @Override
  public Solution newSolution() {
    // A new Solution object is instantiated.
    // The parameters (1, 1) indicate that this solution 
    // has one decision variable and one objective function. 
    Solution solution = new Solution(1, 1);
    Permutation permutationVar = new Permutation(problemSize);
    solution.setVariable(0, permutationVar);
    // The newly created Solution, which encodes a potential matching, 
    // is returned to the MOEA Framework for further processing.
    return solution;
  }
```

Essentially, `newSolution()` acts as a factory for creating different possible matchings that the MOEA algorithm will then try to improve.

#### Evaluation

Once a `Solution` (a potential matching) is generated, the `evaluate()` method is called to assess its quality. This method determines how well the proposed matching satisfies the criteria of the Stable Matching problem, such as stability and the preferences of the entities.

Here's the code snippet for the `evaluate()` method:

```java
  public void evaluate(Solution solution) {
    Matches result = this.stableMatching(solution.getVariable(0));
    // Check Exclude Pairs
    // This section handles constraints on the matching
    int[][] excludedPairs = this.matchingData.getExcludedPairs();
    
    // If any excluded pair is found, the solution is immediately penalized 
    // by setting its objective value to Double.MAX_VALUE — the worst possible score.
    if (Objects.nonNull(excludedPairs)) {
      for (int[] excludedPair : excludedPairs) {
        if (result.isMatched(excludedPair[0], excludedPair[1])) {
          solution.setObjective(0, Double.MAX_VALUE);
          return;
        }
      }
    }
    double[] satisfactions = this.preferenceLists.getMatchesSatisfactions(result, matchingData);
    double fitnessScore;
    if (this.hasFitnessFunc()) {
      fitnessScore = fitnessEvaluator
              .withFitnessFunctionEvaluation(satisfactions, this.fitnessFunction);
    } else {
      fitnessScore = fitnessEvaluator.defaultFitnessEvaluation(satisfactions);
    }
    solution.setAttribute(StableMatchingConst.MATCHES_KEY, result);
    // Finally, the objective value of the `Solution` is set to 
    // the negative of the `fitnessScore`. Most MOEA frameworks aim to 
    // minimize objective values. By negating the fitness score, 
    // we are essentially telling the algorithm to find solutions 
    // that maximize the original fitness (satisfaction and stability).
    solution.setObjective(0, -fitnessScore);
  }
```
In summary, the `evaluate()` method takes a proposed matching, checks for constraint violations, calculates how satisfied the entities are, and assigns an objective value to the solution based on this satisfaction (fitness). This allows the MOEA Framework to compare different potential matchings and iteratively improve them to find the best stable solutions.

#### Applying MOEA

The MOEA Framework orchestrates the process of finding optimal stable matchings by repeatedly using the `newSolution()` method to generate a population of diverse candidate solutions and the `evaluate()` method to assess their quality.

-   The **`newSolution()`** method provides the exploration capability, allowing the algorithm to explore different regions of the solution space (different possible matchings).
-   The **`evaluate()`** method provides the exploitation capability, guiding the algorithm towards better solutions by assigning fitness scores based on the problem's objectives (e.g., maximizing overall satisfaction, minimizing instability) and constraints (e.g., excluding certain pairs).

The MOEA Framework's **`Executor`** is responsible for managing this iterative process. You configure the `Executor` with:

-   The specific MOEA algorithm you want to use (e.g., NSGA-II, SPEA2, which are algorithms designed for multi-objective optimization).
-   The `Problem` definition, which includes the `newSolution()` and `evaluate()` methods that we've discussed.
-   Termination criteria, such as the maximum number of evaluations or generations, to control how long the optimization process runs.

The `Executor` then runs the chosen algorithm, which repeatedly generates new solutions using `newSolution()`, evaluates them using `evaluate()`, and uses evolutionary operators (like crossover and mutation) to create new generations of solutions that are hopefully better than the previous ones. This iterative process continues until the termination criteria are met, and the `Executor` returns a set of non-dominated solutions representing the best stable matchings found.


### Running an SMT Problem

To run a Stable Matching problem that has been defined within the system, you would typically interact with the `StableMatchingService`. This service likely handles the setup of the MOEA optimization process and returns the resulting stable matchings.

Here's a code snippet illustrating how an SMT problem might be executed using the MOEA Framework:

```java
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.algorithm.NSGAII; // Example algorithm

// Bình thường sẽ dùng Mapper để chuyển từ StableMatchingProblemDto sang MatchingProblem với Type
OTOProblem otoProblem = new OTOProblem(/* ... problem-specific parameters ... */);

// Configure and run the MOEA executor
NondominatedPopulation result = new Executor()
        .withProblem(problem)
        .withAlgorithm(new NSGAII(problem, 100 /* population size */, 100 /* number of iterations */))
        .withMaxEvaluations(10000) // Optional termination criteria
        .run();
```

In the context of the provided API endpoint, the `StableMatchingService` likely encapsulates this execution logic. When the `/stable-matching-solver` endpoint is called with a `StableMatchingProblemDto`, the `stableMatchingSolver.solve(object)` method would:

1.  Parse the input data from the `StableMatchingProblemDto`.
2.  Create an instance of the appropriate `MatchingProblem` implementation (e.g., `OTOProblem`, `OTMProblem`).
3.  Configure and run the MOEA `Executor` with the chosen algorithm and problem instance.
4.  Extract the best matching solutions from the resulting nondominated population.
5.  Format the results into a `Response` object.
6.  Return the `Response` to the client.

This abstraction allows the API to handle different types of Stable Matching problems using the power of the MOEA Framework for optimization.
