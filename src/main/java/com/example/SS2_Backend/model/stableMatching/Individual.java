package com.example.SS2_Backend.model.stableMatching;

import com.example.SS2_Backend.dto.request.IndividualDeserializer;

import com.example.SS2_Backend.model.stableMatching.Requirement.Requirement;
import com.example.SS2_Backend.model.stableMatching.Requirement.RequirementDecoder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.SS2_Backend.util.NumberUtils.isDouble;
import static com.example.SS2_Backend.util.NumberUtils.isInteger;

@Getter
@JsonDeserialize(using = IndividualDeserializer.class)
public class Individual {

    private String IndividualName;
    private int IndividualSet;
    @Setter
    private int Capacity;
    private final List<Property> Properties = new ArrayList<>();

    public Individual() {

    }

    @JsonProperty("Properties")
    public void setProperty(double propertyValue, double propertyWeight, String inputRequirement) {
        String[] decodedRequirement = RequirementDecoder.decodeInputRequirement(inputRequirement);
        Property property = new Property(propertyValue, propertyWeight, decodedRequirement);
        this.Properties.add(property);
    }

    public void setProperty(double propertyValue,
                            double propertyWeight,
                            String[] inputRequirement) {
        Property property = new Property(propertyValue, propertyWeight, inputRequirement);
        this.Properties.add(property);
    }

    public static void main(String[] args) {
        String inputReq = "200.011--";
        String[] requirement = RequirementDecoder.decodeInputRequirement(inputReq);
        System.out.println(Arrays.toString(requirement));
    }

    @JsonProperty("IndividualName")
    public void setIndividualName(String individualName) {
        IndividualName = individualName;
    }

    @JsonProperty("IndividualSet")
    public void setIndividualSet(int individualSet) {
        IndividualSet = individualSet;
    }

    public int getNumberOfProperties() {
        return Properties.size();
    }

    public Double getPropertyValue(int index) {
        if (index >= 0 && index < this.Properties.size()) {
            return Properties
                    .get(index)
                    .getValue();
        } else {
            return null;
        }
    }

    public double getPropertyWeight(int index) {
        if (index >= 0 && index < this.Properties.size()) {
            return Properties
                    .get(index)
                    .getWeight();
        } else {
            return 0;
        }
    }

    public Requirement getRequirement(int index) {
        return Properties
                .get(index)
                .getRequirement();
    }

    public String toString() {
        System.out.println("Name: " + IndividualName);
        System.out.println("Belong to set: " + IndividualSet);
        System.out.println("Capacity: " + Capacity);
        System.out.println("Properties:");
        System.out.println("---------------------------------");
        for (Property property : Properties) {
            System.out.println(property.toString());
        }
        return "\n";
    }

}
