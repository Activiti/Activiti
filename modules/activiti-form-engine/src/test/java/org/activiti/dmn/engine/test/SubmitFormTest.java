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
package org.activiti.dmn.engine.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.activiti.form.api.SubmittedForm;
import org.activiti.form.engine.test.FormDeploymentAnnotation;
import org.activiti.form.model.FormDefinition;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class SubmitFormTest extends AbstractActivitiFormTest {
  
  @Test
  @FormDeploymentAnnotation(resources = "org/activiti/form/engine/test/deployment/simple.form")
  public void submitSimpleForm() throws Exception {
    FormDefinition formDefinition = repositoryService.getFormDefinitionByKey("form1");
    
    Map<String, Object> valuesMap = new HashMap<String, Object>();
    valuesMap.put("input1", "test");
    Map<String, Object> formValues = formService.getVariablesFromFormSubmission(formDefinition, valuesMap, "default");
    assertEquals("test", formValues.get("input1"));
    
    SubmittedForm submittedForm = formService.storeSubmittedForm(formValues, formDefinition, null, null);
    assertEquals(formDefinition.getId(), submittedForm.getFormId());
    JsonNode formNode = formEngineConfiguration.getObjectMapper().readTree(submittedForm.getFormValueBytes());
    assertEquals("test", formNode.get("values").get("input1").asText());
    assertEquals("default", formNode.get("activiti_form_outcome").asText());
  }
  
  @Test
  @FormDeploymentAnnotation(resources = "org/activiti/form/engine/test/deployment/form_with_dates.form")
  public void submitDateForm() throws Exception {
    FormDefinition formDefinition = repositoryService.getFormDefinitionByKey("dateform");
    
    Map<String, Object> valuesMap = new HashMap<String, Object>();
    valuesMap.put("input1", "test");
    valuesMap.put("date1", "2016-01-01");
    valuesMap.put("date2", "2017-01-01");
    Map<String, Object> formValues = formService.getVariablesFromFormSubmission(formDefinition, valuesMap, "date");
    assertEquals("test", formValues.get("input1"));
    assertEquals(new LocalDate(2016, 1, 1), formValues.get("date1"));
    assertEquals(new LocalDate(2017, 1, 1), formValues.get("date2"));
    
    SubmittedForm submittedForm = formService.storeSubmittedForm(formValues, formDefinition, null, null);
    assertEquals(formDefinition.getId(), submittedForm.getFormId());
    JsonNode formNode = formEngineConfiguration.getObjectMapper().readTree(submittedForm.getFormValueBytes());
    JsonNode valuesNode = formNode.get("values");
    assertEquals(3, valuesNode.size());
    assertEquals("test", valuesNode.get("input1").asText());
    assertEquals("2016-01-01", valuesNode.get("date1").asText());
    assertEquals("2017-01-01", valuesNode.get("date2").asText());
    assertEquals("date", formNode.get("activiti_form_outcome").asText());
  }
}
