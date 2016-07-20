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
package org.activiti.form.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
@JsonInclude(Include.NON_NULL)
public class FormDefinition implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String name;
  protected String description;
  protected String key;
  protected int version;
  protected List<FormField> fields;
  protected List<FormOutcome> outcomes;
  protected String outcomeVariableName;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  /**
   * Do not use this method for logical operations since it only return the top level fields. I.e. A "container" field's sub fields are not returned. For verifying and listing all fields from a form
   * use instead listAllFields().
   *
   * @return The top level fields, a container's sub fields are not returned.
   */
  public List<FormField> getFields() {
    return fields;
  }

  public void setFields(List<FormField> fields) {
    this.fields = fields;
  }

  public List<FormOutcome> getOutcomes() {
    return outcomes;
  }

  public void setOutcomes(List<FormOutcome> outcomes) {
    this.outcomes = outcomes;
  }

  public String getOutcomeVariableName() {
    return outcomeVariableName;
  }

  public void setOutcomeVariableName(String outcomeVariableName) {
    this.outcomeVariableName = outcomeVariableName;
  }

  /*
   * Helper methods
   */
  public Map<String, FormField> allFieldsAsMap() {
    Map<String, FormField> result = new HashMap<String, FormField>();
    List<FormField> allFields = listAllFields();
    if (allFields != null) {
      for (FormField field : allFields) {
        if (result.containsKey(field.getId()) == false || ("readonly".equals(field.getType()) == false && "readonly-text".equals(field.getType()) == false)) {

          result.put(field.getId(), field);
        }
      }
    }
    return result;
  }

  public List<FormField> listAllFields() {
    List<FormField> listOfAllFields = new ArrayList<FormField>();
    collectSubFields(fields, listOfAllFields);
    return listOfAllFields;
  }

  protected void collectSubFields(List<FormField> fields, List<FormField> listOfAllFields) {
    if (fields != null && fields.size() > 0) {
      for (FormField field : fields) {
        listOfAllFields.add(field);
        if (field instanceof FormContainer) {
          FormContainer container = (FormContainer) field;
          List<List<FormField>> subFields = container.getFields();
          if (subFields != null) {
            for (List<FormField> subFieldDefinitions : subFields) {
              if (subFieldDefinitions != null) {
                collectSubFields(subFieldDefinitions, listOfAllFields);
              }
            }
          }
        }
      }
    }
  }

}
