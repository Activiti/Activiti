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

package org.activiti.engine.impl.history.handler;

import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;
import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.process.ScopeImpl;
import org.activiti.pvm.impl.process.TransitionImpl;


/**
 * @author Tom Baeyens
 */
public class HistoryParseListener implements BpmnParseListener {

  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
    processDefinition.addEventListener(EventListener.EVENTNAME_START, new ProcessInstanceStartHandler());
    processDefinition.addEventListener(EventListener.EVENTNAME_END, new ProcessInstanceEndHandler());
  }

  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
  }

  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(exclusiveGwElement, activity);
  }

  public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseScript(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
  }


  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
  }

  public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
  }

  public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
  }

  // helper methods ///////////////////////////////////////////////////////////
  
  protected void addActivityHandlers(Element activityElement, ActivityImpl activity) {
    String activityType = activityElement.getTagName();
    activity.addEventListener(EventListener.EVENTNAME_START, new ActivityInstanceStartHandler(activityType));
    activity.addEventListener(EventListener.EVENTNAME_END, new ActivityInstanceEndHandler());
  }
}
