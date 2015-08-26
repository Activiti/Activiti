/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.model.runtime;


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
    if (operation == null) {
      return null;
    }
    return QueryVariableOperation.forFriendlyName(operation);
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
      for (QueryVariableOperation type : values()) {
        if (type.friendlyName.equals(friendlyName)) {
          return type;
        }
      }
      return null;
    }
  }
  
}
