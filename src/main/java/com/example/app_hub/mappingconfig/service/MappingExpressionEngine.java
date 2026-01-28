package com.example.app_hub.mappingconfig.service;

import com.example.app_hub.mappingconfig.model.MappingConfigModel;
import com.example.app_hub.mappingconfig.model.MappingExpressionModel;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MappingExpressionEngine {

    private final ExpressionParser parser = new SpelExpressionParser();

    public Object calculateValue(MappingConfigModel mapping, Map<String, String> rowMap) {
        return switch (mapping.getMappingType()) {
            case DIRECT -> getValueIgnoreCase(rowMap, mapping.getSourceAttribute());
            case CONSTANT -> getConstantValue(mapping);
            case TRANSFORMATION -> evaluateTransformation(mapping, rowMap);
            default -> null;
        };
    }

    private Object evaluateTransformation(MappingConfigModel mapping, Map<String, String> rowMap) {
        String rawValue = getValueIgnoreCase(rowMap, mapping.getSourceAttribute());

        // Short-circuit: if the CSV cell is empty, we don't transform
        if (rawValue == null) {
            return null;
        }

        String expressionString = getActiveExpressionString(mapping);
        if (expressionString == null) {
            return rawValue; // Fail-safe: return raw data if expression record is missing
        }

        return runSpel(expressionString, rawValue, rowMap);
    }

    private Object getConstantValue(MappingConfigModel mapping) {
        // We look for the active expression record, but treat it as a literal string
        return getActiveExpressionString(mapping);
    }

    private String getActiveExpressionString(MappingConfigModel mapping) {
        if (mapping.getExpressions() == null) return null;

        return mapping.getExpressions().stream()
                .filter(MappingExpressionModel::isActive)
                .map(MappingExpressionModel::getExpression)
                .findFirst()
                .orElse(null);
    }
    /**
     * The Helper Method: Standardizes how we find values in the CSV row Map.
     * This solves the "AccountName" vs "accountname" case sensitivity issues.
     */
    private String getValueIgnoreCase(Map<String, String> rowMap, String sourceAttr) {
        if (sourceAttr == null || sourceAttr.isBlank()) return null;

        // 1. Quick check for exact match
        if (rowMap.containsKey(sourceAttr)) {
            return rowMap.get(sourceAttr);
        }

        // 2. Fuzzy check: ignore case and trim spaces
        return rowMap.entrySet().stream()
                .filter(e -> e.getKey().trim().equalsIgnoreCase(sourceAttr.trim()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private Object runSpel(String expressionString, String val, Map<String, String> row) {
        try {
            Expression expression = parser.parseExpression(expressionString);
            StandardEvaluationContext context = new StandardEvaluationContext();

            context.setVariable("val", val);
            context.setVariable("row", row);

            return expression.getValue(context);
        } catch (Exception e) {
            System.err.println("SpEL Error [" + expressionString + "]: " + e.getMessage());
            return null;
        }
    }
}