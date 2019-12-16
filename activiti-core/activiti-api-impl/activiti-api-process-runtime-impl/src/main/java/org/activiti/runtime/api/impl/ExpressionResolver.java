package org.activiti.runtime.api.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.el.ExpressionManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionResolver {

    private static final TypeReference<Map<String, ?>> MAP_STRING_OBJECT_TYPE = new TypeReference<Map<String, ?>>() {
    };
    private final Logger logger = LoggerFactory.getLogger(ExpressionResolver.class);

    private static final String EXPRESSION_PATTERN_STRING = "([\\$]\\{([^\\}]*)\\})";
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile(EXPRESSION_PATTERN_STRING);
    private static final int EXPRESSION_KEY_INDEX = 1;

    private ObjectMapper mapper;

    private ExpressionManager expressionManager;

    public ExpressionResolver(ExpressionManager expressionManager,
                              ObjectMapper mapper) {
        this.expressionManager = expressionManager;
        this.mapper = mapper;
    }

    private Object resolveExpressions(final VariableScope variableMappingContext,
                                      final Object value) {
        if (value instanceof String) {
            return resolveExpressionsString(variableMappingContext,
                                            (String) value);
        } else if (value instanceof ObjectNode) {
            return resolveExpressionsMap(variableMappingContext,
                                         mapper.convertValue(value,
                                                             MAP_STRING_OBJECT_TYPE));
        } else if (value instanceof Map<?, ?>) {
            return resolveExpressionsMap(variableMappingContext,
                                         (Map<String, ?>) value);
        } else if (value instanceof List<?>) {
            return resolveExpressionsList(variableMappingContext,
                                          (List<?>) value);
        } else {
            return value;
        }
    }

    private List<Object> resolveExpressionsList(final VariableScope variableMappingContext,
                                                final List<?> sourceList) {
        final List<Object> result = new LinkedList<>();
        sourceList.forEach(value -> result.add(resolveExpressions(variableMappingContext,
                                                                  value)));
        return result;
    }

    public Map<String, Object> resolveExpressionsMap(final VariableScope variableMappingContext,
                                                     final Map<String, ?> sourceMap) {
        final Map<String, Object> result = new LinkedHashMap<>();
        sourceMap.forEach((key,
                           value) -> result.put(key,
                                                resolveExpressions(variableMappingContext,
                                                                   value)));
        return result;
    }

    private Object resolveExpressionsString(final VariableScope variableMappingContext,
                                            final String sourceString) {
        if (StringUtils.isBlank(sourceString)) {
            return sourceString;
        }
        if (sourceString.matches(EXPRESSION_PATTERN_STRING)) {
            return resolveObjectPlaceHolder(variableMappingContext,
                                            sourceString);
        } else {
            return resolveInStringPlaceHolder(variableMappingContext,
                                              sourceString);
        }
    }

    private Object resolveObjectPlaceHolder(VariableScope variableMappingContext,
                                            String sourceString) {
        try {
            return expressionManager.createExpression(sourceString).getValue(variableMappingContext);
        } catch (final Exception e) {
            logger.warn("Unable to resolve expression in variables, keeping original value",
                        e);
            return sourceString;
        }
    }

    private String resolveInStringPlaceHolder(final VariableScope variableMappingContext,
                                              final String sourceString) {
        final Matcher matcher = EXPRESSION_PATTERN.matcher(sourceString);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            final String expressionKey = matcher.group(EXPRESSION_KEY_INDEX);
            final Expression expression = expressionManager.createExpression(expressionKey);
            try {
                final Object value = expression.getValue(variableMappingContext);
                matcher.appendReplacement(sb,
                                          Objects.toString(value));
            } catch (final Exception e) {
                logger.warn("Unable to resolve expression in variables, keeping original value",
                            e);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public boolean containsExpression(final Object source) {
        if (source == null) {
            return false;
        } else if (source instanceof String) {
            return containsExpressionString((String) source);
        } else if (source instanceof ObjectNode) {
            return containsExpressionMap(mapper.convertValue(source,
                                                             MAP_STRING_OBJECT_TYPE));
        } else if (source instanceof Map<?, ?>) {
            return containsExpressionMap((Map<String, ?>) source);
        } else if (source instanceof List<?>) {
            return containsExpressionList((List<?>) source);
        } else {
            return false;
        }
    }

    private boolean containsExpressionString(final String sourceString) {
        return EXPRESSION_PATTERN.matcher(sourceString).find();
    }

    private boolean containsExpressionMap(final Map<String, ?> source) {
        for (Entry<String, ?> entry : source.entrySet()) {
            if (containsExpression(entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsExpressionList(List<?> source) {
        for (Object item : source) {
            if (containsExpression(item)) {
                return true;
            }
        }
        return false;
    }
}
