/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl;

import java.io.Serializable;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.variable.ByteArrayType;
import org.activiti.engine.impl.variable.JPAEntityListVariableType;
import org.activiti.engine.impl.variable.JPAEntityVariableType;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;

/**
 * Represents a variable value used in queries.
 * 

 */
public class QueryVariableValue implements Serializable {
  private static final long serialVersionUID = 1L;
  private String name;
  private Object value;
  private QueryOperator operator;

  private VariableInstanceEntity variableInstanceEntity;
  private boolean local;

  public QueryVariableValue(String name, Object value, QueryOperator operator, boolean local) {
    this.name = name;
    this.value = value;
    this.operator = operator;
    this.local = local;
  }

  public void initialize(VariableTypes types) {
    if (variableInstanceEntity == null) {
      VariableType type = types.findVariableType(value);
      if (type instanceof ByteArrayType) {
        throw new ActivitiIllegalArgumentException("Variables of type ByteArray cannot be used to query");
      } else if (type instanceof JPAEntityVariableType && operator != QueryOperator.EQUALS) {
        throw new ActivitiIllegalArgumentException("JPA entity variables can only be used in 'variableValueEquals'");
      } else if (type instanceof JPAEntityListVariableType) {
        throw new ActivitiIllegalArgumentException("Variables containing a list of JPA entities cannot be used to query");
      } else {
        // Type implementation determines which fields are set on the entity
        variableInstanceEntity = Context.getCommandContext().getVariableInstanceEntityManager().create(name, type, value);
      }
    }
  }

  public String getName() {
    return name;
  }

  public String getOperator() {
    if (operator != null) {
      return operator.toString();
    }
    return QueryOperator.EQUALS.toString();
  }

  public String getTextValue() {
    if (variableInstanceEntity != null) {
      return variableInstanceEntity.getTextValue();
    }
    return null;
  }

  public Long getLongValue() {
    if (variableInstanceEntity != null) {
      return variableInstanceEntity.getLongValue();
    }
    return null;
  }

  public Double getDoubleValue() {
    if (variableInstanceEntity != null) {
      return variableInstanceEntity.getDoubleValue();
    }
    return null;
  }

  public String getTextValue2() {
    if (variableInstanceEntity != null) {
      return variableInstanceEntity.getTextValue2();
    }
    return null;
  }

  public String getType() {
    if (variableInstanceEntity != null) {
      return variableInstanceEntity.getType().getTypeName();
    }
    return null;
  }

  public boolean isLocal() {
    return local;
  }
}