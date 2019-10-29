package org.activiti.runtime.api.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ExpressionResolver {

    private final Logger logger = LoggerFactory.getLogger(ExpressionResolver.class);

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("([\\$]\\{([^\\}]*)\\})");
    private static final int EXPRESSION_KEY_INDEX = 1;

    private ObjectMapper mapper = new ObjectMapper();

    private List<Object> resolveExpressionsList(final DelegateExecution execution,
                                                final List<?> sourceList) {
        final List<Object> result = new LinkedList<>();
        sourceList.forEach(value -> result.add(resolveExpressions(execution,
                                                                  value)));
        return result;
    }

    public Map<String, Object> resolveExpressionsMap(final DelegateExecution execution,
                                                     final Map<String, ? extends Object> sourceMap) {
        final Map<String, Object> result = new LinkedHashMap<>();
        sourceMap.forEach((key,
                           value) -> result.put(key,
                                                resolveExpressions(execution,
                                                                   value)));
        return result;
    }

    public String resolveExpressionsString(final DelegateExecution execution,
                                           final String sourceString) {
        if (sourceString == null || sourceString.isEmpty()) {
            return sourceString;
        }
        final Matcher matcher = EXPRESSION_PATTERN.matcher(sourceString);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            final String expressionKey = matcher.group(EXPRESSION_KEY_INDEX);
            final Expression expression = getExpressionManager().createExpression(expressionKey);
            try {
                final Object value = expression.getValue(execution);
                matcher.appendReplacement(sb,
                                          Objects.toString(value));
            } catch (final Exception e) {
                logger.error("Unable to resolve expression in variables or constants",
                             e);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public Object resolveExpressions(final DelegateExecution execution,
                                     final Object value) {
        if (value instanceof String) {
            return resolveExpressionsString(execution,
                                            (String) value);
        } else if (value instanceof ObjectNode) {
            return resolveExpressionsMap(execution,
                                         mapper.convertValue(value,
                                                             Map.class));
        } else if (value instanceof Map<?, ?>) {
            return resolveExpressionsMap(execution,
                                         (Map<String, ?>) value);
        } else if (value instanceof List<?>) {
            return resolveExpressionsList(execution,
                                          (List<?>) value);
        } else {
            return value;
        }
    }

    private ExpressionManager getExpressionManager() {
        return Context.getProcessEngineConfiguration().getExpressionManager();
    }

}
