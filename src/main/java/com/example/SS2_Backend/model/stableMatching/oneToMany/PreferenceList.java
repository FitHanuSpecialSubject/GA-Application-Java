package com.example.SS2_Backend.model.stableMatching.oneToMany;

import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * This class manages the preferences for both providers and consumers in a one-to-many or many-to-one matching problem.
 */
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PreferenceList {

    // Scores are the preferences or priorities for the matching (either from provider or consumer perspective).
    final double[] scores;
    // The positions correspond to the IDs of the individuals (either providers or consumers).
    final int[] positions;
    int current; // Tracks the current index in the list.
    final int padding; // Used for index adjustments.

    // Constructor to initialize preference list for the given size
    public PreferenceList(int size, int padding) {
        scores = new double[size];
        positions = new int[size];
        current = 0;
        this.padding = padding;
    }

    // Return the number of preferences in the list
    public int size() {
        return positions.length;
    }

    /**
     * <i>THIS METHOD ONLY VALUABLE AFTER @method sortByValueDescending IS INVOKED </i>
     * @param position position (rank best <-- 0, 1, 2, 3, ... --> worst) on the preference list
     * @return unique identifier of the competitor instance that holds the respective position on the list
     */
    public int getIndexByPosition(int position) throws ArrayIndexOutOfBoundsException {
        try {
            return positions[position] + this.padding;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Position " + position + " not found: " + e.getMessage());
            return -1;
        }
    }

    // Add a preference score to the list
    public void add(double score, int position) {
        this.scores[current] = score;
        this.positions[current] = position;
        this.current++;
    }

    // Sort preferences based on scores in descending order
    public void sortPreferences() {
        sortDescendingByScores();
    }

    // Sort scores in descending order (highest preference first)
    private void sortDescendingByScores() {
        double[] cloneScores = scores.clone(); // Copy to a new array
        int size = cloneScores.length;

        for (int i = size / 2 - 1; i >= 0; i--) {
            heapify(cloneScores, size, i);
        }

        for (int i = size - 1; i > 0; i--) {
            double temp = cloneScores[0];
            int tempPos = positions[0];

            cloneScores[0] = cloneScores[i];
            positions[0] = positions[i];

            cloneScores[i] = temp;
            positions[i] = tempPos;

            heapify(cloneScores, i, 0);
        }
    }

    // Heapify the array to maintain the sorting order
    private void heapify(double[] array, int heapSize, int rootIndex) {
        int largestIndex = rootIndex; // Initialize largest as root
        int leftChildIndex = 2 * rootIndex + 1; // left = 2*rootIndex + 1
        int rightChildIndex = 2 * rootIndex + 2; // right = 2*rootIndex + 2

        if (leftChildIndex < heapSize && array[leftChildIndex] > array[largestIndex]) {
            largestIndex = leftChildIndex;
        }

        if (rightChildIndex < heapSize && array[rightChildIndex] > array[largestIndex]) {
            largestIndex = rightChildIndex;
        }

        if (largestIndex != rootIndex) {
            double swap = array[rootIndex];
            int posSwap = positions[rootIndex];

            array[rootIndex] = array[largestIndex];
            positions[rootIndex] = positions[largestIndex];

            array[largestIndex] = swap;
            positions[largestIndex] = posSwap;

            heapify(array, heapSize, largestIndex);
        }
    }

    // Find the least preferred individual among the currently matched set
    public int getLeastPreferredIndividual(int newIndividual, Integer[] currentMatches) {
        int leastPreferred = newIndividual - this.padding;
        for (int currentMatch : currentMatches) {
            if (this.scores[leastPreferred] > this.scores[currentMatch - this.padding]) {
                leastPreferred = currentMatch - this.padding;
            }
        }
        return leastPreferred + this.padding;
    }

    // Check if the score of an individual is greater than another
    public boolean isMorePreferred(int individual, int comparedIndividual) {
        return this.scores[individual - this.padding] > this.scores[comparedIndividual - this.padding];
    }

    public double getScoreByIndex(int x) {
        return scores[x - this.padding];
    }

    // Return the index of the individual at a given preference rank
    public int getIndividualByRank(int rank) throws ArrayIndexOutOfBoundsException {
        try {
            return positions[rank] + this.padding;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Rank " + rank + " not found: " + e.getMessage());
            return -1;
        }
    }

    // String representation of the preference list
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("{");
        for (int i = 0; i < scores.length; i++) {
            int pos = positions[i];
            result
                    .append("[")
                    .append(pos)
                    .append(" -> ")
                    .append(String.format("%.2f", scores[pos]))
                    .append("]");
            if (i < scores.length - 1) result.append(", ");
        }
        result.append("}");
        return result.toString();
    }
}