package org.fit.ssapp.ss.smt;

import static org.fit.ssapp.util.NumberUtils.formatDouble;
import static org.fit.ssapp.util.StringUtils.fillWithChar;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.fit.ssapp.ss.smt.requirement.Requirement;

/**
 * Matching Data
 */
@Getter
public class MatchingData {

  /**
   * number of individuals
   */
  private final int size;

  /**
   * number of properties
   */
  private final int propertyNum;

  /**
   * individual set
   */
  private final int[] sets;

  /**
   * individual capacities
   */
  private final int[] capacities;

  /**
   * total individual foreach set
   */
  private final Map<Integer, Integer> setNums;

  /**
   * exclude/ conflict pairs, solution will be considered as NOT GOOD if Matches it produces
   * contains one of the excludedPairs
   */
  @Setter
  int[][] excludedPairs;

  /**
   * characteristic data
   */
  private final double[][] propertyValues;
  private final double[][] weights;
  private final Requirement[][] requirements;

  /**
   * MatchingData constructor
   */
  public MatchingData(int size,
                      int propertyNum,
                      int[] sets,
                      int[] capacities,
                      double[][] propertyValues,
                      double[][] weights,
                      Requirement[][] requirements) {

    Map<Integer, Integer> setNums = new HashMap<>();
    for (int set : sets) {
      setNums.putIfAbsent(set, 0);
      setNums.put(set, setNums.get(set) + 1);
    }
    this.size = size;
    this.propertyNum = propertyNum;
    this.sets = sets;
    this.capacities = capacities;
    this.setNums = setNums;
    this.propertyValues = propertyValues;
    this.weights = weights;
    this.requirements = requirements;
  }

  /**
   * get number of set in this MatchingData's Problem <br/> example: one-to-many (two set) -> return
   * 2
   *
   * @return number of set
   */
  public int getNumberOfSets() {
    return this.setNums.size();
  }

  /**
   * get set no of individual at idx
   *
   * @param idx idx
   * @return set
   */
  public int getSetNoOf(int idx) {
    return this.sets[idx];
  }

  /**
   * get total of individual inside a given set
   *
   * @param setNo set
   * @return total
   */
  public int getTotalIndividualOfSet(int setNo) {
    return this.setNums.get(setNo);
  }

  /**
   * get capacity of individual
   *
   * @param idx position of individual
   * @return capacity
   */
  public int getCapacityOf(int idx) {
    return this.capacities[idx];
  }

  /**
   * get property value
   *
   * @param idx             position of individual
   * @param indexOfProperty position of property
   * @return value
   */
  public double getPropertyValueOf(int idx, int indexOfProperty) {
    return this.propertyValues[idx][indexOfProperty];
  }

  /**
   * get property weight (importance) (inside [0, 10])
   *
   * @param idx             position of individual
   * @param indexOfProperty position of property
   * @return weight value
   */
  public double getPropertyWeightOf(int idx, int indexOfProperty) {
    return this.weights[idx][indexOfProperty];
  }

  /**
   * get property Requirement
   *
   * @param idx             position of individual
   * @param indexOfProperty position of property
   * @return Requirement
   */
  public Requirement getRequirementOf(int idx, int indexOfProperty) {
    return this.requirements[idx][indexOfProperty];
  }

  /**
   * to String Builder
   *
   * @return StringBuilder
   */
  public StringBuilder toStringBuilder() {
    StringBuilder sb = new StringBuilder();
    String propName;
    for (int i = 1; i <= this.propertyNum; i++) {
      propName = "Prop " + i;
      sb.append(String.format("%-16s| ", propName));
    }
    String propNames = sb.toString();
    sb.delete(0, sb.length());
    //header
    System.out.println("No | Set | Name                | " + propNames);
    int width = this.propertyNum * 18 + 32;
    String filledString = fillWithChar('-', width);
    sb.append(filledString).append("\n");
    //content
    for (int i = 0; i < this.size; i++) {
      //name / set
      sb.append(String.format("%-3d| ", i));
      sb.append(String.format("%-4d| ", this.getSetNoOf(i)));
      sb.append(String.format("%-20s| ", "Node " + i));
      // prop value
      StringBuilder ss = new StringBuilder();
      for (int j = 0; j < this.propertyNum; j++) {
        ss.append(String.format("%-16s| ", formatDouble(this.getPropertyValueOf(i, j))));
      }
      sb.append(ss).append("\n");
      ss.delete(0, sb.length());
      ss.append(String.format("%33s", "Requirement: | "));
      for (int j = 0; j < this.propertyNum; j++) {
        ss.append(String.format("%-16s| ",
                this.getRequirementOf(i, j).toString()));
      }
      sb.append(ss).append("\n");
      ss.delete(0, sb.length());
      ss.append(String.format("%33s", "Weight: | "));
      for (int j = 0; j < this.propertyNum; j++) {
        ss.append(String.format("%-16s| ", this.getPropertyWeightOf(i, j)));
      }
      sb.append(ss).append("\n");
    }
    sb.append(filledString).append("\n");
    return sb;
  }

  public String toString() {
    return this.toStringBuilder().toString();
  }

}

