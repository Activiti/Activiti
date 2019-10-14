package org.activiti.runtime.api.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivitExpressionParser {
    
    private final Logger logger = LoggerFactory.getLogger(ActivitExpressionParser.class);

    private static final Pattern EL_PATTERN = Pattern.compile("([\\$]\\{([^\\}]*)\\})");
    private static final int EL_KEY = 1;

    private DelegateExecution execution;

    public ActivitExpressionParser(DelegateExecution execution) {
        this.execution = execution;
    }

    public Map<String, Object> resolveExpressionsMap(final Map<String, ? extends Object> sourceMap) {
        final Map<String, Object> result = new LinkedHashMap<>();
        sourceMap.forEach((key,
                           value) -> result.put(key,
                                                resolveExpressions(value)));
        return result;
    }

    public String resolveExpressionsString(final String sourceString) {
        if (sourceString == null || sourceString.isEmpty()) {
            return sourceString;
        }
        final Matcher matcher = EL_PATTERN.matcher(sourceString);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            final String expressionKey = matcher.group(EL_KEY);
            final Expression expression = Context.getProcessEngineConfiguration().getExpressionManager().createExpression(expressionKey);
            try {
                Object value = expression.getValue(execution);
                matcher.appendReplacement(sb,
                                          Objects.toString(value));
            } catch (Exception e) {
                logger.error("Unable to resolve expression in variables or constants", e);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public Object resolveExpressions(Object value) {
        if (value instanceof String) {
            return resolveExpressionsString((String) value);
        } else if (value instanceof Map<?, ?>) {
            return resolveExpressionsMap((Map<String, ?>) value);
        } else {
            return value;
        }
    }
}
