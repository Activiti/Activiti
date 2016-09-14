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
package org.activiti.form.engine.impl.cmd;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.activiti.form.engine.ActivitiFormValidationException;
import org.activiti.form.engine.impl.interceptor.Command;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.model.FormDefinition;
import org.activiti.form.model.FormField;
import org.activiti.form.model.FormFieldTypes;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.LocalDate;

/**
 * @author Tijs Rademakers
 */
public class GetVariablesFromFormSubmissionCmd implements Command<Map<String, Object>>, Serializable {

  private static final long serialVersionUID = 1L;

  protected FormDefinition formDefinition;
  protected Map<String, Object> values;
  protected String outcome;
  
  public GetVariablesFromFormSubmissionCmd(FormDefinition formDefinition, Map<String, Object> values) {
    this.formDefinition = formDefinition;
    this.values = values;
  }

  public GetVariablesFromFormSubmissionCmd(FormDefinition formDefinition, Map<String, Object> values, String outcome) {
    this(formDefinition, values);
    this.outcome = outcome;
  }

  public Map<String, Object> execute(CommandContext commandContext) {
    // When no values are given, use an empty map to ensure validation is performed (eg. for required fields)
    if (values == null) {
      values = Collections.emptyMap();
    }

    // Loop over all form fields and see if a value was provided
    Map<String, FormField> fieldMap = formDefinition.allFieldsAsMap();
    Map<String, Object> variables = new HashMap<String, Object>();
    for (String fieldId : fieldMap.keySet()) {
      Object variableValue = null;
      FormField formField = fieldMap.get(fieldId);

      if (FormFieldTypes.EXPRESSION.equals(formField.getType()) || FormFieldTypes.CONTAINER.equals(formField.getType())) {
        continue;
      }

      if (values.containsKey(fieldId)) {
        variableValue = transformFormFieldValueToVariableValue(formField, values.get(fieldId));
        variables.put(formField.getId(), variableValue);
      }

      if (formField.isRequired() && variableValue == null && !FormFieldTypes.UPLOAD.equals(formField.getType())) {
        throw new ActivitiFormValidationException("Form field " + formField.getId() + " is required, but no value was found");
      }
    }

    // Handle outcomes
    if (outcome != null) {
      String targetVariable = null;
      if (formDefinition.getOutcomeVariableName() != null) {
        targetVariable = formDefinition.getOutcomeVariableName();
      } else {
        targetVariable = "form_" + formDefinition.getKey() + "_outcome";
      }
      
      variables.put(targetVariable, outcome);
    }

    return variables;
  }
  
  @SuppressWarnings("unchecked")
  protected Object transformFormFieldValueToVariableValue(FormField formField, Object formFieldValue) {

    Object result = formFieldValue;
    if (formField.getType().equals(FormFieldTypes.DATE)) {
      if (StringUtils.isNotEmpty((String) formFieldValue)) {
        try {
          result = LocalDate.parse((String) formFieldValue);
  
        } catch (Exception e) {
          e.printStackTrace();
          result = null;
        }
      }

    } else if (formField.getType().equals(FormFieldTypes.INTEGER) && formFieldValue instanceof String) {
      String strFieldValue = (String) formFieldValue;
      if (StringUtils.isNotEmpty(strFieldValue) && NumberUtils.isNumber(strFieldValue)) {
        result = Long.valueOf(strFieldValue);
        
      } else {
        result = (Long) null;
      }

    } else if (formField.getType().equals(FormFieldTypes.AMOUNT) && formFieldValue instanceof String) {
      try {
        result = Double.parseDouble((String) formFieldValue);
        
      } catch (NumberFormatException e) {
        result = null;
      }
      
    } else if (formField.getType().equals(FormFieldTypes.DROPDOWN)) {
      if (formFieldValue != null && formFieldValue instanceof Map<?, ?>) {
        result = ((Map<?, ?>) formFieldValue).get("id");
        if (result == null) {
          // fallback to name for manual config options
          result = ((Map<?, ?>) formFieldValue).get("name");
        }
      }

    } else if (formField.getType().equals(FormFieldTypes.UPLOAD)) {
      // We don't store the variable, the field-name will be referenced by the created related content entries
      result = null;

    } else if (formField.getType().equals(FormFieldTypes.PEOPLE) || formField.getType().equals(FormFieldTypes.FUNCTIONAL_GROUP)) {
      if (formFieldValue != null && formFieldValue instanceof Map<?, ?>) {
        Map<String, Object> value = (Map<String, Object>) formFieldValue;
        Object id = value.get("id");
        if (id instanceof Number) {
          result = ((Number) id).longValue();
          
        } else {
          // Wrong type, ignore
          result = null;
        }
      } else {
        // Incorrect or empty map, ignore
        result = null;
      }
    }

    // Default: no processing needs to be done, can be stored as-is
    return result;
  }
}