package com.example.SS2_Backend.model.stableMatching;

import com.example.SS2_Backend.model.stableMatching.Requirement.Requirement;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

import static com.example.SS2_Backend.util.NumberUtils.formatDouble;
import static com.example.SS2_Backend.util.StringUtils.fillWithChar;

@Getter
public class IndividualList {

    private final List<Individual> individuals;
    private final int numberOfIndividual;
    private int numberOfIndividualForSet0;
    private final int numberOfProperties;
    private final int[] capacities;
    private final String[] propertyNames;

    /**
     * Initializes fields related to the population data.
     * ------------------------------------------
     * This method is executed only after the Individuals list has been initialized.
     * It sets up various fields such as the number of individuals, number of individuals in set 0,
     * number of properties, and preference lists based on the Individuals list.
     * @throws IllegalArgumentException if the number of individuals is less than 3, as matching would make no sense.
     */
    public IndividualList(List<Individual> individuals, String[] propertyNames) {
        this.individuals = individuals;
        this.numberOfIndividual = individuals.size();
        if (numberOfIndividual < 3) {
            throw new IllegalArgumentException(
                    "Invalid number of individuals, number must be greater or equal to 3 (int) as matching makes no sense");
        }
        this.numberOfProperties = individuals.get(0).getProperties().size();
        if (numberOfProperties == 0) {
            throw new IllegalArgumentException(
                    "Invalid number of properties, number must be greater than 0 (int) as matching makes no sense");
        }
        this.capacities = new int[individuals.size()];
        this.propertyNames = propertyNames;
        initialize();
    }

    private void initialize() {
        int count = 0;
        int tmpCapacity;
        for (int i = 0; i < this.numberOfIndividual; i++) {
            tmpCapacity = individuals.get(i).getCapacity();
            this.capacities[i] = tmpCapacity;
            this.capacities[count] = tmpCapacity;
            if (individuals.get(i).getIndividualSet() == 0) {
                count++;
            }
        }
        this.numberOfIndividualForSet0 = count;
    }
    //public Individual getIndividual(int index) {
    //    return this.individuals.get(index);
    //}

    public int getSetOf(int index) {
        return this.individuals.get(index).getIndividualSet();
    }

    /**
     * Retrieves the capacity of each node.
     * ---------------------------------
     * This method returns an array containing the capacity of each object.
     * The capacity of a single object can be obtained by passing its index (in the individual list) to this array.
     * For example: The capacity of the person at index 0 can be accessed as capacities[0].
     * @return An array of integers representing the capacities of each object.
     */
    public int getCapacityOf(int index) {
        return this.capacities[index];
    }

    public double getPropertyValueOf(int index, int indexOfProperty) {
        return individuals.get(index).getPropertyValue(indexOfProperty);
    }

    public double getPropertyWeightOf(int indexOfObject, int indexOfProperty) {
        return individuals.get(indexOfObject).getPropertyWeight(indexOfProperty);
    }

    public void print() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.numberOfProperties; i++) {
            sb.append(String.format("%-16s| ", propertyNames[i]));
        }
        String propName = sb.toString();
        sb.delete(0, sb.length());
        //header
        System.out.println("No | Set | Name                | " + propName);
        int width = this.numberOfProperties * 18 + 32;
        String filledString = fillWithChar('-', width);
        sb.append(filledString).append("\n");
        //content
        for (int i = 0; i < this.numberOfIndividual; i++) {
            //name / set
            sb.append(String.format("%-3d| ", i));
            sb.append(String.format("%-4d| ", individuals.get(i).getIndividualSet()));
            sb.append(String.format("%-20s| ", individuals.get(i).getIndividualName()));
            // prop value
            StringBuilder ss = new StringBuilder();
            for (int j = 0; j < this.numberOfProperties; j++) {
                ss.append(String.format("%-16s| ", formatDouble(this.getPropertyValueOf(i, j))));
            }
            sb.append(ss).append("\n");
            ss.delete(0, sb.length());
            ss.append(String.format("%33s", "Requirement: | "));
            for (int j = 0; j < this.numberOfProperties; j++) {
                ss.append(String.format("%-16s| ",
                        this.individuals.get(i).getRequirement(j).toString()));
            }
            sb.append(ss).append("\n");
            ss.delete(0, sb.length());
            ss.append(String.format("%33s", "Weight: | "));
            for (int j = 0; j < this.numberOfProperties; j++) {
                ss.append(String.format("%-16s| ", this.getPropertyWeightOf(i, j)));
            }
            sb.append(ss).append("\n");
        }
        sb.append(filledString).append("\n");
        System.out.print(sb);
    }

    public String toString() {
        return "IndividualList{" + "individuals=" + individuals + ", numberOfIndividual=" +
                numberOfIndividual + ", numberOfIndividualForSet0=" + numberOfIndividualForSet0 +
                ", numberOfProperties=" + numberOfProperties +
//                 ", capacities=" + capacities +
                ", propertyNames=" + Arrays.toString(propertyNames) + '}';
    }

    public Requirement getRequirementOf(int idx1, int i) {
        return this.individuals.get(idx1).getRequirement(i);
    }

}
