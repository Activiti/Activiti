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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.StartEvent;
import org.junit.jupiter.api.Test;

public class StartEventConverterTest extends AbstractConverterTest {

  @Test
  public void convertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  @Override
  protected String getResource() {
    return "test.starteventmodel.json";
  }

  private void validateModel(BpmnModel model) {

    FlowElement flowElement = model.getMainProcess().getFlowElement("start", true);
    assertThat(flowElement).isInstanceOf(StartEvent.class);

    StartEvent startEvent = (StartEvent) flowElement;
    assertThat(startEvent.getId()).isEqualTo("start");
    assertThat(startEvent.getName()).isEqualTo("startName");
    assertThat(startEvent.getFormKey()).isEqualTo("startFormKey");
    assertThat(startEvent.getInitiator()).isEqualTo("startInitiator");
    assertThat(startEvent.getDocumentation()).isEqualTo("startDoc");

    assertThat(startEvent.getExecutionListeners()).hasSize(2);
    ActivitiListener executionListener = startEvent.getExecutionListeners().get(0);
    assertThat(executionListener.getEvent()).isEqualTo("start");
    assertThat(executionListener.getImplementation()).isEqualTo("org.test.TestClass");
    assertThat(executionListener.getImplementationType()).isEqualTo(ImplementationType.IMPLEMENTATION_TYPE_CLASS);

    executionListener = startEvent.getExecutionListeners().get(1);
    assertThat(executionListener.getEvent()).isEqualTo("end");
    assertThat(executionListener.getImplementation()).isEqualTo("${someExpression}");
    assertThat(executionListener.getImplementationType()).isEqualTo(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);

    List<FormProperty> formProperties = startEvent.getFormProperties();
    assertThat(formProperties).hasSize(2);

    FormProperty formProperty = formProperties.get(0);
    assertThat(formProperty.getId()).isEqualTo("startFormProp1");
    assertThat(formProperty.getName()).isEqualTo("startFormProp1");
    assertThat(formProperty.getType()).isEqualTo("string");

    formProperty = formProperties.get(1);
    assertThat(formProperty.getId()).isEqualTo("startFormProp2");
    assertThat(formProperty.getName()).isEqualTo("startFormProp2");
    assertThat(formProperty.getType()).isEqualTo("boolean");

  }

}
