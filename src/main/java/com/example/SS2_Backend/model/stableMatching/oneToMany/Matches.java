package com.example.SS2_Backend.model.stableMatching.oneToMany;

import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Represents the results of a Stable Matching algorithm for a one-to-many matching problem.
 * Each provider can be matched to multiple consumers, but each consumer is matched to only one provider.
 * This class supports operations for adding and removing matches, checking excluded pairs,
 * and tracking those who are left unmatched.
 *
 * <p>The {@code Matches} class offers the following functionality:
 * <ul>
 *   <li>Maintaining a map of providers to their matched consumers</li>
 *   <li>Checking if a provider has reached its maximum consumer capacity</li>
 *   <li>Managing unmatched (left-over) individuals</li>
 *   <li>Retrieving provider-consumer relationships for further analysis or debugging</li>
 * </ul>
 */
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class Matches implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    // Map of each provider to their matched consumers.
    Map<Integer, Set<Integer>> providerToConsumers;
    Set<Integer> leftOverConsumers = new HashSet<>();
    Set<Integer> leftOverProviders = new HashSet<>();

    public Matches(int numProviders) {
        this.providerToConsumers = new HashMap<>(numProviders);
    }

    /**
     * Retrieves the set of consumers currently matched to a specific provider.
     *
     * @param providerIndex The index of the provider.
     * @return A set of consumers matched to the specified provider, or an empty set if none.
     */
    public Set<Integer> getConsumersOfProvider(int providerIndex) {
        return providerToConsumers.getOrDefault(providerIndex, new HashSet<>());
    }

    /**
     * Adds a match between the specified provider and consumer.
     *
     * @param provider The provider's index.
     * @param consumer The consumer's index.
     */
    public void addMatch(int provider, int consumer) {
        providerToConsumers
                .computeIfAbsent(provider, k -> new HashSet<>())
                .add(consumer);
    }

    /**
     * Removes an existing match between the specified provider and consumer.
     *
     * @param provider The provider's index.
     * @param consumer The consumer's index.
     */
    public void removeMatch(int provider, int consumer) {
        Set<Integer> consumers = providerToConsumers.get(provider);
        if (consumers != null) {
            consumers.remove(consumer);
        }
    }

    /**
     * Checks if a specific pair (provider and consumer) has already been matched.
     *
     * @param provider The provider's index.
     * @param consumer The consumer's index.
     * @return {@code true} if the consumer is matched to the specified provider; {@code false} otherwise.
     */
    public boolean hasPairExcluded(int provider, int consumer) {
        return providerToConsumers.get(provider).contains(consumer);
    }

    public boolean hasAnyMatchForProvider(int provider) {
        return getConsumersOfProvider(provider).size() > 0;
    }

    /**
     * Retrieves the provider to whom a specific consumer is matched.
     *
     * @param consumer The consumer's index.
     * @return The provider's index if the consumer is matched; {@code null} otherwise.
     */
    public Integer getProviderOfConsumer(int consumer) {
        for (Map.Entry<Integer, Set<Integer>> entry : providerToConsumers.entrySet()) {
            if (entry.getValue().contains(consumer)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Checks if a provider has reached its maximum consumer capacity.
     *
     * @param provider The provider's index.
     * @param maxCapacity The maximum allowed consumers for the provider.
     * @return {@code true} if the provider's matched consumers meet or exceed the maximum capacity; {@code false} otherwise.
     */
    public boolean isProviderFull(int provider, int maxCapacity) {
        return getConsumersOfProvider(provider).size() >= maxCapacity;
    }

    /**
     * Adds a indv to the list of unmatched individuals.
     *
     * @param consumer The index of the unmatched.
     */
    public void addLeftOverConsumers(int consumer) {
        leftOverConsumers.add(consumer);
    }

    public void addLeftOverProvider(int provider) {
        leftOverProviders.add(provider);
    }

    /**
     * Retrieves an array of consumers matched to a specific provider.
     *
     * @param provider The provider's index.
     * @return An array of consumer indices matched to the specified provider.
     */
    public Integer[] getConsumerMatchesForProvider(int provider) {
        return getConsumersOfProvider(provider).toArray(new Integer[0]);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Matches: \n");
        for (Map.Entry<Integer, Set<Integer>> entry : providerToConsumers.entrySet()) {
            result.append("Provider ").append(entry.getKey()).append(" -> Consumers: ")
                    .append(entry.getValue()).append("\n");
        }
        result.append("Left-over Providers: ").append(leftOverProviders).append("\n");
        result.append("Left-over Consumers: ").append(leftOverConsumers).append("\n");
        return result.toString();
    }
}
