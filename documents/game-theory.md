# GameTheoryProblem User Guide

## 1. Introduction
`GameTheoryProblem` is a class in the system that models and solves game theory problems. The system supports extensions to create new variations of the problem.

## 2. How to implement a new variation
### 2.1. Create a new class that inherits `GameTheoryProblem`

Create a new class that inherits `GameTheoryProblem` and implements the necessary methods.

```java
public class NewGameTheoryVariant extends GameTheoryProblem {

   public NewGameTheoryVariant() {
      super("NewGameTheoryVariant");
   }

   @Override
   public void solve() {
      // Implement the problem solving algorithm here
   }
}
```

### 2.2. Register the route in `HomeController`
Extend `HomeController` to add a new endpoint for the newly created variant.

```java
@RestController
@RequestMapping("/game-theory")
public class HomeController {

    @GetMapping("/new-variant")
    public ResponseEntity<String> solveNewVariant() {
        GameTheoryProblem problem = new NewGameTheoryVariant();
        problem.solve();
        return ResponseEntity.ok("New game theory variant solved successfully");
    }
}
```

### 2.3. Explanation and integration of `GameTheoryProblemMapper`

#### Role of `GameTheoryProblemMapper`
This class is responsible for converting `GameTheoryProblemDto` into an instance of `GameTheoryProblem`, allowing the system to process input data from the API and create an appropriate problem instance for solving.

It determines which algorithm should be used based on the value in the DTO.

#### How to add a new variation to `GameTheoryProblemMapper`
When creating a new variation (`NewGameTheoryVariant`), you need to extend the logic in `toProblem` to support it.

Example of updating `GameTheoryProblemMapper`:

```java
public class GameTheoryService {
    public static GameTheoryProblem toProblem(GameTheoryProblemDto request) {
        GameTheoryProblem problem;
        String algorithm = request.getAlgorithm();

        if (!StringUtils.isEmptyOrNull(algorithm) && AppConst.PSO_BASED_ALGOS.contains(algorithm)) {
            problem = new PsoCompatibleGameTheoryProblem();
        } else if ("NewGameTheoryVariant".equals(algorithm)) {
            problem = new NewGameTheoryVariant();
        } else {
            problem = new StandardGameTheoryProblem();
        }

        problem.setDefaultPayoffFunction(EvaluatorUtils.getIfDefaultFunction(request.getDefaultPayoffFunction()));
        problem.setFitnessFunction(EvaluatorUtils.getValidFitnessFunction(request.getFitnessFunction()));
        problem.setSpecialPlayer(request.getSpecialPlayer());
        problem.setNormalPlayers(request.getNormalPlayers());
        problem.setConflictSet(request.getConflictSet());
        problem.setMaximizing(request.isMaximizing());

        return problem;
    }
}
```

#### Using `GameTheoryProblemMapper` in `GameTheoryService`
`GameTheoryProblemMapper` is a conversion class that maps a DTO (`GameTheoryProblemDto`) to an appropriate `GameTheoryProblem` instance based on the specified algorithm type in the request. When adding a new variation (`NewGameTheoryVariant`), update the `toProblem` method to check if `request.getAlgorithm()` contains the value "NewGameTheoryVariant". If so, it will initialize and return an instance of `NewGameTheoryVariant`.

When a request is sent to the API, `GameTheoryService` will use `GameTheoryProblemMapper.toProblem(request)` to retrieve the appropriate problem instance.

This ensures that the new variation is handled correctly without requiring changes to `GameTheoryService`.




## 3. Conclusion
By following the steps above, you can extend the system to support new variations of game theory problems. If you need more information, please refer to the system source code.