/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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


package org.activiti.engine.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.variable.VariableTypes;
import org.activiti.engine.query.Query;

/**
 * Abstract query class that adds methods to query for variable values.
 *

 */
public abstract class AbstractVariableQueryImpl<T extends Query<?, ?>, U> extends AbstractQuery<T, U> {

  private static final long serialVersionUID = 1L;

  protected List<QueryVariableValue> queryVariableValues = new ArrayList<QueryVariableValue>();

  public AbstractVariableQueryImpl() {
  }

  public AbstractVariableQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public AbstractVariableQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public abstract long executeCount(CommandContext commandContext);

  @Override
  public abstract List<U> executeList(CommandContext commandContext, Page page);

  public T variableValueEquals(String name, Object value) {
    return variableValueEquals(name, value, true);
  }

  @SuppressWarnings("unchecked")
  protected T variableValueEquals(String name, Object value, boolean localScope) {
    addVariable(name, value, QueryOperator.EQUALS, localScope);
    return (T) this;
  }

  public T variableValueEquals(Object value) {
    return variableValueEquals(value, true);
  }

  @SuppressWarnings("unchecked")
  protected T variableValueEquals(Object value, boolean localScope) {
    queryVariableValues.add(new QueryVariableValue(null, value, QueryOperator.EQUALS, localScope));
    return (T) this;
  }

  public T variableValueEqualsIgnoreCase(String name, String value) {
    return variableValueEqualsIgnoreCase(name, value, true);
  }

  @SuppressWarnings("unchecked")
  protected T variableValueEqualsIgnoreCase(String name, String value, boolean localScope) {
    if(value == null) {
      throw new ActivitiIllegalArgumentException("value is null");
    }
    addVariable(name, value.toLowerCase(), QueryOperator.EQUALS_IGNORE_CASE, localScope);
    return (T) this;
  }

  public T variableValueNotEqualsIgnoreCase(String name, String value) {
    return variableValueNotEqualsIgnoreCase(name, value, true);
  }

  @SuppressWarnings("unchecked")
  protected T variableValueNotEqualsIgnoreCase(String name, String value, boolean localScope) {
    if(value == null) {
      throw new ActivitiIllegalArgumentException("value is null");
    }
    addVariable(name, value.toLowerCase(), QueryOperator.NOT_EQUALS_IGNORE_CASE, localScope);
    return (T) this;
  }

  public T variableValueNotEquals(String name, Object value) {
    return variableValueNotEquals(name, value, true);
  }

  @SuppressWarnings("unchecked")
  protected T variableValueNotEquals(String name, Object value, boolean localScope) {
    addVariable(name, value, QueryOperator.NOT_EQUALS, localScope);
    return (T) this;
  }

  public T variableValueGreaterThan(String name, Object value) {
    return variableValueGreaterThan(name, value, true);
  }

  @SuppressWarnings("unchecked")
  protected T variableValueGreaterThan(String name, Object value, boolean localScope) {
    addVariable(name, value, QueryOperator.GREATER_THAN, localScope);
    return (T) this;
  }

  public T variableValueGreaterThanOrEqual(String name, Object value) {
    return variableValueGreaterThanOrEqual(name, value, true);
  }

  @SuppressWarnings("unchecked")
  protected T variableValueGreaterThanOrEqual(String name, Object value, boolean localScope) {
    addVariable(name, value, QueryOperator.GREATER_THAN_OR_EQUAL, localScope);
    return (T) this;
  }

  public T variableValueLessThan(String name, Object value) {
    return variableValueLessThan(name, value, true);
  }

  @SuppressWarnings("unchecked")
  protected T variableValueLessThan(String name, Object value, boolean localScope) {
    addVariable(name, value, QueryOperator.LESS_THAN, localScope);
    return (T) this;
  }

  public T variableValueLessThanOrEqual(String name, Object value) {
    return variableValueLessThanOrEqual(name, value, true);
  }

  @SuppressWarnings("unchecked")
  protected T variableValueLessThanOrEqual(String name, Object value, boolean localScope) {
    addVariable(name, value, QueryOperator.LESS_THAN_OR_EQUAL, localScope);
    return (T) this;
  }

  public T variableValueLike(String name, String value) {
    return variableValueLike(name, value, true);
  }

  public T variableValueLikeIgnoreCase(String name, String value) {
    return variableValueLikeIgnoreCase(name, value, true);
  }

  @SuppressWarnings("unchecked")
  protected T variableValueLike(String name, String value, boolean localScope) {
    addVariable(name, value, QueryOperator.LIKE, localScope);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  protected T variableValueLikeIgnoreCase(String name, String value, boolean localScope) {
    addVariable(name, value.toLowerCase(), QueryOperator.LIKE_IGNORE_CASE, localScope);
    return (T) this;
  }

  protected void addVariable(String name, Object value, QueryOperator operator, boolean localScope) {
    if(name == null) {
      throw new ActivitiIllegalArgumentException("name is null");
    }
    if (value == null || isBoolean(value)) {
      // Null-values and booleans can only be used in EQUALS and
      // NOT_EQUALS
      switch (operator) {
      case GREATER_THAN:
        throw new ActivitiIllegalArgumentException("Booleans and null cannot be used in 'greater than' condition");
      case LESS_THAN:
        throw new ActivitiIllegalArgumentException("Booleans and null cannot be used in 'less than' condition");
      case GREATER_THAN_OR_EQUAL:
        throw new ActivitiIllegalArgumentException("Booleans and null cannot be used in 'greater than or equal' condition");
      case LESS_THAN_OR_EQUAL:
        throw new ActivitiIllegalArgumentException("Booleans and null cannot be used in 'less than or equal' condition");
      }

      if (operator == QueryOperator.EQUALS_IGNORE_CASE && !(value instanceof String)) {
        throw new ActivitiIllegalArgumentException("Only string values can be used with 'equals ignore case' condition");
      }

      if (operator == QueryOperator.NOT_EQUALS_IGNORE_CASE && !(value instanceof String)) {
        throw new ActivitiIllegalArgumentException("Only string values can be used with 'not equals ignore case' condition");
      }

      if((operator == QueryOperator.LIKE || operator == QueryOperator.LIKE_IGNORE_CASE) && !(value instanceof String))
      {
        throw new ActivitiIllegalArgumentException("Only string values can be used with 'like' condition");
      }
    }
    queryVariableValues.add(new QueryVariableValue(name, value, operator, localScope));
  }

  protected boolean isBoolean(Object value) {
    if (value == null) {
      return false;
    }
    return Boolean.class.isAssignableFrom(value.getClass()) || boolean.class.isAssignableFrom(value.getClass());
  }

  protected void ensureVariablesInitialized() {
    if (!queryVariableValues.isEmpty()) {
      VariableTypes variableTypes = Context.getProcessEngineConfiguration().getVariableTypes();
      for (QueryVariableValue queryVariableValue : queryVariableValues) {
        queryVariableValue.initialize(variableTypes);
      }
    }
  }

  public List<QueryVariableValue> getQueryVariableValues() {
    return queryVariableValues;
  }

  public boolean hasLocalQueryVariableValue() {
    for (QueryVariableValue qvv : queryVariableValues) {
        if (qvv.isLocal()) {
            return true;
        }
    }
    return false;
  }

  public boolean hasNonLocalQueryVariableValue() {
    for (QueryVariableValue qvv : queryVariableValues) {
        if (!qvv.isLocal()) {
            return true;
        }
    }
    return false;
  }

}
