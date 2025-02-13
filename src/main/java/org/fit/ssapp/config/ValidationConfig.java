package org.fit.ssapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "validation")
@Data
public class ValidationConfig {
    private int individualsMin;
    private int populationMax;
    private int generationMax;
}

