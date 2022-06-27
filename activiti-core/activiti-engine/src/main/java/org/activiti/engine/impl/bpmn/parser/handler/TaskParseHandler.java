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

package org.activiti.engine.impl.bpmn.parser.handler;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.Task;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;


public class TaskParseHandler extends AbstractActivityBpmnParseHandler<Task> {

  public Class<? extends BaseElement> getHandledType() {
    return Task.class;
  }

  protected void executeParse(BpmnParse bpmnParse, Task task) {
    task.setBehavior(bpmnParse.getActivityBehaviorFactory().createTaskActivityBehavior(task));
  }

}
