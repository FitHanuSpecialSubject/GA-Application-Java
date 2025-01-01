package com.example.SS2_Backend.model.stableMatching.oneToMany;

import com.example.SS2_Backend.model.stableMatching.Requirement.Requirement;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static com.example.SS2_Backend.util.NumberUtils.formatDouble;
import static com.example.SS2_Backend.util.StringUtils.fillWithChar;

/**
 * Represents a list of individuals involved in a one-to-many matching problem model.
 * This class initializes and manages details about each individual, such as provider capacities,
 * property names, and attribute details, enabling structured access to individual and provider data.
 *
 * <p>The {@code IndividualList} class provides functionality for:
 * <ul>
 *   <li>Setting up and validating the list of individuals and their attributes</li>
 *   <li>Counting providers and setting their capacities</li>
 *   <li>Retrieving attributes, weights, and requirements for each individual</li>
 *   <li>Formatting and printing participant information in a readable format</li>
 * </ul>
 *
 */
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class IndividualList {

    final List<Individual> individuals;
    final int totalIndividuals;
    int providerCount;
    final int propertiesPerIndividual;
    final int[] providerCapacities;
    final String[] propertiesNames;

    /**
     * Constructs an {@code IndividualList} and initializes fields related to population data.
     * Ensures that the number of participants and attributes meet the minimum requirements for matching.
     *
     * @param individuals The list of individual participants.
     * @param propertiesNames An array of property names used for each individual's attributes.
     * @throws IllegalArgumentException if there are fewer than three participants or no properties per individual.
     */
    public IndividualList(List<Individual> individuals, String[] propertiesNames) {
        this.individuals = individuals;
        this.totalIndividuals = individuals.size();
        if (totalIndividuals < 3) {
            throw new IllegalArgumentException(
                    "Invalid number of participants, number must be greater or equal to 3 as matching makes no sense");
        }
        this.propertiesPerIndividual = individuals.get(0).getProperties().size();
        if (propertiesPerIndividual == 0) {
            throw new IllegalArgumentException(
                    "Invalid number of attributes, number must be greater than 0 as matching makes no sense");
        }
        this.propertiesNames = propertiesNames;
        this.providerCapacities = new int[getProviderCountAndInitCapacities()];
    }

    /**
     * Initializes the provider count and capacities in a single loop.
     * Counts individuals in the provider set and assigns their capacities.
     *
     * @return The count of providers.
     */
    private int getProviderCountAndInitCapacities() {
        int count = 0;
        for (int i = 0; i < this.totalIndividuals; i++) {
            if (individuals.get(i).getBelongToSet() == 0) {
                providerCapacities[count] = individuals.get(i).getCapacity();
                count++;
            }
        }
        this.providerCount = count;
        return count;
    }

    /**
     * Retrieves the set role (provider or consumer) of a specific participant by index.
     *
     * @param index The index of the participant.
     * @return 0 if the participant is a provider, 1 if a consumer.
     */
    public int getRoleOfIndividual(int index) {
        return this.individuals.get(index).getBelongToSet();
    }

    /**
     * Retrieves the capacity of a specific provider by index.
     *
     * @param index The index of the provider.
     * @return The capacity of the provider.
     */
    public int getProviderCapacity(int index) {
        return this.providerCapacities[index];
    }

    /**
     * Retrieves the value of a specified attribute for a participant.
     *
     * @param index The index of the participant.
     * @param attributeIndex The index of the attribute.
     * @return The attribute value for the specified participant and attribute.
     */
    public double getAttributeValueOf(int index, int attributeIndex) {
        return individuals.get(index).getProperty(attributeIndex);
    }

    /**
     * Retrieves the weight of a specified attribute for a participant.
     *
     * @param participantIndex The index of the participant.
     * @param attributeIndex The index of the attribute.
     * @return The attribute weight for the specified participant and attribute.
     */
    public double getAttributeWeightOf(int participantIndex, int attributeIndex) {
        return individuals.get(participantIndex).getPropertyWeight(attributeIndex);
    }

    /**
     * Retrieves the requirement of a specified attribute for a participant.
     *
     * @param idx1 The index of the participant.
     * @param i The index of the attribute.
     * @return The requirement for the specified attribute.
     */
    public Requirement getRequirementOf(int idx1, int i) {
        return this.individuals.get(idx1).getRequirement(i);
    }

    public void print() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.propertiesPerIndividual; i++) {
            sb.append(String.format("%-16s| ", propertiesNames[i]));
        }
        String attrName = sb.toString();
        sb.delete(0, sb.length());

        // Header
        System.out.println("No | Set | Name                | " + attrName);
        int width = this.propertiesPerIndividual * 18 + 32;
        String filledString = fillWithChar('-', width);
        sb.append(filledString).append("\n");

        // Content
        for (int i = 0; i < this.totalIndividuals; i++) {
            sb.append(String.format("%-3d| ", i));
            sb.append(String.format("%-4d| ", individuals.get(i).getBelongToSet()));
            sb.append(String.format("%-20s| ", individuals.get(i).getName()));

            // Attribute values
            StringBuilder ss = new StringBuilder();
            for (int j = 0; j < this.propertiesPerIndividual; j++) {
                ss.append(String.format("%-16s| ", formatDouble(this.getAttributeValueOf(i, j))));
            }
            sb.append(ss).append("\n");

            // Requirements
            ss.delete(0, sb.length());
            ss.append(String.format("%33s", "Requirement: | "));
            for (int j = 0; j < this.propertiesPerIndividual; j++) {
                ss.append(String.format("%-16s| ", this.individuals.get(i).getRequirement(j).toString()));
            }
            sb.append(ss).append("\n");

            // Weights
            ss.delete(0, sb.length());
            ss.append(String.format("%33s", "Weight: | "));
            for (int j = 0; j < this.propertiesPerIndividual; j++) {
                ss.append(String.format("%-16s| ", this.getAttributeWeightOf(i, j)));
            }
            sb.append(ss).append("\n");
        }
        sb.append(filledString).append("\n");
        System.out.print(sb);
    }
}
