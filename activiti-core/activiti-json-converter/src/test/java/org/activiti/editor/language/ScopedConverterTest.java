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

import java.util.List;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

public class ScopedConverterTest extends AbstractConverterTest {

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

  protected String getResource() {
    return "test.scopedmodel.json";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("outerSubProcess", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(SubProcess.class);
    assertThat(flowElement.getId()).isEqualTo("outerSubProcess");
    SubProcess outerSubProcess = (SubProcess) flowElement;
    List<BoundaryEvent> eventList = outerSubProcess.getBoundaryEvents();
    assertThat(eventList).hasSize(1);
    BoundaryEvent boundaryEvent = eventList.get(0);
    assertThat(boundaryEvent.getId()).isEqualTo("outerBoundaryEvent");

    FlowElement subElement = outerSubProcess.getFlowElement("innerSubProcess");
    assertThat(subElement).isNotNull();
    assertThat(subElement).isInstanceOf(SubProcess.class);
    assertThat(subElement.getId()).isEqualTo("innerSubProcess");
    SubProcess innerSubProcess = (SubProcess) subElement;
    eventList = innerSubProcess.getBoundaryEvents();
    assertThat(eventList).hasSize(1);
    boundaryEvent = eventList.get(0);
    assertThat(boundaryEvent.getId()).isEqualTo("innerBoundaryEvent");

    FlowElement taskElement = innerSubProcess.getFlowElement("usertask");
    assertThat(taskElement).isNotNull();
    assertThat(taskElement).isInstanceOf(UserTask.class);
    UserTask userTask = (UserTask) taskElement;
    assertThat(userTask.getId()).isEqualTo("usertask");
    eventList = userTask.getBoundaryEvents();
    assertThat(eventList).hasSize(1);
    boundaryEvent = eventList.get(0);
    assertThat(boundaryEvent.getId()).isEqualTo("taskBoundaryEvent");
  }
}
