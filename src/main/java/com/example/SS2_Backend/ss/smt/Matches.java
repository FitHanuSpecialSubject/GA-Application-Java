package com.example.SS2_Backend.ss.smt;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 * Data Structure for result of StableMatchingExtra algorithm
 * Matches = {Match1, Match2, Match3, ...}
 * Match can be an Object of "Pair" or "MatchSet" Class, both Implement "MatchItem" Interface
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Matches implements Serializable {

    /** size of Matches */
    final int size;

    /** matches data */
    final TreeSet<Integer>[] matches;

    @SuppressWarnings("unchecked")
    public Matches(int size) {
        this.size = size;
        this.matches = new TreeSet[size];
        for (int i = 0; i < size; i++) {
            this.matches[i] = new TreeSet<>();
        }
    }

    /**
     * get matched individual(s) of targetIndividual
     *
     * @param targetIndividual int
     * @return matched individual(s)
     */
    public Set<Integer> getSetOf(int targetIndividual) {
        return matches[targetIndividual];
    }

    /**
     * as name
     *
     * @return int
     */
    public int size() {
        return matches.length;
    }

    /**
     * check if node is matched with other node(s)
     *
     * @param node to check
     * @return as title true
     */
    public boolean isMatched(int node) {
        return !matches[node].isEmpty();
    }

    /**
     * check if two nodes is matched
     *
     * @param node1 node1
     * @param node2 node2
     * @return as title true
     */
    public boolean isMatched(int node1, int node2) {
        return matches[node1].contains(node2) || matches[node2].contains(node1);
    }

    /**
     * check if node number of connections reach its capacity
     *
     * @param targetNode node to check
     * @param targetNodeCapacity node cap
     * @return as title true
     */
    public boolean isFull(int targetNode, int targetNodeCapacity) {
        int currentSize = this.getSetOf(targetNode).size();
        return currentSize >= targetNodeCapacity;
    }

    /**
     * add match
     *
     * @param node as name
     * @param nodeToAdd as name
     */
    public void addMatch(int node, int nodeToAdd) {
        matches[node].add(nodeToAdd);
    }

    /**
     * remove match bidirectionally
     *
     * @param node as name
     * @param nodeToRemove as name
     */
    public void removeMatch(int node, int nodeToRemove) {
        matches[node].remove(nodeToRemove);
    }

    /**
     * add match bidirectionally
     *
     * @param node1 index of node1
     * @param node2 index of node2
     */
    public void addMatchBi(int node1, int node2) {
        matches[node1].add(node2);
        matches[node2].add(node1);
    }

    /**
     * remove match bidirectionally
     *
     * @param node1 index of node1
     * @param node2 index of node2
     */
    public void removeMatchBi(int node1, int node2) {
        matches[node1].remove(node2);
        matches[node2].remove(node1);
    }

    /**
     * Get the single match of a node if it exists
     *
     * @param targetNode the node to query
     * @return the single matched node or -1 if none exists
     */
    public int getSingleMatch(int targetNode) {
        if (matches[targetNode].size() == 1) {
            return matches[targetNode].iterator().next();
        }
        return -1; // Indicating no single match exists
    }

    /**
     * as name
     *
     * @return StringBuilder
     */
    public StringBuilder toStringBuilder() {
        StringBuilder sb = new StringBuilder();
        int i = 0;

        for (Set<Integer> matchSet : this.matches) {
            sb.append("[").append(i).append(" -> ").append(matchSet).append("] ").append("\n");
            i++;
        }

        sb.append("Left Overs: ").append(getLeftOvers()).append("\n");
        return sb;
    }

    public Set<Integer> getLeftOvers() {
        ArrayList<Integer> leftOvers = new ArrayList<>();

        for (int i = 0; i < matches.length; i++) {
             if ( matches[i].isEmpty()) {
                leftOvers.add(i);
             }
        }

        return new TreeSet(leftOvers);
    }

    /**
     * as name
     *
     * @return String
     */
    @Override
    public String toString() {
       return this.toStringBuilder().toString();
    }


    public TreeSet<Integer>[] getAllMatches() {
        return matches;
    }

    /**
     * add all nodes that is matched in each turn
     * @param nodes
     */

    public void addMatchForGroup(List<Integer> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            int currentNode = nodes.get(i);
            for (int j = 0; j < nodes.size(); j++) {
                if (i != j) { // Prevent matching a node with itself
                    addMatch(currentNode, nodes.get(j));
                }
            }
        }
    }

    /**
     * get matches of target node
     * @param target
     * @return
     */
    public Collection<Integer> getMatchesAndTarget(int target) {
        Collection<Integer> nodesToRemove = new HashSet<>();
        nodesToRemove.add(target);
        nodesToRemove.addAll(getSetOf(target));
        return nodesToRemove;
    }

    /**
     * dismatch target node from a collection of nodes that was matched with target
     * @param target
     * @param nodeToRemove
     */
    public void disMatch(int target, Collection<Integer> nodeToRemove) {
        matches[target].removeAll(nodeToRemove);
    }
}
