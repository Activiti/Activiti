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

import java.util.ArrayDeque;
import java.util.Deque;
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
    private static final char OUTER_EXPRESSION_DELIMITER = '#';
    private static final char NESTED_EXPRESSION_DELIMITER = '$';

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
        } catch (NumberFormatException _) {
            return logInvalidMaxVarSizeAndReturnDefault("non-numeric");
        }

        return logInvalidMaxVarSizeAndReturnDefault("non-positive");
    }

    private static String formatMaxVarSizeForLogging(int maxVarSizeForExpressionParsing) {
        return maxVarSizeForExpressionParsing == Integer.MAX_VALUE
            ? "unlimited"
            : maxVarSizeForExpressionParsing + " characters";
    }

    private static int logInvalidMaxVarSizeAndReturnDefault(String reason) {
        if (logger.isWarnEnabled()) {
            logger.warn(
                "MAX_VAR_SIZE_FOR_EXPRESSION_PARSING was set to an invalid {} value. Using default: {}",
                reason,
                formatMaxVarSizeForLogging(DEFAULT_MAX_VAR_SIZE_FOR_EXPRESSION_PARSING)
            );
        }
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
            ExpressionRange expressionRange = findNextExpressionRange(sourceString, currentIndex);
            if (expressionRange == null) {
                result.append(sourceString, currentIndex, sourceString.length());
                break;
            }

            result.append(sourceString, currentIndex, expressionRange.start);
            final String expressionKey = sourceString.substring(expressionRange.start, expressionRange.end + 1);
            final Expression expression = expressionManager.createExpression(expressionKey);
            try {
                final Object value = expressionEvaluator.evaluate(expression, expressionManager, delegateInterceptor);
                result.append(Objects.toString(value));
            } catch (final Exception e) {
                logger.warn("Unable to resolve expression in variables", e);
                result.append("");
            }
            currentIndex = expressionRange.end + 1;
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
        return findNextExpressionRange(sourceString, 0) != null;
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
        ExpressionRange expressionRange = findNextExpressionRange(sourceString, 0);
        return (
            expressionRange != null && expressionRange.start == 0 && expressionRange.end == sourceString.length() - 1
        );
    }

    private ExpressionRange findNextExpressionRange(String sourceString, int fromIndex) {
        ExpressionRangeParserState parserState = new ExpressionRangeParserState();
        int index = Math.max(0, fromIndex);

        while (index < sourceString.length()) {
            char currentCharacter = sourceString.charAt(index);

            if (!parserState.isExpressionStarted()) {
                index = advanceUntilExpressionStart(sourceString, index, currentCharacter, parserState);
            } else if (parserState.isInsideQuote()) {
                updateQuotedState(currentCharacter, parserState);
            } else {
                ExpressionRange expressionRange = handleExpressionCharacter(
                    sourceString,
                    index,
                    currentCharacter,
                    parserState
                );
                if (expressionRange != null) {
                    return expressionRange;
                }
            }

            if (parserState.skipNextCharacter) {
                index++;
                parserState.skipNextCharacter = false;
            }
            index++;
        }

        return parserState.getCompatibilityFallbackRange();
    }

    private int advanceUntilExpressionStart(
        String sourceString,
        int currentIndex,
        char currentCharacter,
        ExpressionRangeParserState parserState
    ) {
        if (isExpressionOpening(sourceString, currentIndex, currentCharacter)) {
            parserState.expressionStart = currentIndex;
            parserState.delimiterStack.push(OUTER_EXPRESSION_DELIMITER);
            return currentIndex + 1;
        }
        return currentIndex;
    }

    private void updateQuotedState(char currentCharacter, ExpressionRangeParserState parserState) {
        if (currentCharacter == '\\' && !parserState.escaped) {
            parserState.escaped = true;
            return;
        }

        if (currentCharacter == parserState.activeQuote && !parserState.escaped) {
            parserState.activeQuote = 0;
        }
        parserState.escaped = false;
    }

    private ExpressionRange handleExpressionCharacter(
        String sourceString,
        int currentIndex,
        char currentCharacter,
        ExpressionRangeParserState parserState
    ) {
        return switch (currentCharacter) {
            case '$' -> handleNestedExpressionOpening(sourceString, currentIndex, parserState);
            case '\'', '"', '`' -> {
                parserState.activeQuote = currentCharacter;
                yield null;
            }
            case '{' -> {
                pushPlainBrace(currentIndex, parserState);
                yield null;
            }
            case '[' -> pushAndContinue(parserState.delimiterStack, '[');
            case '(' -> pushAndContinue(parserState.delimiterStack, '(');
            case '}' -> handleClosingBrace(sourceString, currentIndex, parserState);
            case ']' -> popMatchingDelimiter(parserState.delimiterStack, '[');
            case ')' -> popMatchingDelimiter(parserState.delimiterStack, '(');
            default -> null;
        };
    }

    private boolean isExpressionOpening(String sourceString, int currentIndex, char currentCharacter) {
        return (
            currentCharacter == EXPRESSION_PREFIX.charAt(0) &&
            currentIndex + 1 < sourceString.length() &&
            sourceString.charAt(currentIndex + 1) == EXPRESSION_PREFIX.charAt(1)
        );
    }

    private ExpressionRange handleNestedExpressionOpening(
        String sourceString,
        int currentIndex,
        ExpressionRangeParserState parserState
    ) {
        if (isExpressionOpening(sourceString, currentIndex, '$')) {
            parserState.delimiterStack.push(NESTED_EXPRESSION_DELIMITER);
            parserState.skipNextCharacter = true;
        }
        return null;
    }

    private void pushPlainBrace(int currentIndex, ExpressionRangeParserState parserState) {
        if (
            currentIndex == parserState.expressionStart + EXPRESSION_PREFIX.length() ||
            !hasOnlyOuterExpressionDelimiter(parserState.delimiterStack)
        ) {
            parserState.delimiterStack.push('{');
        }
    }

    private ExpressionRange pushAndContinue(Deque<Character> delimiterStack, char delimiter) {
        delimiterStack.push(delimiter);
        return null;
    }

    private ExpressionRange popMatchingDelimiter(Deque<Character> delimiterStack, char delimiter) {
        if (!delimiterStack.isEmpty() && delimiterStack.peek() == delimiter) {
            delimiterStack.pop();
        }
        return null;
    }

    private ExpressionRange handleClosingBrace(
        String sourceString,
        int currentIndex,
        ExpressionRangeParserState parserState
    ) {
        Character currentDelimiter = parserState.delimiterStack.peek();
        if (!isClosingExpressionDelimiter(currentDelimiter)) {
            if (parserState.compatibilityFallbackEnd < 0) {
                parserState.compatibilityFallbackEnd = currentIndex;
            }
            return null;
        }

        char closedDelimiter = parserState.delimiterStack.pop();
        if (closedDelimiter == OUTER_EXPRESSION_DELIMITER) {
            return new ExpressionRange(parserState.expressionStart, currentIndex);
        }
        if (
            closedDelimiter == NESTED_EXPRESSION_DELIMITER &&
            hasOnlyOuterExpressionDelimiter(parserState.delimiterStack) &&
            hasTrailingClosingBrace(sourceString, currentIndex)
        ) {
            return new ExpressionRange(parserState.expressionStart, currentIndex + 1);
        }
        return null;
    }

    private boolean isClosingExpressionDelimiter(char delimiter) {
        return delimiter == '{' || delimiter == NESTED_EXPRESSION_DELIMITER || delimiter == OUTER_EXPRESSION_DELIMITER;
    }

    private boolean hasOnlyOuterExpressionDelimiter(Deque<Character> delimiterStack) {
        return delimiterStack.size() == 1 && delimiterStack.peek() == OUTER_EXPRESSION_DELIMITER;
    }

    private boolean hasTrailingClosingBrace(String sourceString, int currentIndex) {
        return currentIndex + 1 < sourceString.length() && sourceString.charAt(currentIndex + 1) == '}';
    }

    private static final class ExpressionRange {

        private final int start;
        private final int end;

        private ExpressionRange(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static final class ExpressionRangeParserState {

        private int expressionStart = -1;
        private int compatibilityFallbackEnd = -1;
        private char activeQuote = 0;
        private boolean escaped = false;
        private boolean skipNextCharacter = false;
        private final Deque<Character> delimiterStack = new ArrayDeque<>();

        private boolean isExpressionStarted() {
            return expressionStart >= 0;
        }

        private boolean isInsideQuote() {
            return activeQuote != 0;
        }

        private ExpressionRange getCompatibilityFallbackRange() {
            if (expressionStart >= 0 && compatibilityFallbackEnd >= 0) {
                return new ExpressionRange(expressionStart, compatibilityFallbackEnd);
            }
            return null;
        }
    }
}
