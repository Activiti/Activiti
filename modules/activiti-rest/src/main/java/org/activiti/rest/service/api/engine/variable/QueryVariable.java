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

/**
 * @author Frederik Heremans
 */
public class QueryVariable {

  private String name;
  private String operation;
  private Object value;
  private String type;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public QueryVariableOperation getVariableOperation() {
    if(operation == null) {
      return null;
    }
    return QueryVariableOperation.forFriendlyName(operation);
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }
  
  public Object getValue() {
    return value;
  }
  
  public void setValue(Object value) {
    this.value = value;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public enum QueryVariableOperation {
    EQUALS("equals"),
    NOT_EQUALS("notEquals"),
    EQUALS_IGNORE_CASE("equalsIgnoreCase"),
    NOT_EQUALS_IGNORE_CASE("notEqualsIgnoreCase"),
    LIKE("like"),
    LIKE_IGNORE_CASE("likeIgnoreCase"),
    GREATER_THAN("greaterThan"),
    GREATER_THAN_OR_EQUALS("greaterThanOrEquals"),
    LESS_THAN("lessThan"),
    LESS_THAN_OR_EQUALS("lessThanOrEquals");
    
    private String friendlyName;
    
    private QueryVariableOperation(String friendlyName) {
      this.friendlyName = friendlyName;
    }
    
    public String getFriendlyName() {
      return friendlyName;
    }
    
    public static QueryVariableOperation forFriendlyName(String friendlyName) {
      for(QueryVariableOperation type : values()) {
        if(type.friendlyName.equals(friendlyName)) {
          return type;
        }
      }
      throw new ActivitiIllegalArgumentException("Unsupported variable query operation: " + friendlyName);
    }
  }
  
}
