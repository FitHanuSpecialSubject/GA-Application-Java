```txt
com/example/ss2backend/
    ├── aspects/                          # Cross-cutting concerns (e.g., logging)
    ├── config/                           # Application configuration files
    ├── controller/                       # Controllers for handling requests
    │   └── StableMatchingController.java
    ├── dto/                              # Data transfer objects (DTOs)
    │   ├── StableMatchingRequestDTO.java
    │   └── StableMatchingResponseDTO.java
    ├── exception/                        # Custom exceptions for error handling
    │   ├── InvalidPreferenceException.java
    │   └── MatchingNotFoundException.java
    ├── model/                            
    │   ├── gt/                           # Game theory entities
    │   └── smt/                          # Stable matching entities
    │       ├── individual/              
    │       │   ├── Individual.java
    │       │   ├── IndividualList.java
    │       │   └── PreferenceList.java
    │       ├── match/   
    │       │   ├── Matcher.java           # Interface or abstract class for matching algorithms, manage the actual matching process, including handling preferences and matches.
    │       │   ├── Matches.java
    │       │   ├── MatchesOTO.java
    │       │   ├── MatchingSolution.java
    │       │   └── MatchingSolutionInsights.java                 
    │       ├── preference/              
    │       │   ├── Preference.java            # Preference entity
    │       │   ├── PreferenceProvider.java    # Interface or abstract class for providing preferences, centralize the logic for fetching preferences and managing them.
    │       │   └── OldPreferenceProvider.java # Old implementation of preference provider
    │       │   └── NewPreferenceProvider.java # New implementation of preference provider
    │       ├── requirement/              
    │       │   ├── Property.java             # Property entity
    │       │   ├── Requirement.java          # Remain the same
    │       │   └── PropertyRequirement.java  # Requirement of properties
    │       └── problem/                  
    │       │   ├── StableMatchingProblem.java # Abstract base class for matching problems
    │       │   ├── OneToManyProblem.java      # One-to-many problem
    │       │   ├── OneToOneProblem.java       # One-to-one problem
    │       │   └── RBOProblem.java  
    │       └── evaluator/                  # Abstract classes and problem types
    │           ├── Evaluator.java          # Interface for the evaluator
    │           └── impl/                         # Implementations of services
    │               ├── OneToOneEvaluator.java                # One-to-one Evaluator implementing Evaluator
    │               ├── OneToManyMatchingSolver.java       
    │               └── RBOMatchingSolver.java 
    ├── service/                          # Business logic services
    │   ├── GameTheoryService.java        # class for game theory service
    │   ├── StableMatchingService.java    # Interface for stable matching service
    │   └── impl/                         # Implementations of services
    │       ├── OneToOneMatchingSolver.java                # One-to-one solver implementing StableMatchingSolver
    │       ├── OneToManyMatchingSolver.java               # One-to-many solver implementing StableMatchingSolver
    │       └── RBOMatchingSolver.java          
    ├── util/                             # Utility classes for common functionalities        
```
# A Modular Approach: SOLID and MVC Principles in Action

This folder structure follows the principles of **SOLID** and **MVC (Model-View-Controller)** to ensure that each component in the project is modular, organized, and easy to maintain. Here’s a breakdown of how each section fits into the SOLID principles and MVC pattern:

## 1. SOLID Principles

The structure is designed to align with the **SOLID principles**, which promote flexibility and maintainability. Key principles are applied as follows:

- **Single Responsibility Principle (SRP):**
  Each class and package has a clear purpose. For example, DTOs (Data Transfer Objects) are solely responsible for data transfer, while the controller package is dedicated to request handling, and the service package handles business logic. This separation reduces dependencies and promotes focus within each component.

  💡 *Think of each class and package as a specialist in a well-organized team, each performing a unique role efficiently.*

- **Open-Closed Principle (OCP):**
  Many of the core entities, like `Matcher` or `Evaluator`, are interfaces or abstract classes. These classes allow for extensibility, meaning new types of matching algorithms or evaluators can be introduced without modifying the core structure, enhancing flexibility.

  ✨ *It's like adding new tools to your toolkit without changing the basics.*

- **Liskov Substitution Principle (LSP):**
  By defining clear interfaces and base classes for matching and preferences, subclasses can be introduced with confidence that they will operate seamlessly within the existing structure. This approach ensures consistency and compatibility across different implementations.

  🔄 *Imagine being able to swap out a blender for a mixer in your kitchen without any hiccups.*

- **Interface Segregation Principle (ISP):**
  Interfaces, like `Matcher` and `PreferenceProvider`, separate specific functionality into focused contracts. This keeps the implementation classes simple, as they only need to fulfill a single role rather than implement large, catch-all interfaces.

  🔍 *It's about keeping tools sharp and focused, not bogged down with unnecessary features.*

