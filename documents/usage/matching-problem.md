# Stable Matching Implementation Documentation

## 1. How to implement a new variant
To implement a new variant of `MatchingProblem`, you have to create a New Class Implementing `MatchingProblem`
Your new class should extend `MatchingProblem` and implement all required methods.

Example:
```java
package org.fit.ssapp.ss.smt;

import org.moeaframework.core.Variable;

public class MyMatchingProblem implements MatchingProblem {
    private final MatchingData matchingData;
    
    public MyMatchingProblem(MatchingData data) {
        this.matchingData = data;
    }
    
    @Override
    public String getName() {
        return "MyMatchingProblem";
    }
    
    @Override
    public String getMatchingTypeName() {
        return "Custom Matching";
    }
    
    @Override
    public MatchingData getMatchingData() {
        return matchingData;
    }
    
    @Override
    public Matches stableMatching(Variable var) {
        // Implement the stable matching logic here
        return new Matches();
    }
    
    @Override
    public double[] getMatchesSatisfactions(Matches matches) {
        // Compute satisfactions for the given matches
        return new double[] {1.0};
    }
}
```


## 2. How to register new route to `HomeController` for the new variant
To expose the new matching variant through an API, register it in `HomeController` by adding a new endpoint for the new variant. Here is an example: 

```java
@RestController
@RequestMapping("/matching")
public class HomeController {

    @PostMapping("/custom-matching")
    public ResponseEntity<Matches> solveCustomMatching(@RequestBody @Valid StableMatchingProblemDto dto) {
        MatchingProblem problem = new MyMatchingProblem(dto.toMatchingData());
        Matches result = problem.stableMatching(null);
        return ResponseEntity.ok(result);
    }
}
```


## 3. How to register to a mapper: Convert the Dto into the new varient
To allow conversion from `StableMatchingProblemDto` to the new matching variant, modify the mapper class handling conversions.

### Step 1: Modify `StableMatchingProblemMapper`
Locate the class responsible for mapping DTOs and add support for your new variant.

Example:
```java
public class StableMatchingProblemMapper {
      /**
   * Map from request to problem.
   *
   * @param request StableMatchingProblemDto
   * @return OTMProblem
   */
    public static MyMatchingProblem toMatchingProblem(StableMatchingProblemDto dto) {
        if ("Custom Matching".equals(dto.getProblemName())) {
            return new MyMatchingProblem(dto.toMatchingData());
        }
        throw new IllegalArgumentException("Unsupported matching problem type");
    }
}
```

(Trong ứng dụng đã có `toMatchingData()`, vẫn đang tìm hiểu thêm)

### Step 2: Ensure `StableMatchingProblemDto` Supports Conversion
Modify `StableMatchingProblemDto` to include a helper method to extract `MatchingData`:

```java
public MatchingData toMatchingData() {
    return new MatchingData(
        this.numberOfSets, this.individualSetIndices,
        this.individualCapacities, this.individualRequirements,
        this.individualWeights, this.individualProperties
    );
}
```

