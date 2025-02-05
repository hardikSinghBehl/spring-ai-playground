package com.behl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "com.behl")
record EvaluatorOptimizerProperties(
    Evaluator evaluator,
    Optimizer optimizer
) {
    record Evaluator(
        String model,
        Integer maxIterations,
        Float acceptableScore,
        Resource systemPrompt,
        Resource userPrompt) {
    }
    record Optimizer(Resource feedbackPrompt) {
    }
}