- **Dependency Inversion Principle (DIP):**
  Core functionalities, such as matching or evaluating, depend on abstractions (`Matcher`, `Evaluator`) rather than specific implementations, allowing easy swapping of implementations (e.g., `OneToOneEvaluator`, `OneToManyMatchingSolver`) to suit various problem types.

  🔗 *Think of it as using universal adapters that let you connect various devices effortlessly.*

## 2. MVC Pattern

While primarily following SOLID, this structure also reflects **MVC principles** to create a cohesive application with clear separation between data, business logic, and user interaction.

- **Model (model/ package):**
  The model package contains entities (like `Individual`, `Preference`, `Requirement`) that represent the core data structure of the application. These are the classes that make up the "Model" in MVC and reflect the domain logic required for stable matching and game theory.

  🗂️ *It's like having a well-organized pantry where each ingredient is in its designated place.*

    - **Sub-packages:**
        - **gt/**: Focuses on game theory-specific entities.
        - **smt/**: Houses classes involved in stable matching.
        - **evaluator/**: Organizes evaluators for assessing solutions.
        - **individual/**: Contains classes like `Individual` and `PreferenceList`.
        - **match/**: Contains classes like `Matcher` and `Matches`.
        - **preference/**: Handles preference-related logic.
        - **requirement/**: Represents properties and requirements.
        - **problem/**: Defines different problem types.
        - **evaluator/**: Abstracts and implementations for evaluators.

- **View:**
  In the context of this backend service, there isn’t a direct "View" layer like in typical MVC web applications. Instead, the controller layer (discussed below) manages HTTP request handling, effectively serving as the "View" by exposing endpoints that interact with the outside world.

  🌐 *Think of it as the front desk in a hotel, welcoming and directing guests.*

- **Controller (controller/ package):**
  The controller package represents the Controller in the MVC architecture. It’s where HTTP requests are handled, taking inputs from clients and interacting with service components.

  🎛️ *Controllers are like skilled receptionists who manage requests efficiently.*

    - **Controllers like `StableMatchingController.java` handle:**
        - Request validation
        - Response formatting
        - Passing data to the appropriate service for processing

- **Service (service/ package):**
  The service package encapsulates business logic and acts as the intermediary between controllers and models. It contains core logic and orchestrates the interactions between entities, ensuring that controllers remain lightweight and focused solely on request handling.

  🔧 *Services are the chefs in the kitchen, turning raw ingredients into a delicious meal.*

    - **Services like `StableMatchingService` implement:**
        - Matching algorithms
        - Specifics of stable matching scenarios

## 3. Detailed Explanation of Each Package

- **aspects/:**
  Cross-cutting concerns like logging, exception handling, or performance tracking are centralized here, ensuring that such logic is reusable across the application. 

  📊 *It's like having a central station for all the behind-the-scenes operations.*

- **config/:**
  Contains application configurations, such as database settings, API configurations, or application properties. 

  ⚙️ *Think of it as the settings menu where you can tweak configurations without disrupting the entire system.*

- **controller/:**
  Houses controllers that handle incoming HTTP requests and pass data to the appropriate services.

  🎛️ *Controllers are the smooth operators managing the flow of requests.*

- **dto/:**
  DTOs represent data that will be transferred between layers.

  📦 *DTOs are the messengers, carrying data cleanly between layers.*

- **exception/:**
  Centralizes custom exceptions to manage error handling consistently. 

  🚨 *It's the emergency response team, ready to handle errors with precision.*

- **model/:**
  This package contains the domain models, which represent core data structures. Sub-packages organize the different model categories:

  🗂️ *Models are like the ingredients in your kitchen, each with a specific role to play.*

    - **gt/**: Focuses on game theory-specific entities.
    - **smt/**: Houses classes involved in stable matching.
      - **individual/**: Contains classes like `Individual` and `PreferenceList`.
      - **match/**: Contains classes like `Matcher` and `Matches`.
      - **preference/**: Handles preference-related logic.
      - **requirement/**: Represents properties and requirements.
      - **problem/**: Defines different problem types.
      - **evaluator/**: Abstracts and implementations for evaluators.

- **service/:**
  Services contain the business logic, performing computations, interacting with models, and handling complex operations. By splitting implementation classes into `impl/`, it’s easier to swap or modify specific services without altering the underlying interface.

  🔧 *Services are the backbone, executing the core operations seamlessly.*

- **util/:**
  Stores utility classes for common functionalities, making them reusable across different parts of the application, which is essential for reducing code duplication.

  🛠️ *Util classes are the handy tools, ready to be used wherever needed.*
