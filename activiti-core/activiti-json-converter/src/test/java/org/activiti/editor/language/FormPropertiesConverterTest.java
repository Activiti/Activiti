/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

public class FormPropertiesConverterTest extends AbstractConverterTest {

  @Test
  public void convertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  protected String getResource() {
    return "test.formpropertiesmodel.json";
  }

  private void validateModel(BpmnModel model) {
    assertThat(model.getMainProcess().getId()).isEqualTo("formPropertiesProcess");
    assertThat(model.getMainProcess().getName()).isEqualTo("User registration");
    assertThat(model.getMainProcess().isExecutable()).isEqualTo(true);

    FlowElement startFlowElement = model.getMainProcess().getFlowElement("startNode", true);
    assertThat(startFlowElement).isNotNull();
    assertThat(startFlowElement).isInstanceOf(StartEvent.class);
    StartEvent startEvent = (StartEvent) startFlowElement;

    List<FormProperty> formProperties = startEvent.getFormProperties();

    assertThat(formProperties).isNotNull();
    assertThat(formProperties).as("Invalid form properties list: ").hasSize(8);

    for (FormProperty formProperty :formProperties) {
      if (formProperty.getId().equals("new_property_1")) {
        checkFormProperty(formProperty, "v000", true, false, false);
      } else if (formProperty.getId().equals("new_property_2")) {
        checkFormProperty(formProperty, "v001", true, false, true);
      } else if (formProperty.getId().equals("new_property_3")) {
        checkFormProperty(formProperty, "v010", true, true, false);
      } else if (formProperty.getId().equals("new_property_4")) {
        checkFormProperty(formProperty, "v011", true, true, true);
      } else if (formProperty.getId().equals("new_property_5")) {
        checkFormProperty(formProperty, "v100", true, false, false);

        List<Map<String, Object>> formValues = new ArrayList<Map<String,Object>>();
        for (FormValue formValue : formProperty.getFormValues()) {
          Map<String, Object> formValueMap = new HashMap<String, Object>();
          formValueMap.put("id", formValue.getId());
          formValueMap.put("name", formValue.getName());
          formValues.add(formValueMap);
        }
        checkFormPropertyFormValues(formValues);

      } else if (formProperty.getId().equals("new_property_6")) {
        checkFormProperty(formProperty, "v101", true, false, true);
      } else if (formProperty.getId().equals("new_property_7")) {
        checkFormProperty(formProperty, "v110", true, true, false);
      } else if (formProperty.getId().equals("new_property_8")) {
        checkFormProperty(formProperty, "v111", true, true, true);
      } else {
        fail("unexpected form property id " + formProperty.getId());
      }
    }

    FlowElement userFlowElement = model.getMainProcess().getFlowElement("userTask", true);
    assertThat(userFlowElement).isNotNull();
    assertThat(userFlowElement).isInstanceOf(UserTask.class);
    UserTask userTask = (UserTask) userFlowElement;

    formProperties = userTask.getFormProperties();

    assertThat(formProperties).isNotNull();
    assertThat(formProperties).as("Invalid form properties list: ").hasSize(8);

    for (FormProperty formProperty :formProperties) {
      if (formProperty.getId().equals("new_property_1")) {
        checkFormProperty(formProperty, "v000", false, false, false);
      } else if (formProperty.getId().equals("new_property_2")) {
        checkFormProperty(formProperty, "v001", false, false, true);
      } else if (formProperty.getId().equals("new_property_3")) {
        checkFormProperty(formProperty, "v010", false, true, false);
      } else if (formProperty.getId().equals("new_property_4")) {
        checkFormProperty(formProperty, "v011", false, true, true);
      } else if (formProperty.getId().equals("new_property_5")) {
        checkFormProperty(formProperty, "v100", true, false, false);

        List<Map<String, Object>> formValues = new ArrayList<Map<String,Object>>();
        for (FormValue formValue : formProperty.getFormValues()) {
          Map<String, Object> formValueMap = new HashMap<String, Object>();
          formValueMap.put("id", formValue.getId());
          formValueMap.put("name", formValue.getName());
          formValues.add(formValueMap);
        }
        checkFormPropertyFormValues(formValues);

      } else if (formProperty.getId().equals("new_property_6")) {
        checkFormProperty(formProperty, "v101", true, false, true);
      } else if (formProperty.getId().equals("new_property_7")) {
        checkFormProperty(formProperty, "v110", true, true, false);
      } else if (formProperty.getId().equals("new_property_8")) {
        checkFormProperty(formProperty, "v111", true, true, true);
      } else {
        fail("unexpected form property id " + formProperty.getId());
      }
    }

  }

  private void checkFormProperty(FormProperty formProperty, String name, boolean shouldBeRequired, boolean shouldBeReadable, boolean shouldBeWritable) {
    assertThat(formProperty.getName()).isEqualTo(name);
    assertThat(formProperty.isRequired()).isEqualTo(shouldBeRequired);
    assertThat(formProperty.isReadable()).isEqualTo(shouldBeReadable);
    assertThat(formProperty.isWriteable()).isEqualTo(shouldBeWritable);
  }

  private void checkFormPropertyFormValues(List<Map<String, Object>> formValues) {
    List<Map<String, Object>> expectedFormValues = new ArrayList<Map<String,Object>>();
    Map<String, Object> formValue1 = new HashMap<String, Object>();
    formValue1.put("id", "value1");
    formValue1.put("name", "Value 1");
    Map<String, Object> formValue2 = new HashMap<String, Object>();
    formValue2.put("id", "value2");
    formValue2.put("name", "Value 2");

    Map<String, Object> formValue3 = new HashMap<String, Object>();
    formValue3.put("id", "value3");
    formValue3.put("name", "Value 3");

    Map<String, Object> formValue4 = new HashMap<String, Object>();
    formValue4.put("id", "value4");
    formValue4.put("name", "Value 4");

    expectedFormValues.add(formValue1);
    expectedFormValues.add(formValue2);
    expectedFormValues.add(formValue3);
    expectedFormValues.add(formValue4);

    assertThat(formValues).isEqualTo(expectedFormValues);
  }
}
