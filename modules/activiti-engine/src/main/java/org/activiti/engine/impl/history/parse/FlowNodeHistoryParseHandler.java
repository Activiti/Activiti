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
package org.activiti.engine.impl.history.parse;

import java.util.HashSet;
import java.util.Set;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.SendTask;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.parse.BpmnParseHandler;

/**
 * @author Joram Barrez
 */
public class FlowNodeHistoryParseHandler implements BpmnParseHandler {

  protected static final String ACTIVITY_INSTANCE_START_LISTENER = "org.activiti.engine.impl.history.handler.ActivityInstanceStartHandler";

  protected static final String ACTIVITI_INSTANCE_END_LISTENER = "org.activiti.engine.impl.history.handler.ActivityInstanceEndHandler";

  protected static Set<Class<? extends BaseElement>> supportedElementClasses = new HashSet<Class<? extends BaseElement>>();

  static {
    supportedElementClasses.add(StartEvent.class);
    supportedElementClasses.add(EndEvent.class);
    supportedElementClasses.add(ThrowEvent.class);
    supportedElementClasses.add(BoundaryEvent.class);
    supportedElementClasses.add(IntermediateCatchEvent.class);

    supportedElementClasses.add(ExclusiveGateway.class);
    supportedElementClasses.add(InclusiveGateway.class);
    supportedElementClasses.add(ParallelGateway.class);
    supportedElementClasses.add(EventGateway.class);

    supportedElementClasses.add(Task.class);
    supportedElementClasses.add(ManualTask.class);
    supportedElementClasses.add(ReceiveTask.class);
    supportedElementClasses.add(ScriptTask.class);
    supportedElementClasses.add(ServiceTask.class);
    supportedElementClasses.add(BusinessRuleTask.class);
    supportedElementClasses.add(SendTask.class);
    supportedElementClasses.add(UserTask.class);

    supportedElementClasses.add(CallActivity.class);
    supportedElementClasses.add(SubProcess.class);
  }

  public Set<Class<? extends BaseElement>> getHandledTypes() {
    return supportedElementClasses;
  }

  public void parse(BpmnParse bpmnParse, BaseElement element) {
    if (element instanceof BoundaryEvent) {
      // A boundary-event never receives an activity start-event
      BoundaryEvent boundaryEvent = (BoundaryEvent) element;
      addExecutionListener("end", ACTIVITY_INSTANCE_START_LISTENER, boundaryEvent);
      addExecutionListener("end", ACTIVITI_INSTANCE_END_LISTENER, boundaryEvent);
    } else {
      FlowElement flowElement = (FlowElement) element;
      addExecutionListener("start", ACTIVITY_INSTANCE_START_LISTENER, flowElement);
      addExecutionListener("end", ACTIVITI_INSTANCE_END_LISTENER, flowElement);
    }
  }

  protected void addExecutionListener(String event, String className, FlowElement element) {
    ActivitiListener listener = new ActivitiListener();
    listener.setEvent(event);
    listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
    listener.setImplementation(className);
    element.getExecutionListeners().add(listener);
  }

}
