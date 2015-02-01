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
package org.activiti.cdi.impl.event;

import java.util.HashSet;
import java.util.Set;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.SendTask;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.Transaction;
import org.activiti.bpmn.model.UserTask;
import org.activiti.cdi.BusinessProcessEventType;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.parse.BpmnParseHandler;

/**
 * {@link BpmnParseHandler} registering the {@link CdiExecutionListener} for
 * distributing execution events using the cdi event infrastructure
 * 
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class CdiEventSupportBpmnParseHandler implements BpmnParseHandler {

  protected static final Set<Class<? extends BaseElement>> supportedTypes = new HashSet<Class<? extends BaseElement>>();
  
  static {
    supportedTypes.add(StartEvent.class);
    supportedTypes.add(EndEvent.class);
    supportedTypes.add(ExclusiveGateway.class);
    supportedTypes.add(InclusiveGateway.class);
    supportedTypes.add(ParallelGateway.class);
    supportedTypes.add(ScriptTask.class);
    supportedTypes.add(ServiceTask.class);
    supportedTypes.add(BusinessRuleTask.class);
    supportedTypes.add(Task.class);
    supportedTypes.add(ManualTask.class);
    supportedTypes.add(UserTask.class);
    supportedTypes.add(EndEvent.class);
    supportedTypes.add(SubProcess.class);
    supportedTypes.add(EventSubProcess.class);
    supportedTypes.add(CallActivity.class);
    supportedTypes.add(SendTask.class);
    supportedTypes.add(ReceiveTask.class);
    supportedTypes.add(EventGateway.class);
    supportedTypes.add(Transaction.class);
    supportedTypes.add(ThrowEvent.class);
    
    supportedTypes.add(TimerEventDefinition.class); 
    supportedTypes.add(ErrorEventDefinition.class);
    supportedTypes.add(SignalEventDefinition.class);
    
    supportedTypes.add(SequenceFlow.class);
  }
  
  public Set<Class< ? extends BaseElement>> getHandledTypes() {
    return supportedTypes;
  }
  
  public void parse(BpmnParse bpmnParse, BaseElement element) {
    if (element instanceof SequenceFlow) {
      TransitionImpl transition = bpmnParse.getSequenceFlows().get(element.getId());
      transition.addExecutionListener(new CdiExecutionListener(transition.getId()));
    } else {
      ActivityImpl activity = bpmnParse.getCurrentScope().findActivity(element.getId());
      if (element instanceof UserTask) {
        addCreateListener(activity);
        addAssignListener(activity);
        addCompleteListener(activity);
        addDeleteListener(activity);
      }
      if (activity != null) {
        addStartEventListener(activity);
        addEndEventListener(activity);
      }
    }
  }
  
  private void addCompleteListener(ActivityImpl activity) {
	UserTaskActivityBehavior behavior = getUserTaskActivityBehavior(activity.getActivityBehavior());
    behavior.getTaskDefinition().addTaskListener(TaskListener.EVENTNAME_COMPLETE, new CdiTaskListener(activity.getId(), BusinessProcessEventType.COMPLETE_TASK));
  }

  private void addAssignListener(ActivityImpl activity) {
    UserTaskActivityBehavior behavior = getUserTaskActivityBehavior(activity.getActivityBehavior());
    behavior.getTaskDefinition().addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, new CdiTaskListener(activity.getId(), BusinessProcessEventType.ASSIGN_TASK));
  }

  private void addCreateListener(ActivityImpl activity) {
	UserTaskActivityBehavior behavior = getUserTaskActivityBehavior(activity.getActivityBehavior());
    behavior.getTaskDefinition().addTaskListener(TaskListener.EVENTNAME_CREATE, new CdiTaskListener(activity.getId(), BusinessProcessEventType.CREATE_TASK));
  }

  protected void addDeleteListener(ActivityImpl activity) {
    UserTaskActivityBehavior behavior = getUserTaskActivityBehavior(activity.getActivityBehavior());
      behavior.getTaskDefinition().addTaskListener(TaskListener.EVENTNAME_DELETE, new CdiTaskListener(activity.getId(), BusinessProcessEventType.DELETE_TASK));
  }
  protected void addEndEventListener(ActivityImpl activity) {
    activity.addExecutionListener(ExecutionListener.EVENTNAME_END, new CdiExecutionListener(activity.getId(), BusinessProcessEventType.END_ACTIVITY));
  }

  protected void addStartEventListener(ActivityImpl activity) {
    activity.addExecutionListener(ExecutionListener.EVENTNAME_START, new CdiExecutionListener(activity.getId(), BusinessProcessEventType.START_ACTIVITY));
  }

  private UserTaskActivityBehavior getUserTaskActivityBehavior(ActivityBehavior behavior) {
	  if (behavior instanceof UserTaskActivityBehavior) {
		  return (UserTaskActivityBehavior)behavior;
	  } else if (behavior instanceof MultiInstanceActivityBehavior) {
		  return (UserTaskActivityBehavior)((MultiInstanceActivityBehavior)behavior).getInnerActivityBehavior();
	  }
	  
	  return null;
  }
}
