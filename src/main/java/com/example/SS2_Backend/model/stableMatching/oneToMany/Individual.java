package com.example.SS2_Backend.model.stableMatching.oneToMany;

import com.example.SS2_Backend.model.stableMatching.Property;
import com.example.SS2_Backend.model.stableMatching.Requirement.Requirement;
import com.example.SS2_Backend.model.stableMatching.Requirement.RequirementDecoder;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;


/**
 * Represents a standard individual entity in a one-to-many matching problem model.
 * This class is used for defining the properties, set type, and capacity of an individual,
 * which can either be a provider or a consumer in a matching scenario.
 *
 * <p>The {@code Individual} class includes methods to manage properties and requirements,
 * allowing each instance to store values associated with specific matching needs.
 *
 * <p>For example, in a job matching scenario, providers might represent job positions
 * and consumers would represent applicants. Each provider has a certain capacity,
 * which signifies the number of matches it can accept.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Setting and retrieving individual properties with values and weights</li>
 *   <li>Specifying requirements for each property</li>
 *   <li>Determining the individual's type as a provider or consumer</li>
 * </ul>
 *
 * <p>Annotations ensure proper JSON serialization and deserialization.
 *
 * @see Property
 * @see Requirement
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class Individual {
    String name;
    int belongToSet; // 0 for provider, 1 for consumer
    List<Property> properties;
    int capacity; // Capacity limit for providers

    /**
     * Sets a property with a specified value, weight, and requirement.
     * The requirement is decoded from a string format.
     *
     * @param propertyValue   The numeric value of the property.
     * @param propertyWeight  The weight of the property, affecting its priority.
     * @param inputRequirement The input requirement as a string, which will be decoded.
     */
    @JsonProperty("Properties")
    public void setProperty(double propertyValue, double propertyWeight, String inputRequirement) {
        String[] decodedRequirement = RequirementDecoder.decodeInputRequirement(inputRequirement);
        Property property = new Property(propertyValue, propertyWeight, decodedRequirement);
        this.properties.add(property);
    }

    /**
     * Sets the name of the individual, trimming any leading or trailing spaces.
     *
     * @param name The name of the individual.
     */
    @JsonProperty("IndividualName")
    public void setName(String name) {
        this.name = name.trim();
    }

    /**
     * Sets the type of the individual as either provider or consumer.
     *
     * @param set 0 for provider, 1 for consumer.
     */
    @JsonProperty("IndividualSet")
    public void setBelongToSet(int set) {
        this.belongToSet = set;
    }

    public double getProperty(int index) {
        if (index >= 0 && index < this.properties.size()) {
            return properties.get(index).getValue();
        } else {
            return 0;
        }
    }

    public double getPropertyWeight(int index) {
        if (index >= 0 && index < this.properties.size()) {
            return properties.get(index).getWeight();
        } else {
            return 0;
        }
    }

    public Requirement getRequirement(int index) {
        return properties.get(index).getRequirement();
    }
}
