//package com.example.SS2_Backend.ss.smt.preference.impl.list;
//
//import com.example.SS2_Backend.ss.smt.preference.PreferenceList;
//
//import java.util.HashMap;
//import java.util.Set;
//
///**
// * TODO: implement PreferenceList
// * @author vu hoang
// */
//public class OTOPreferenceList extends HashMap<Integer, Double> implements PreferenceList {
//
//    /**
//     * {@inheritDoc}
//     * @author vu hoand
//     */
//    public int getLeastNode(int newMatch, Set<Integer> currentMatches) {
//        return currentMatches.stream().reduce(newMatch, (x, y) -> (get(x) > get(y)) ? y : x);
//    }
//
//    /**
//     * {@inheritDoc}
//     * @author vu hoand
//     */
//    public int getLeastNode(int newMatch, int currentMatch) {
//        return (get(newMatch) > get(currentMatch))
//                ? currentMatch
//                : newMatch;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public int getIndexByPosition(int position) {
//        return 0;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public int getLastOption() {
//        return 0;
//    }
//
//}
