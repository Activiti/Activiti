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
package org.activiti.editor.form.converter;

import org.activiti.form.model.FormDefinition;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
public class FormJsonConverter {

  protected ObjectMapper objectMapper = new ObjectMapper();

  public FormDefinition convertToForm(String modelJson, String modelId, int modelVersion) {
    try {
      FormDefinition definition = objectMapper.readValue(modelJson, FormDefinition.class);
      definition.setId(modelId);
      definition.setVersion(modelVersion);
  
      return definition;
    } catch (Exception e) {
      throw new ActivitiFormJsonException("Error reading form json", e);
    }
  }

  public String convertToJson(FormDefinition definition) {
    try {
      return objectMapper.writeValueAsString(definition);
    } catch (Exception e) {
      throw new ActivitiFormJsonException("Error writing form json", e);
    }
  }
}