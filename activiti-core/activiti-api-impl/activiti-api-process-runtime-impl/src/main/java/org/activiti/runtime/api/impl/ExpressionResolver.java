/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.runtime.api.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

public class ExpressionResolver {

    private static final TypeReference<Map<String, ?>> MAP_STRING_OBJECT_TYPE = new TypeReference<Map<String, ?>>() {};
    private static final Logger logger = LoggerFactory.getLogger(ExpressionResolver.class);

    private static final String EXPRESSION_PREFIX = "${";
    private static final int DEFAULT_MAX_VAR_SIZE_FOR_EXPRESSION_PARSING = Integer.MAX_VALUE;
    private static final int MAX_VAR_SIZE_FOR_EXPRESSION_PARSING = resolveMaxVarSizeForExpressionParsing(
        System.getenv("MAX_VAR_SIZE_FOR_EXPRESSION_PARSING")
    );

    private JsonMapper mapper;
    private final DelegateInterceptor delegateInterceptor;
    private final int maxVarSizeForExpressionParsing;

    private ExpressionManager expressionManager;

    public ExpressionResolver(
        ExpressionManager expressionManager,
        JsonMapper mapper,
        DelegateInterceptor delegateInterceptor
    ) {
        this(expressionManager, mapper, delegateInterceptor, MAX_VAR_SIZE_FOR_EXPRESSION_PARSING);
    }

    ExpressionResolver(
        ExpressionManager expressionManager,
        JsonMapper mapper,
        DelegateInterceptor delegateInterceptor,
        int maxVarSizeForExpressionParsing
    ) {
        this.expressionManager = expressionManager;
        this.mapper = mapper;
        this.delegateInterceptor = delegateInterceptor;
        this.maxVarSizeForExpressionParsing = maxVarSizeForExpressionParsing;
        logger.info(
            "ExpressionResolver initialized with MAX_VAR_SIZE_FOR_EXPRESSION_PARSING: {}",
            formatMaxVarSizeForLogging(maxVarSizeForExpressionParsing)
        );
    }

    /**
     * Returns the maximum variable size for expression parsing.
     * Strings exceeding this size will skip expression resolution to prevent OutOfMemoryError.
     *
     * @return the maximum size in characters
     */
    public static int getMaxVarSizeForExpressionParsing() {
        return MAX_VAR_SIZE_FOR_EXPRESSION_PARSING;
    }

    static int resolveMaxVarSizeForExpressionParsing(String envValue) {
        if (StringUtils.isBlank(envValue)) {
            return DEFAULT_MAX_VAR_SIZE_FOR_EXPRESSION_PARSING;
        }

        try {
            int maxSize = Integer.parseInt(envValue.trim());
            if (maxSize > 0) {
                return maxSize;
            }
        } catch (NumberFormatException e) {
            return logInvalidMaxVarSizeAndReturnDefault(envValue);
        }

        return logInvalidMaxVarSizeAndReturnDefault(envValue);
    }

    private static String formatMaxVarSizeForLogging(int maxVarSizeForExpressionParsing) {
        return maxVarSizeForExpressionParsing == Integer.MAX_VALUE
            ? "unlimited"
            : maxVarSizeForExpressionParsing + " characters";
    }

    private static int logInvalidMaxVarSizeAndReturnDefault(String envValue) {
        logger.warn(
            "Invalid value for MAX_VAR_SIZE_FOR_EXPRESSION_PARSING environment variable: {}. Using default: {}",
            envValue,
            formatMaxVarSizeForLogging(DEFAULT_MAX_VAR_SIZE_FOR_EXPRESSION_PARSING)
        );
        return DEFAULT_MAX_VAR_SIZE_FOR_EXPRESSION_PARSING;
    }

    private Object resolveExpressions(final ExpressionEvaluator expressionEvaluator, final Object value) {
        if (value instanceof String) {
            return resolveExpressionsString(expressionEvaluator, (String) value);
        } else if (value instanceof ObjectNode) {
            return resolveExpressionsMap(expressionEvaluator, mapper.convertValue(value, MAP_STRING_OBJECT_TYPE));
        } else if (value instanceof Map<?, ?>) {
            return resolveExpressionsMap(expressionEvaluator, (Map<String, ?>) value);
        } else if (value instanceof List<?>) {
            return resolveExpressionsList(expressionEvaluator, (List<?>) value);
        } else {
            return value;
        }
    }

    private List<Object> resolveExpressionsList(
        final ExpressionEvaluator expressionEvaluator,
        final List<?> sourceList
    ) {
        final List<Object> result = new LinkedList<>();
        sourceList.forEach(value -> result.add(resolveExpressions(expressionEvaluator, value)));
        return result;
    }

    public Map<String, Object> resolveExpressionsMap(
        final ExpressionEvaluator expressionEvaluator,
        final Map<String, ?> sourceMap
    ) {
        final Map<String, Object> result = new LinkedHashMap<>();
        sourceMap.forEach((key, value) -> result.put(key, resolveExpressions(expressionEvaluator, value)));
        return result;
    }

