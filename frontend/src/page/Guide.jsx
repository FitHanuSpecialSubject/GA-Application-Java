import React from 'react';
import '../module/core/asset/css/guide.scss';
import {useRef, useContext, useEffect, useState} from 'react';
import DataContext from '../module/core/context/DataContext';
import MathConversionImage
  from '../module/core/asset/image/math-conversion.png';

export default function GuidePage() {
  const gettingStartedRef = useRef();
  const problemNameRef = useRef();
  const specialPlayerExistsRef = useRef();
  const specialPlayerPropsNumRef = useRef();
  const normalPlayerNumRef = useRef();
  const normalPlayerPropsNumRef = useRef();
  const fitnessFunctionRef = useRef();
  const payoffFunctionRef = useRef();
  const maxminRef = useRef();
  const inputToExcelRef = useRef();
  const aboutSMRef = useRef();
  const problemSMRef = useRef();
  const backendSMRef = useRef();
  const outputSMRef = useRef();

  const refArray = [
    gettingStartedRef,
    problemNameRef,
    specialPlayerExistsRef,
    specialPlayerPropsNumRef,
    normalPlayerNumRef,
    normalPlayerPropsNumRef,
    fitnessFunctionRef,
    payoffFunctionRef,
    maxminRef,
    inputToExcelRef,
    aboutSMRef,
    problemSMRef,
    backendSMRef,
    outputSMRef];

  const {guideSectionIndex, setGuideSectionIndex} = useContext(DataContext);

  const [guideFor, setGuideFor] = useState('gt');

  function handleSelect(e) {
    const val = e.target.value;
    setGuideFor(val);
  }

  // setup observer for observing the scrolling of the page
  useEffect(() => {
    const options = {
      root: null, rootMargin: '150px', threshold: 1,
    };

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          setGuideSectionIndex(Number(entry.target.id));
        }
      });
    }, options);

    observer.observe(gettingStartedRef.current);
    observer.observe(problemNameRef.current);
    observer.observe(specialPlayerExistsRef.current);
    observer.observe(specialPlayerPropsNumRef.current);
    observer.observe(normalPlayerNumRef.current);
    observer.observe(normalPlayerPropsNumRef.current);
    observer.observe(fitnessFunctionRef.current);
    observer.observe(payoffFunctionRef.current);
    observer.observe(inputToExcelRef.current);
    observer.observe(aboutSMRef.current);
    observer.observe(problemSMRef.current);
    observer.observe(backendSMRef.current);
    observer.observe(outputSMRef.current);
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    const ref = refArray[guideSectionIndex];
    scrollTo(ref);
  }, []);

  function scrollTo(sectionRef) {
    const index = refArray.indexOf(sectionRef);
    setGuideSectionIndex(index);
    sectionRef.current.scrollIntoView({
      behavior: 'instant', block: 'start',
    });
  }

  function checkIfHightlight(index) {
    if (index == guideSectionIndex) {
      return 'highlight';
    } else {
      return '';
    }
  }

  return (<div className="guide-page">
    <div className="sidebar">
      <select
          className="form-select"
          onChange={handleSelect}
          aria-label="Default select example"
      >
        <option selected value="gt">
          Game Theory
        </option>
        <option value="sm">Stable Matching</option>
      </select>
      <div
          className={`sidebar__item__title ${guideFor == 'gt'
              ? 'd-block'
              : 'd-none'}     ${checkIfHightlight(0)}`}
          onClick={() => scrollTo(gettingStartedRef)}
      >
        Getting Started
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'gt'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(1)}`}
          onClick={() => scrollTo(problemNameRef)}
      >
        Name of the problem
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'gt'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(2)}`}
          onClick={() => scrollTo(specialPlayerExistsRef)}
      >
        Special Player exists
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'gt'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(3)}`}
          onClick={() => scrollTo(specialPlayerPropsNumRef)}
      >
        Number of properties of special player
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'gt'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(4)}`}
          onClick={() => scrollTo(normalPlayerNumRef)}
      >
        Number of normal players
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'gt'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(5)}`}
          onClick={() => scrollTo(normalPlayerPropsNumRef)}
      >
        Number of properties of each normal player
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'gt'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(6)}`}
          onClick={() => scrollTo(fitnessFunctionRef)}
      >
        Fitness function
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'gt'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(7)}`}
          onClick={() => scrollTo(payoffFunctionRef)}
      >
        Player payoff function
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'gt'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(8)}`}
          onClick={() => scrollTo(payoffFunctionRef)}
      >
        Maximizing and minimizing
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'gt'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(9)}`}
          onClick={() => scrollTo(inputToExcelRef)}
      >
        Input to Excel
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'sm'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(10)}`}
          onClick={() => scrollTo(aboutSMRef)}
      >
        About Stable Matching
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'sm'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(11)}`}
          onClick={() => scrollTo(problemSMRef)}
      >
        Problem properties
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'sm'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(12)}`}
          onClick={() => scrollTo(backendSMRef)}
      >
        Back-end Endpoints
      </div>
      <div
          className={`sidebar__item__title ${guideFor == 'sm'
              ? 'd-block'
              : 'd-none'} ${checkIfHightlight(13)}`}
          onClick={() => scrollTo(outputSMRef)}
      >
        Outputs
      </div>
    </div>

    <div className="content">
      <section
          className={`section ${guideFor == 'gt' ? 'd-block' : 'd-none'}`}
          ref={gettingStartedRef}
          id="0"
      >
        <h1>Getting started</h1>
        <p>
          Game theory is a branch of mathematics that studies the
          interactions between rational decision-makers, often
          modeled as players in a game. The goal of game theory is
          to understand how people make decisions in strategic
          situations where the outcome of a decision depends on
          the actions of others.
        </p>
        <p>
          Game theory provides a framework for analyzing and
          modeling strategic interactions in a wide range of
          fields, including economics, political science,
          psychology, sociology, biology, and computer science.
          The insights gained from game theory can be applied to
          many different scenarios, such as business competition,
          international relations, bargaining, auctions, voting,
          and social dilemmas.
        </p>
        <p>
          One of the central concepts in game theory is the notion
          of a Nash equilibrium, which is a set of strategies
          where no player can benefit by changing their strategy,
          assuming that all other players keep their strategies
          unchanged. Game theory also encompasses various solution
          concepts, such as dominant strategies, Pareto
          optimality, and correlated equilibrium, which provide
          different ways of analyzing the outcomes of strategic
          interactions
        </p>
        <p>
          The MOEA framework is an optimization algorithm that can
          be used to find Nash equilibria in games. MOEA works by
          exploring the space of all possible strategies for each
          player and finding the set of strategies that constitute
          a Nash equilibrium. This is done by defining an
          objective function that captures the payoff for each
          player based on their strategy choices. The MOEA
          algorithm then optimizes this objective function to find
          the Nash equilibrium.
        </p>
      </section>
      <section
          className={`section ${guideFor == 'gt' ? 'd-block' : 'd-none'}`}
          ref={problemNameRef}
          id="1"
      >
        <h1>Name of the problem</h1>
        <p>
          This section requires the user to provide a name for the
          problem they are trying to solve using the MOEA
          framework. The name should be concise and meaningful,
          reflecting the nature of the game being analyzed. For
          example, if the game is about two competing companies
          deciding whether to enter a new market, the name could
          be "Market Entry Game." Providing a name for the problem
          helps keep track of different games that are being
          analyzed and facilitates sharing and collaboration.
        </p>
      </section>
      <section
          className={`section ${guideFor == 'gt' ? 'd-block' : 'd-none'}`}
          ref={specialPlayerExistsRef}
          id="2"
      >
        <h1>Special Player exists.</h1>
        <p>
          The checkbox in the UI labeled "Special Player" is an
          option for users to indicate whether or not the game
          being analyzed involves a player with unique
          characteristics or advantages not present in the other
          players. If this option is selected, the user will be
          prompted to input the number of properties that the
          special player possesses.
        </p>
        <p>
          In game theory, a special player is a player who is
          different from the other players in the game. This
          player is considered special because they may have
          different objectives, different strategies, or different
          payoffs compared to the other players.
        </p>
        <p>
          The concept of a special player is commonly used in many
          game theory models, such as in asymmetric games, where
          one player has more information or resources than the
          other players. Special players can also be used to model
          scenarios where one player has a unique advantage, such
          as a monopoly or a government agency with regulatory
          power.
        </p>
        <p>
          One example of a special player in game theory is the
          leader in a Stackelberg competition model. In this
          model, one firm acts as a leader and sets their output
          quantity first, and the other firms act as followers and
          choose their output quantities after observing the
          leader's choice. The leader has a unique advantage in
          this scenario, as they can anticipate the followers'
          responses and make decisions accordingly.
        </p>
        <p>
          Another example of a special player is a player with a
          different set of objectives. For instance, in a
          two-player game where one player is a manufacturer and
          the other is a retailer, the manufacturer may be
          interested in maximizing profits, while the retailer may
          be interested in minimizing inventory costs. In this
          case, the two players have different objectives and can
          be considered special players.
        </p>
        <p>
          Overall, the concept of a special player in game theory
          allows for a more nuanced understanding of different
          types of players in a game, and can be used to model
          various real-world scenarios.
        </p>
      </section>
      <section
          className={`section ${guideFor == 'gt' ? 'd-block' : 'd-none'}`}
          ref={specialPlayerPropsNumRef}
          id="3"
      >
        <h1>Number of properties of special player.</h1>
        <p>
          If the user has selected the checkbox indicating the
          presence of a special player in the game, the next step
          is to input the number of properties possessed by this
          player. This field is a numerical input where the user
          should enter a positive integer value that corresponds
          to the number of properties that the special player has.
        </p>
        <p>
          The number of properties possessed by a special player
          is an important aspect of the game that affects its
          dynamics and outcomes. In some games, the special player
          may have a greater influence on the game than other
          players, and hence their properties could be more
          significant. In other games, the special player may have
          a more limited role, and hence their properties may be
          less significant.
        </p>
        <p>
          In the context of Nash equilibrium and MOEA framework,
          the number of properties possessed by a special player
          is used to define the game's parameters and constraints.
          By inputting this value, the user provides crucial
          information that allows the algorithm to search for Nash
          equilibria more effectively.
        </p>
        <p>
          For example, consider a game where there is a special
          player who has three properties, and four normal players
          who each have two properties. The algorithm will use
          this information to generate a set of strategies and
          payoffs that satisfy the game's constraints and find the
          Nash equilibrium. Without this input, the algorithm
          would not be able to consider the special player's
          properties, and the Nash equilibrium may not be accurate
          or feasible.
        </p>
      </section>
      <section
          className={`section ${guideFor == 'gt' ? 'd-block' : 'd-none'}`}
          ref={normalPlayerNumRef}
          id="4"
      >
        <h1>Number of normal players.</h1>
        <p>
          Normal players are the participants in a game theory
          problem who do not have any special advantages or
          properties. They are typically considered as equals in
          terms of decision-making power and available resources.
          When inputting the number of normal players in the game,
          it is important to accurately reflect the number of
          players involved to ensure that the resulting Nash
          equilibrium is valid for the scenario at hand.
        </p>
        <p>
          The input field for the number of normal players should
          be a numeric field that only accepts positive integers.
          The field should have a clear label indicating what it
          is asking for, such as "Number of normal players." It is
          also important to provide some context or guidance for
          the user on how to determine the appropriate number to
          input. This could be done through the use of placeholder
          text, a tooltip, or a brief description of the game
          scenario in the surrounding text.
        </p>
        <p>
          For example, if the game theory problem involves a group
          of individuals deciding whether to cooperate or compete
          for a limited resource, the number of normal players
          might represent the number of individuals involved in
          the decision-making process. If the problem involves
          multiple rounds or iterations of the game, the number of
          normal players might represent the number of
          participants in each round.
        </p>
        <p>
          Overall, accurately inputting the number of normal
          players is an important step in finding the Nash
          equilibrium for a game theory problem. By providing a
          clear input field and guidance for the user, you can
          ensure that the resulting equilibrium is meaningful and
          relevant for the specific scenario being analyzed.
        </p>
      </section>
      <section
          className={`section ${guideFor == 'gt' ? 'd-block' : 'd-none'}`}
          ref={normalPlayerPropsNumRef}
          id="5"
      >
        <h1>Number of properties of each normal player.</h1>
        <p>
          The next input is the number of properties each normal
          player possesses. A property in game theory refers to
          any characteristic or attribute that a player has, which
          affects their decisions and outcomes in the game. For
          example, in a game of chess, a player's properties may
          include the location of their pieces, the number of
          pieces they have, and their overall strategy.{' '}
        </p>
        <p>
          It's important to note that all normal players should
          have the same number of properties, as it simplifies the
          calculation of the game's Nash equilibria. If the number
          of properties differs between players, it can complicate
          the game and make it more difficult to find a Nash
          equilibrium.{' '}
        </p>
        <p>
          The number of properties each normal player has can vary
          depending on the game. For example, in a game of chess,
          each player has 16 pieces with different properties such
          as movement patterns and point values. In a game of
          monopoly, each player has a different set of properties
          such as properties, houses, and hotels.
        </p>
      </section>
      <section
          className={`section ${guideFor == 'gt' ? 'd-block' : 'd-none'}`}
          ref={fitnessFunctionRef}
          id="6"
      >
        <h1>Fitness function.</h1>
        <p>
          The fitness function is a crucial component of any game
          theory problem, as it determines the success of a
          player's strategy in a given game. In simple terms, the
          fitness function represents the payoff that a player
          receives for a specific combination of strategies played
          by all the players in the game.
        </p>
        <p>
          The fitness function is a mathematical function that
          evaluates how well a player's strategy performs in the
          game. It is used to determine the relative fitness or
          success of a strategy in relation to other strategies
          used by other players in the game. The fitness function
          is defined based on the payoff matrix of the game, which
          outlines the outcomes and associated payoffs for each
          possible combination of player strategies.
        </p>
        <p>
          The fitness function can take on various forms depending
          on the type of game being analyzed. For example, in a
          two-player zero-sum game, the fitness function is
          typically represented by a single scalar value
          representing the payoff to one player, with the payoff
          to the other player being the negative of this value. In
          contrast, in a cooperative game, the fitness function
          represents the collective payoff of all the players, and
          may be more complex to calculate.
        </p>
        <p>
          In the context of the MOEA framework, the fitness
          function is often formulated as an optimization problem,
          where the goal is to find the set of strategies that
          maximize the expected payoff of a player or group of
          players. This requires the use of mathematical tools
          such as optimization algorithms and numerical methods.
        </p>
        <p>
          It is important to note that the fitness function is
          specific to the game being analyzed and must be
          carefully designed to reflect the objectives and
          constraints of the problem. A poorly designed fitness
          function can lead to incorrect or suboptimal solutions,
          so it is crucial to spend time understanding the game
          and its underlying dynamics before formulating the
          fitness function.
        </p>
        <p>
          Fitness function need to be converted from mathematical
          formula into a form of a string expression that can be
          parsed by the app.{' '}
        </p>
        <p>
          Note: It's important to note that the string expression
          must be correctly formatted with respect to order of
          operations, parentheses, and other mathematical
          conventions. Any mistakes in formatting could lead to
          incorrect results when the app processes the fitness
          function.
        </p>
        <div className="gray-board">
          <img src={MathConversionImage} alt=""/>
        </div>
      </section>
      <section
          className={`section ${guideFor == 'gt' ? 'd-block' : 'd-none'}`}
          ref={payoffFunctionRef}
          id="7"
      >
        <h1>Player payoff function.</h1>
        <p>
          The player payoff function represents the benefit or
          utility that a player can get from a certain strategy or
          set of strategies in the game. In game theory, the
          payoff function is typically represented as a table or
          matrix where each row corresponds to a strategy of one
          player and each column corresponds to a strategy of the
          other player.
        </p>
        <p>
          It is important to note that the payoff function should
          satisfy the rationality assumption of the players. That
          is, each player will choose the strategy that maximizes
          their expected payoff given the other player's strategy.
          This is the basis for finding the Nash equilibrium of
          the game.
        </p>
        <p>
          The payoff function can be a function of the properties
          of the players, the strategies chosen by the players, or
          both. In this case, the MOEA framework will construct
          the payoff function using the number of properties for
          each normal player and the existence of a special
          player. The framework will ensure that the payoff
          function takes into account the properties of each
          player when calculating the payoff.
        </p>
        <p>
          For example, if there are two properties for each
          player, the payoff function should be input such that it
          takes into account the two properties of each normal
          player. This ensures that the payoff function accurately
          reflects the game being played and provides an
          appropriate measure of the players' success.
        </p>
        <p>
          In the context of game theory, a payoff function is a
          mathematical formula that calculates the payoffs for
          each player in a game, based on the strategies they
          choose. To use a payoff function in an app, the user
          needs to input the formula as a string expression.
        </p>
        <div className="gray-board">
          <img src={MathConversionImage} alt=""/>
        </div>
      </section>

      <section
          className={`section ${guideFor == 'gt' ? 'd-block' : 'd-none'}`}
          ref={maxminRef}
          id="8"
      >
        <h1>Maximizing and minimizing.</h1>
        <p>
          In game theory, maximizing refers to the strategy of
          attempting to achieve the best possible outcome or
          highest payoff in a game. This means that a player will
          try to make decisions and take actions that lead to the
          most favorable outcome for themselves, even if this
          means that other players may not benefit as much or may
          even suffer negative consequences.
        </p>
        <p>
          On the other hand, minimizing refers to the strategy of
          trying to achieve the least possible negative outcome or
          lowest payoff in a game. This means that a player will
          try to avoid making decisions and taking actions that
          could lead to a negative outcome for themselves, even if
          this means that they may not receive the most favorable
          outcome.
        </p>
        <p>
          One example of maximizing in game theory is in a game of
          poker. In poker, a player may make decisions that
          maximize their potential to win the hand and earn the
          highest possible payoff. This may involve taking risks,
          such as betting a large sum of money, in order to
          increase the potential payoff.
        </p>
        <p>
          On the other hand, an example of minimizing in game
          theory is in a game of chicken. In this game, two
          drivers are racing towards each other and the first to
          swerve to avoid a collision loses. In this situation, a
          driver may be motivated to minimize their potential for
          negative outcomes, such as crashing or being injured, by
          swerving before the collision occurs.
        </p>
        <p>
          Note: If a game is maximizing, the payoff function
          should be negative,
        </p>
      </section>
      <section
          className={`section ${guideFor == 'gt' ? 'd-block' : 'd-none'}`}
          ref={inputToExcelRef}
          id="9"
      >
        <h1>Input to Excel</h1>
        <div className="steps">
          <b>Input problem data</b>
          <ol>
            <li>
              Fill out the necessary information about the
              problem in the "Problem Information" sheet,
              including the problem name, whether a special
              player exists, the number of properties of the
              special player, the number of normal players,
              the number of properties of each normal player,
              the fitness function, and the player payoff
              function.
            </li>
            <li>
              If a special player exists, fill out the data of
              the properties and weights of the special player
              in the "Special Player" sheet.
            </li>
            <li>
              Fill out all the necessary data about normal
              players, including name, number of strategies,
              and property data of each strategy, in the
              "Normal Players" sheet.
            </li>
            <li>
              If necessary, conflict set of strategies to the
              sheet "Conflict Set".
            </li>
            <li>Save the Excel file.</li>
          </ol>
        </div>

        <div className="gray-board">
          <p>Try it here</p>
        </div>
        <div className="steps">
          <b>Process the Excel file</b>
          <ol>
            <li>
              Open the drag and drop area in the application.
            </li>
            <li>
              Drag and drop the Excel file into the designated
              area.
            </li>
            <li>
              Wait for the application to process the file.
            </li>
            <li>
              Review the game theory problem information and
              data in the application.
            </li>
          </ol>
        </div>
      </section>
      <section
          className={`section ${guideFor == 'sm' ? 'd-block' : 'd-none'}`}
          ref={aboutSMRef}
          id="10"
      >
        <h1>About Stable Matching</h1>
        <p>Matching theory explores the optimal assignment of
          individuals to partners or objects based on
          preferences and constraints. It has applications in various fields
          such as economics, computer
          science, and sociology. Stable matching and stable marriage are
          central concepts within matching
          theory, aiming to find allocations that are both efficient and
          stable.</p>

        <h2>Stable Matching</h2>
        <p>Stable matching seeks to find allocations where no pair of agents has
          an incentive to deviate from
          their assigned partners. In a stable matching, there are no “blocking
          pairs,” where two agents
          prefer each other over their current partners. The Gale-Shapley
          algorithm, also known as the
          deferred acceptance algorithm, is a classic method for finding stable
          matchings in bipartite graphs,
          demonstrating the effectiveness of this approach.</p>

        <h2>Stable Marriage</h2>
        <p>Stable marriage is a specific application of stable matching
          involving two sets of agents (typically
          men and women) seeking to form mutually acceptable pairs. In the
          stable marriage problem, each agent
          has preferences over members of the opposite sex, and the goal is to
          find a stable matching where no
          pair of agents prefers each other to their assigned partners. The
          concept of stable marriage has
          implications in real-world scenarios such as matching medical
          residents to hospitals or students to
          schools.</p>

        <h2>Algorithmic Solution</h2>
        <p>Various algorithms have been developed to solve stable matching and
          stable marriage problems
          efficiently. In addition to the Gale-Shapley algorithm, other methods
          such as the Top-Trading-Cycles
          algorithm and the Roth-Peranson algorithm offer alternative approaches
          for finding stable
          allocations in different contexts. These algorithms leverage
          mathematical properties and
          optimization techniques to compute stable matchings effectively.</p>

        <h2>Applications</h2>
        <p>Stable matching and stable marriage have practical applications in
          fields such as labor markets,
          school choice mechanisms, and kidney exchange programs. By ensuring
          stable and efficient
          allocations, matching theory contributes to better resource
          allocation, reduced inefficiencies, and
          improved social welfare in various domains. Research papers in
          economics, operations research, and
          computer science provide insights into the design and implementation
          of matching mechanisms in
          real-world settings.</p>
        <h3>NSGA-II (Nondominated Sorting Genetic Algorithm II)</h3>
        <p>NSGA-II is a well-known genetic algorithm for multi-objective
          optimization problems. The key logic of
          NSGA-II includes:</p>
        <ul>
          <li><strong>Nondominated sorting:</strong> Individuals in a population
            are sorted into Pareto fronts
            based on dominance. Individuals in the first Pareto front are
            non-dominated solutions.
          </li>
          <li><strong>Crowding distance calculation:</strong> It uses a crowding
            distance measure to ensure
            diversity in the objective space.
          </li>
          <li><strong>Selection of individuals:</strong> Genetic operations
            (selection, crossover, mutation)
            are applied to generate new generations, ensuring the population
            maintains diversity while
            searching for better solutions.
          </li>
        </ul>
        <h2>MOEA Algorithm</h2>
        <h3>NSGA-II (Nondominated Sorting Genetic Algorithm II)</h3>
        <p>NSGA-II is a well-known genetic algorithm for multi-objective
          optimization problems. The key logic of
          NSGA-II includes:</p>
        <ul>
          <li><strong>Nondominated sorting:</strong> Individuals in a population
            are sorted into Pareto fronts
            based on dominance. Individuals in the first Pareto front are
            non-dominated solutions.
          </li>
          <li><strong>Crowding distance calculation:</strong> It uses a crowding
            distance measure to ensure
            diversity in the objective space.
          </li>
          <li><strong>Selection of individuals:</strong> Genetic operations
            (selection, crossover, mutation)
            are applied to generate new generations, ensuring the population
            maintains diversity while
            searching for better solutions.
          </li>
        </ul>
        <p>NSGA-II focuses on optimizing multiple objectives simultaneously,
          which can be applied to Stable
          Matching when there are multiple criteria for ranking partners, such
          as priority levels, personal
          preferences, and other factors. NSGA-II can help find optimal matches
          based on competing
          objectives.</p>

        <h3>NSGA-III (Nondominated Sorting Genetic Algorithm III)</h3>
        <p>NSGA-III is an improved version of NSGA-II, designed to handle
          optimization problems with many
          objectives (high-dimensional objectives). Key aspects of NSGA-III
          include:</p>
        <ul>
          <li><strong>Reference-point based approach:</strong> Instead of using
            crowding distance like
            NSGA-II, NSGA-III uses reference points to distribute individuals in
            the objective space, making
            it more effective for problems with many objectives.
          </li>
          <li><strong>Better handling of many-objective
            problems:</strong> NSGA-III improves accuracy in
            finding the Pareto front in spaces with many dimensions.
          </li>
        </ul>
        <p>In the context of stable matching, NSGA-III can help analyze matching
          options across multiple
          criteria simultaneously, offering more optimal solutions in a space
          with competing objectives.</p>

        <h3>eMOEA (Elitist Multi-Objective Evolutionary Algorithm)</h3>
        <p>eMOEA is a multi-objective evolutionary algorithm that emphasizes
          elitism, focusing on retaining the
          best individuals across generations. This ensures that the best
          solutions from each generation are
          preserved while exploring new areas of the search space.</p>
        <ul>
          <li><strong>Elitism:</strong> The best solutions from each generation
            are carried over to the next.
          </li>
          <li><strong>Diversity and exploration:</strong> The algorithm uses
            techniques like mutation and
            crossover to explore different regions of the search space.
          </li>
        </ul>
        <p>In a matching problem, eMOEA could be useful when optimizing multiple
          ranking criteria for
          participants, while ensuring that the best solutions are preserved and
          improved across
          generations.</p>

        <h3>PESA2 (Pareto Envelope-based Selection Algorithm)</h3>
        <p>PESA2 is a multi-objective optimization algorithm that uses selection
          based on a Pareto envelope. Its
          key features include:</p>
        <ul>
          <li><strong>Pareto-based selection:</strong> Solutions are selected
            based on their coverage of the
            Pareto front.
          </li>
          <li><strong>Multi-objective analysis:</strong> The algorithm focuses
            on maintaining diversity in the
            population and exploiting solutions near the Pareto frontier.
          </li>
        </ul>
        <p>PESA2 can be used to find stable matching solutions with multiple
          ranking objectives while ensuring
          broad distribution of solutions along the Pareto front.</p>

        <h3>VEGA (Vector Evaluated Genetic Algorithm)</h3>
        <p>VEGA is one of the earliest multi-objective genetic algorithms. It
          processes each objective
          separately in each generation:</p>
        <ul>
          <li><strong>Subdivision into subpopulations:</strong> VEGA splits the
            population into subgroups and
            optimizes each group for a specific objective.
          </li>
          <li><strong>Does not generate proper Pareto fronts:</strong> Since
            VEGA optimizes individual
            objectives instead of simultaneously, it often does not provide a
            proper Pareto front but can
            generate diversity in the population.
          </li>
        </ul>
        <p>VEGA could be applied to stable matching problems with multiple
          objectives, but it may not be optimal
          when simultaneous analysis of all objectives is required.</p>
      </section>
      <section
          className={`section ${guideFor == 'sm' ? 'd-block' : 'd-none'}`}
          ref={problemSMRef}
          id="11"
      >
        <h1>Problem properties</h1>
        <ol>
          <li>
            <strong>Problem Name</strong>
            <p><strong>Input Cell:</strong> Enter a concise and meaningful name
              for the problem you are
              addressing.</p>
            <p><em>Note:</em> The name should reflect the content of the game or
              analysis problem.</p>
          </li>
          <li>
            <strong>Number of Sets</strong>
            <p><strong>Input Cell:</strong> Enter a positive integer, e.g., 2,
              3, etc.</p>
            <p><em>Explanation:</em> This is the number of sets you will
              analyze. The system will
              display a corresponding table after you enter this number.</p>
          </li>
          <li>
            <strong>Number of Characteristics</strong>
            <p><strong>Input Cell:</strong> Enter the number of characteristics
              for each set, e.g., 3,
              5, etc.</p>
            <p><em>Explanation:</em> Each characteristic represents a
              requirement or attribute that an
              individual has, affecting their weight in the pairing process.</p>
          </li>
          <li>
            <strong>Total Individuals</strong>
            <p><strong>Input Cell:</strong> Enter the total number of
              individuals in each set, e.g., 10,
              15, etc.</p>
            <p><em>Explanation:</em> This is the total number of individuals in
              each set that you wish
              to analyze.</p>
          </li>
          <li>
            <strong>Fitness Function</strong>
            <p><strong>Input Cell:</strong> Enter the formula for the fitness
              function, e.g., f(x, y) =
              x + 2y.</p>
            <p><em>Explanation:</em> The fitness function is a mathematical
              expression representing the
              benefit that a player receives for a specific strategy
              combination.</p>
          </li>
          <li>
            <strong>Information for Each Set</strong>
            <p><strong>Input Cells:</strong> Enter detailed information for each
              set in the
              corresponding cells.</p>
            <ul>
              <li>"Num individuals of Set_x": Enter the number of individuals in
                the corresponding set
                (Set_1, Set_2, etc.).
              </li>
              <li>"Evaluate Function Set_x": Enter the evaluation function
                corresponding to each
                set.
              </li>
              <li>"Tick if Set_2 is Many":
                <p><strong>Input Cell:</strong> If Set_2 contains many
                  individuals (for example,
                  more than 5 or 10), check this box.</p>
                <p><em>Explanation:</em> This information will help determine
                  how to handle this set
                  in the analysis.</p>
              </li>
            </ul>
            <p><strong>Example:</strong></p>
            <p>If you have 2 sets:</p>
            <p>Set 1:</p>
            <ul>
              <li>Num individuals of Set_1: 5</li>
              <li>Evaluate Function Set_1: g1(x) = x^2</li>
            </ul>
            <p>Set 2:</p>
            <ul>
              <li>Num individuals of Set_2: 10</li>
              <li>Evaluate Function Set_2: g2(y) = 2y + 1</li>
              <li>Tick if Set_2 is Many: ✔ (checked)</li>
            </ul>
          </li>
          <li>
            <strong>Data Validation</strong>
            <p><em>Before Saving:</em> Ensure that all cells are filled
              correctly and no cell is left
              empty.</p>
            <p><em>Formatting Note:</em> Make sure you enter the correct data
              type (integer, string,
              formula).</p>
          </li>
        </ol>

      </section>
      <section
          className={`section ${guideFor == 'sm' ? 'd-block' : 'd-none'}`}
          ref={backendSMRef}
          id="12"
      >
        <h1>About backend</h1>
        <h2>Logic</h2>
        <p>After receiving your problem's data, the backend server will create
          the following MOEA problem. We
          implement an advanced Gale-Shapley to find the result for your
          problem. <span
              className={'fst-italic fw-bold'}>Then, why do we need MOEA?</span>
        </p>
        <p>This is because MOEA will run that algorithm many times with
          different input orders. The input order
          is the most important factor in getting the best result (advanced
          Gale-Shapley). We decide which is
          the best result by calculating SatisfactionPoint based on your
          evaluate function.</p>
        <p>Afterward, the backend server returns the best solution to your
          problem with pairs. If you're looking for more information, such as
          algorithms' efficiency on solving your problem, you should click "Get
          detailed insights" button.</p>
        <h2>Endpoints</h2>
        <p>
          By default, Spring Boot server (backend) use port 8080.
          Open{' '}
          <a
              rel="noreferrer"
              href="http://localhost:8080"
              target="_blank"
          >
            this
          </a>{' '}
          on browser in order to check backend's status.
        </p>
        <div className="card border-0">
          <div className="card-body text-black rounded-3 mb-3 p-4"
               style={{background: '#dedede'}}>
            <p className="fs-4 fw-bold">
              POST
              <span className="ms-2 badge text-bg-warning">
                                /api/stable-matching-solver
                            </span>
            </p>
            <p>
              <b>Description</b>: Currently responsible for
              handling one-to-many and many-to-many Stable
              Matching problem.
            </p>
            <p>
              <b className="d-block">Body params:</b>
              <ul>
                <li>problemName : String</li>
                <li>numberOfSets : int</li>
                <li>numberOfIndividuals : int</li>
                <li>allPropertyNames : String[]</li>
                <li>Individuals : Individual[numberOfIndividuals]</li>
                <li>fitnessFunction : String</li>
                <li>evaluateFunction : String[numberOfSets]</li>
                <li>algorithm : String</li>
                <li>distributedCores : String</li>
                <li>populationSize : int</li>
                <li>generation : int</li>
                <li>maxTime : int</li>
              </ul>
            </p>
            <p><b>Possible Errors: </b> 500 Internal Server Error - unexpected
              exception occurs.</p>
          </div>
          <div className="card-body text-black rounded-3 mb-3 p-4"
               style={{background: '#dedede'}}>
            <p className="fs-4 fw-bold">
              POST
              <span className="ms-2 badge text-bg-warning">
                                /api/stable-matching-oto-solver
                            </span>
            </p>
            <p>
              <b>Description</b>: responsible for
              handling one-to-one Stable
              Matching problem.
            </p>
            <p>
              <b className="d-block">Body params:</b>
              <ul>
                <li>problemName : String</li>
                <li>numberOfSets : int</li>
                <li>numberOfIndividuals : int</li>
                <li>allPropertyNames : String[]</li>
                <li>Individuals : Individual[numberOfIndividuals]</li>
                <li>fitnessFunction : String</li>
                <li>evaluateFunction : String[numberOfSets]</li>
                <li>algorithm : String</li>
                <li>distributedCores : String</li>
                <li>populationSize : int</li>
                <li>generation : int</li>
                <li>maxTime : int</li>
              </ul>
            </p>
            <p><b>Possible Errors: </b> 500 Internal Server Error - unexpected
              exception occurs.</p>
          </div>
        </div>

      </section>
      <section
          className={`section ${guideFor == 'sm' ? 'd-block' : 'd-none'}`}
          ref={outputSMRef}
          id="12"
      >
        <h1>Outputs</h1>
        <p>
          Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Dicta sed quaerat debitis officiis veritatis voluptate
          voluptatibus fuga sunt voluptas, reiciendis placeat
          laboriosam ipsum quis dolorem nam qui accusantium
          consectetur provident.
        </p>
      </section>
    </div>
  </div>);
}
