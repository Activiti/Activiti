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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Pojo representing a variable used in REST-service which definies it's name,
 * variable, scope and type.
 * 
 * @author Frederik Heremans
 */
public class RestVariable {

    public enum RestVariableScope {
        LOCAL, GLOBAL
    };

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

    @JsonInclude(Include.NON_NULL)
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

    @JsonInclude(Include.NON_NULL)
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
            return null;
        } else {
            return null;
        }
    }
}