    private Object resolveExpressionsString(final ExpressionEvaluator expressionEvaluator, final String sourceString) {
        if (StringUtils.isBlank(sourceString)) {
            return sourceString;
        }

        // Skip expression parsing for strings exceeding the configured size limit to prevent OutOfMemoryError
        if (sourceString.length() > maxVarSizeForExpressionParsing) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Skipping expression parsing for string exceeding max size: {} characters (limit: {})",
                    sourceString.length(),
                    formatMaxVarSizeForLogging(maxVarSizeForExpressionParsing)
                );
            }
            return sourceString;
        }

        if (isWholeExpression(sourceString)) {
            return resolveObjectPlaceHolder(expressionEvaluator, sourceString);
        } else {
            return resolveInStringPlaceHolder(expressionEvaluator, sourceString);
        }
    }

    private Object resolveObjectPlaceHolder(ExpressionEvaluator expressionEvaluator, String sourceString) {
        try {
            return expressionEvaluator.evaluate(
                expressionManager.createExpression(sourceString),
                expressionManager,
                delegateInterceptor
            );
        } catch (final Exception e) {
            logger.warn("Unable to resolve expression in variables", e);
            return null;
        }
    }

    private String resolveInStringPlaceHolder(
        final ExpressionEvaluator expressionEvaluator,
        final String sourceString
    ) {
        // Size check already done in resolveExpressionsString, but adding defensive check
        if (sourceString.length() > maxVarSizeForExpressionParsing) {
            return sourceString;
        }

        int currentIndex = 0;
        StringBuilder result = new StringBuilder(sourceString.length());
        while (currentIndex < sourceString.length()) {
            int expressionStart = findExpressionStart(sourceString, currentIndex);
            if (expressionStart < 0) {
                result.append(sourceString, currentIndex, sourceString.length());
                break;
            }

            int expressionEnd = findExpressionEnd(sourceString, expressionStart);
            if (expressionEnd < 0) {
                result.append(sourceString, currentIndex, sourceString.length());
                break;
            }

            result.append(sourceString, currentIndex, expressionStart);
            final String expressionKey = sourceString.substring(expressionStart, expressionEnd + 1);
            final Expression expression = expressionManager.createExpression(expressionKey);
            try {
                final Object value = expressionEvaluator.evaluate(expression, expressionManager, delegateInterceptor);
                result.append(Objects.toString(value));
            } catch (final Exception e) {
                logger.warn("Unable to resolve expression in variables", e);
                result.append("");
            }
            currentIndex = expressionEnd + 1;
        }
        return result.toString();
    }

    public List<String> findVariableNamesContainingExpressions(final Map<String, ?> source) {
        final List<String> result = new LinkedList<>();
        if (source == null) {
            return result;
        }
        source.forEach((key, value) -> {
            if (containsExpression(value)) {
                result.add(key);
            }
        });
        return result;
    }

    public boolean containsExpression(final Object source) {
        if (source == null) {
            return false;
        } else if (source instanceof String) {
            return containsExpressionString((String) source);
        } else if (source instanceof ObjectNode) {
            return containsExpressionMap(mapper.convertValue(source, MAP_STRING_OBJECT_TYPE));
        } else if (source instanceof Map<?, ?>) {
            return containsExpressionMap((Map<String, ?>) source);
        } else if (source instanceof List<?>) {
            return containsExpressionList((List<?>) source);
        } else {
            return false;
        }
    }

    private boolean containsExpressionString(final String sourceString) {
        return findExpressionStart(sourceString, 0) >= 0;
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

    private boolean isWholeExpression(String sourceString) {
        int expressionStart = findExpressionStart(sourceString, 0);
        return expressionStart == 0 && findExpressionEnd(sourceString, expressionStart) == sourceString.length() - 1;
    }

    private int findExpressionStart(String sourceString, int fromIndex) {
        int searchIndex = fromIndex;
        while (searchIndex >= 0 && searchIndex < sourceString.length()) {
            int expressionStart = sourceString.indexOf(EXPRESSION_PREFIX, searchIndex);
            if (expressionStart < 0) {
                return -1;
            }

            if (findExpressionEnd(sourceString, expressionStart) >= 0) {
                return expressionStart;
            }

            searchIndex = expressionStart + EXPRESSION_PREFIX.length();
        }
        return -1;
    }

    private int findExpressionEnd(String sourceString, int expressionStart) {
        if (expressionStart < 0) {
            return -1;
        }

        char activeQuote = 0;
        boolean escaped = false;
        int curlyDepth = 0;
        int squareDepth = 0;
        int roundDepth = 0;

        for (int index = expressionStart + EXPRESSION_PREFIX.length(); index < sourceString.length(); index++) {
            char currentCharacter = sourceString.charAt(index);

            if (activeQuote != 0) {
                if (currentCharacter == '\\' && !escaped) {
                    escaped = true;
                    continue;
                }

                if (currentCharacter == activeQuote && !escaped) {
                    activeQuote = 0;
                }
                escaped = false;
                continue;
            }

            switch (currentCharacter) {
                case '\'':
                case '"':
                    activeQuote = currentCharacter;
                    break;
                case '{':
                    curlyDepth++;
                    break;
                case '[':
                    squareDepth++;
                    break;
                case '(':
                    roundDepth++;
                    break;
                case '}':
                    if (curlyDepth == 0 && squareDepth == 0 && roundDepth == 0) {
                        return index;
                    }
                    if (curlyDepth > 0) {
                        curlyDepth--;
                    }
                    break;
                case ']':
                    if (squareDepth > 0) {
                        squareDepth--;
                    }
                    break;
                case ')':
                    if (roundDepth > 0) {
                        roundDepth--;
                    }
                    break;
                default:
                    break;
            }
        }

        return -1;
    }
}
