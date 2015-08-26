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

import java.text.ParseException;
import java.util.Date;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.springframework.stereotype.Component;

import com.activiti.model.runtime.RestVariable;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;


/**
 * @author Frederik Heremans
 */
@Component
public class DateRestVariableConverter implements RestVariableConverter {

  protected ISO8601DateFormat isoFormatter = new ISO8601DateFormat();
  
  @Override
  public String getRestTypeName() {
    return "date";
  }

  @Override
  public Class< ? > getVariableType() {
    return Date.class;
  }

  @Override
  public Object getVariableValue(RestVariable result) {
    if(result.getValue() != null) {
      if(!(result.getValue() instanceof String)) {
        throw new ActivitiIllegalArgumentException("Converter can only convert string to date");
      }
      try {
        return isoFormatter.parse((String) result.getValue());
      } catch (ParseException e) {
        throw new ActivitiIllegalArgumentException("The given variable value is not a date: '" + result.getValue() + "'", e);
      }
    }
    return null;
  }

  @Override
  public void convertVariableValue(Object variableValue, RestVariable result) {
    if(variableValue != null) {
      if(!(variableValue instanceof Date)) {
        throw new ActivitiIllegalArgumentException("Converter can only convert booleans");
      }
      result.setValue(isoFormatter.format(variableValue));
    } else {
      result.setValue(null);
    }
  }

}
