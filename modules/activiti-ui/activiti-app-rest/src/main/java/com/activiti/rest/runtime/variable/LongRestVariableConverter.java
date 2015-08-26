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
package com.activiti.rest.runtime.variable;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.springframework.stereotype.Component;

import com.activiti.model.runtime.RestVariable;


/**
 * @author Frederik Heremans
 */
@Component
public class LongRestVariableConverter implements RestVariableConverter {

  @Override
  public String getRestTypeName() {
    return "long";
  }

  @Override
  public Class< ? > getVariableType() {
    return Long.class;
  }

  @Override
  public Object getVariableValue(RestVariable result) {
    if(result.getValue() != null) {
      if(!(result.getValue() instanceof Number)) {
        throw new ActivitiIllegalArgumentException("Converter can only convert longs");
      }
      return ((Number) result.getValue()).longValue();
    }
    return null;
  }

  @Override
  public void convertVariableValue(Object variableValue, RestVariable result) {
    if(variableValue != null) {
      if(!(variableValue instanceof Long)) {
        throw new ActivitiIllegalArgumentException("Converter can only convert integers");
      }
      result.setValue(variableValue);
    } else {
      result.setValue(null);
    }
  }

}
