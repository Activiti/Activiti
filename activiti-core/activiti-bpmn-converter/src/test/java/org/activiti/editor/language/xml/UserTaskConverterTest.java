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
package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.UserTask;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class UserTaskConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    validateModel(parsedModel);
    deployProcess(parsedModel);
  }

  protected String getResource() {
    return "usertaskmodel.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("usertask");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(UserTask.class);
    assertThat(flowElement.getId()).isEqualTo("usertask");
    UserTask userTask = (UserTask) flowElement;
    assertThat(userTask.getId()).isEqualTo("usertask");
    assertThat(userTask.getName()).isEqualTo("User task");
    assertThat(userTask.getCategory()).isEqualTo("Test Category");
    assertThat(userTask.getFormKey()).isEqualTo("testKey");
    assertThat(userTask.getPriority()).isEqualTo("40");
    assertThat(userTask.getDueDate()).isEqualTo("2012-11-01");

    assertThat(userTask.getBusinessCalendarName()).isEqualTo("customCalendarName");

    assertThat(userTask.getAssignee()).isEqualTo("kermit");
    assertThat(userTask.getCandidateUsers()).hasSize(2);
    assertThat(userTask.getCandidateUsers().contains("kermit")).isTrue();
    assertThat(userTask.getCandidateUsers().contains("fozzie")).isTrue();
    assertThat(userTask.getCandidateGroups()).hasSize(2);
    assertThat(userTask.getCandidateGroups().contains("management")).isTrue();
    assertThat(userTask.getCandidateGroups().contains("sales")).isTrue();

    assertThat(userTask.getCustomUserIdentityLinks()).hasSize(1);
    assertThat(userTask.getCustomGroupIdentityLinks()).hasSize(2);
    assertThat(userTask.getCustomUserIdentityLinks().get("businessAdministrator").contains("kermit")).isTrue();
    assertThat(userTask.getCustomGroupIdentityLinks().get("manager").contains("management")).isTrue();
    assertThat(userTask.getCustomGroupIdentityLinks().get("businessAdministrator").contains("management")).isTrue();

    List<FormProperty> formProperties = userTask.getFormProperties();
    assertThat(formProperties).hasSize(3);
    FormProperty formProperty = formProperties.get(0);
    assertThat(formProperty.getId()).isEqualTo("formId");
    assertThat(formProperty.getName()).isEqualTo("formName");
    assertThat(formProperty.getType()).isEqualTo("string");
    assertThat(formProperty.getVariable()).isEqualTo("variable");
    assertThat(formProperty.getExpression()).isEqualTo("${expression}");
    formProperty = formProperties.get(1);
    assertThat(formProperty.getId()).isEqualTo("formId2");
    assertThat(formProperty.getName()).isEqualTo("anotherName");
    assertThat(formProperty.getType()).isEqualTo("long");
    assertThat(StringUtils.isEmpty(formProperty.getVariable())).isTrue();
    assertThat(StringUtils.isEmpty(formProperty.getExpression())).isTrue();
    formProperty = formProperties.get(2);
    assertThat(formProperty.getId()).isEqualTo("formId3");
    assertThat(formProperty.getName()).isEqualTo("enumName");
    assertThat(formProperty.getType()).isEqualTo("enum");
    assertThat(StringUtils.isEmpty(formProperty.getVariable())).isTrue();
    assertThat(StringUtils.isEmpty(formProperty.getExpression())).isTrue();
    assertThat(formProperty.getFormValues()).hasSize(2);

    List<ActivitiListener> listeners = userTask.getTaskListeners();
    assertThat(listeners).hasSize(3);
    ActivitiListener listener = listeners.get(0);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("org.test.TestClass");
    assertThat(listener.getEvent()).isEqualTo("create");
    assertThat(listener.getOnTransaction()).isEqualTo("before-commit");
    assertThat(listener.getCustomPropertiesResolverImplementation()).isEqualTo("org.test.TestResolverClass");
    listener = listeners.get(1);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("${someExpression}");
    assertThat(listener.getEvent()).isEqualTo("assignment");
    assertThat(listener.getOnTransaction()).isEqualTo("committed");
    assertThat(listener.getCustomPropertiesResolverImplementation()).isEqualTo("${testResolverExpression}");
    listener = listeners.get(2);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("${someDelegateExpression}");
    assertThat(listener.getEvent()).isEqualTo("complete");
    assertThat(listener.getOnTransaction()).isEqualTo("rolled-back");
    assertThat(listener.getCustomPropertiesResolverImplementation()).isEqualTo("${delegateResolverExpression}");

    List<ActivitiListener> executionListeners = userTask.getExecutionListeners();
    assertThat(executionListeners).hasSize(1);
    ActivitiListener executionListener = executionListeners.get(0);
    assertThat(executionListener.getEvent()).isEqualTo("end");
    assertThat(executionListener.getOnTransaction()).isEqualTo("before-commit");
    assertThat(executionListener.getCustomPropertiesResolverImplementation()).isEqualTo("org.test.TestResolverClass");

  }
}
