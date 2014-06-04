package org.activiti.crystalball.simulator.impl.bpmn.parser.handler;

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


import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;

/**
 * This class changes UserTaskBehavior for simulation purposes.
 *
 * @author martin.grofcik
 */
public class AddListenerUserTaskParseHandler extends UserTaskParseHandler {

  private final String eventName;
  private final TaskListener taskListener;

  public AddListenerUserTaskParseHandler(String eventName, TaskListener taskListener) {
    this.eventName = eventName;
    this.taskListener = taskListener;
  }
  protected void executeParse(BpmnParse bpmnParse, UserTask userTask) {
    super.executeParse(bpmnParse, userTask);

    ScopeImpl scope = bpmnParse.getCurrentScope();
    ProcessDefinitionImpl processDefinition = scope.getProcessDefinition();
    ActivityImpl activity = processDefinition.findActivity(userTask.getId());

    SimulatorParserUtils.setSimulationBehavior(scope, userTask);

    UserTaskActivityBehavior userTaskActivity = (UserTaskActivityBehavior) activity.getActivityBehavior();
    userTaskActivity.getTaskDefinition().addTaskListener(eventName, taskListener);

  }
}
