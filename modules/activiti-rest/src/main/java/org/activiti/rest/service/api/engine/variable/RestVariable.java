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

package org.activiti.rest.service.api.engine.variable;

import org.activiti.engine.ActivitiIllegalArgumentException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;


/**
 * Pojo representing a variable used in REST-service which definies it's name, variable, 
 * scope and type.
 * 
 * @author Frederik Heremans
 */
public class RestVariable {
  
  public enum RestVariableScope {
    LOCAL, GLOBAL
  }

  private String name;
  private String type;
  private RestVariableScope variableScope;
  private Object value;
  private String valueUrl;
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  @JsonSerialize(include=Inclusion.NON_NULL)
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  @JsonIgnore
  public RestVariableScope getVariableScope() {
   return variableScope; 
  }
  public void setVariableScope(RestVariableScope variableScope) {
    this.variableScope = variableScope;
  }
  public Object getValue() {
    return value;
  }
  public void setValue(Object value) {
    this.value = value;
  }
  public String getScope() {
    String scope = null;
    if (variableScope != null) {
      scope = variableScope.name().toLowerCase();
    }
    return scope;
  }
  public void setScope(String scope) {
    setVariableScope(getScopeFromString(scope));
  }
  public void setValueUrl(String valueUrl) {
    this.valueUrl = valueUrl;
  }
  @JsonSerialize(include=Inclusion.NON_NULL)
  public String getValueUrl() {
    return valueUrl;
  }
  
  public static RestVariableScope getScopeFromString(String scope) {
    if (scope != null) {
      for (RestVariableScope s : RestVariableScope.values()) {
        if (s.name().equalsIgnoreCase(scope)) {
          return s;
        }
      }
      throw new ActivitiIllegalArgumentException("Invalid variable scope: '" + scope + "'");
    } else {
      return null;
    }
  }
}
