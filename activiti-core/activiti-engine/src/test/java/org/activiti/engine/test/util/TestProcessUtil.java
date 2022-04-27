/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.engine.test.util;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;

import java.util.UUID;


public class TestProcessUtil {

  /**
   * Since the 'one task process' is used everywhere the actual process content doesn't matter, instead of copying around the BPMN 2.0 xml one could use this method which gives a {@link BpmnModel}
   * version of the same process back.
   */
  public static BpmnModel createOneTaskBpmnModel() {
    BpmnModel model = new BpmnModel();
    model.addProcess(createOneTaskProcess());
    return model;
  }

  public static org.activiti.bpmn.model.Process createOneTaskProcessWithId(String id) {
    org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();

    process.setId(id);
    process.setExecutable(true);
    process.setName("The one task process");

    StartEvent startEvent = new StartEvent();
    startEvent.setId("start");
    process.addFlowElement(startEvent);

    UserTask userTask = new UserTask();
    userTask.setName("The Task");
    userTask.setId("theTask");
    userTask.setAssignee("kermit");
    process.addFlowElement(userTask);

    EndEvent endEvent = new EndEvent();
    endEvent.setId("theEnd");
    process.addFlowElement(endEvent);

    process.addFlowElement(new SequenceFlow("start", "theTask"));
    process.addFlowElement(new SequenceFlow("theTask", "theEnd"));

    return process;
}

public static org.activiti.bpmn.model.Process createOneTaskProcess() {
    return createOneTaskProcessWithId(UUID.randomUUID().toString());
}

  public static BpmnModel createTwoTasksBpmnModel() {
    BpmnModel model = new BpmnModel();
    org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
    model.addProcess(process);
    process.setId("twoTasksProcess");
    process.setName("The two tasks process");

    StartEvent startEvent = new StartEvent();
    startEvent.setId("start");
    process.addFlowElement(startEvent);

    UserTask userTask = new UserTask();
    userTask.setName("The First Task");
    userTask.setId("task1");
    userTask.setAssignee("kermit");
    process.addFlowElement(userTask);

    UserTask userTask2 = new UserTask();
    userTask2.setName("The Second Task");
    userTask2.setId("task2");
    userTask2.setAssignee("kermit");
    process.addFlowElement(userTask2);

    EndEvent endEvent = new EndEvent();
    endEvent.setId("theEnd");
    process.addFlowElement(endEvent);

    process.addFlowElement(new SequenceFlow("start", "task1"));
    process.addFlowElement(new SequenceFlow("start", "task2"));
    process.addFlowElement(new SequenceFlow("task1", "theEnd"));
    process.addFlowElement(new SequenceFlow("task2", "theEnd"));

    return model;
  }

}
