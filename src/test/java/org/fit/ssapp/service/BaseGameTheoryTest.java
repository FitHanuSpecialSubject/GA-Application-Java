package org.fit.ssapp.service;

import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;

/**
 * Base class for Game Theory tests with common helper methods
 */
public abstract class BaseGameTheoryTest {
    
    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Helper method to create a normal player with strategies
     */
    protected NormalPlayer createNormalPlayer(String name, double[][] strategyData) {
        NormalPlayer player = new NormalPlayer();
        player.setName(name);
        player.setStrategies(Arrays.asList(
            createStrategy(strategyData[0]),
            createStrategy(strategyData[1])
        ));
        return player;
    }

    /**
     * Helper method to create a strategy with properties
     */
    protected Strategy createStrategy(double... properties) {
        Strategy strategy = new Strategy();
        for (double prop : properties) {
            strategy.addProperty(prop);
        }
        return strategy;
    }
} 
