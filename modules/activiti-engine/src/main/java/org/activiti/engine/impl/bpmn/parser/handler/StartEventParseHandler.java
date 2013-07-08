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
package org.activiti.engine.impl.bpmn.parser.handler;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.form.DefaultStartFormHandler;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Joram Barrez
 */
public class StartEventParseHandler extends AbstractActivityBpmnParseHandler<StartEvent> {
  
  public static final String PROPERTYNAME_INITIATOR_VARIABLE_NAME = "initiatorVariableName";
  public static final String PROPERTYNAME_INITIAL = "initial";
  
  @Override
  public Class< ? extends BaseElement> getHandledType() {
    return StartEvent.class;
  }
  
  @Override
  protected void executeParse(BpmnParse bpmnParse, StartEvent startEvent) {
    ActivityImpl startEventActivity = createActivityOnCurrentScope(bpmnParse, startEvent, BpmnXMLConstants.ELEMENT_EVENT_START);

    ScopeImpl scope = bpmnParse.getCurrentScope();
    if (scope instanceof ProcessDefinitionEntity) {
      createProcessDefinitionStartEvent(bpmnParse, startEventActivity, startEvent, (ProcessDefinitionEntity) scope);
      selectInitial(bpmnParse, startEventActivity, startEvent, (ProcessDefinitionEntity) scope);
      createStartFormHandlers(bpmnParse, startEvent, (ProcessDefinitionEntity) scope);
    } else {
      createScopeStartEvent(bpmnParse, startEventActivity, startEvent);
    }
  }
  
  protected void selectInitial(BpmnParse bpmnParse, ActivityImpl startEventActivity, StartEvent startEvent, ProcessDefinitionEntity processDefinition) {
    if (processDefinition.getInitial() == null) {
      processDefinition.setInitial(startEventActivity);
    } else {
      // validate that there is s single none start event / timer start event:
      if (startEventActivity.getProperty("type").equals("messageStartEvent") == false) {
        String currentInitialType = (String) processDefinition.getInitial().getProperty("type");
        if (currentInitialType.equals("messageStartEvent")) {
          processDefinition.setInitial(startEventActivity);
        } else {
          bpmnParse.getBpmnModel().addProblem("multiple none start events or timer start events not supported on process definition.", startEvent);
        }
      }
    }
  }
  
  protected void createStartFormHandlers(BpmnParse bpmnParse, StartEvent startEvent, ProcessDefinitionEntity processDefinition) {
    if (processDefinition.getInitial() != null) {
      if (startEvent.getId().equals(processDefinition.getInitial().getId())) {
        StartFormHandler startFormHandler = new DefaultStartFormHandler();
        startFormHandler.parseConfiguration(startEvent.getFormProperties(), startEvent.getFormKey(), bpmnParse.getDeployment(), processDefinition);
        processDefinition.setStartFormHandler(startFormHandler);
      }
    }
  }
  
  protected void createProcessDefinitionStartEvent(BpmnParse bpmnParse, ActivityImpl startEventActivity, StartEvent startEvent, ProcessDefinitionEntity processDefinition) {
    if (StringUtils.isNotEmpty(startEvent.getInitiator())) {
      processDefinition.setProperty(PROPERTYNAME_INITIATOR_VARIABLE_NAME, startEvent.getInitiator());
    }

    // all start events share the same behavior:
    startEventActivity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createNoneStartEventActivityBehavior(startEvent));
    if (startEvent.getEventDefinitions().size() > 0) {
      EventDefinition eventDefinition = startEvent.getEventDefinitions().get(0);
      if (eventDefinition instanceof TimerEventDefinition || eventDefinition instanceof MessageEventDefinition) {
        bpmnParse.getBpmnParserHandlers().parseElement(bpmnParse, eventDefinition);
      } else {
        bpmnParse.getBpmnModel().addProblem("Unsupported event definition on start event", eventDefinition);
      }
    }
  }
  
  protected void createScopeStartEvent(BpmnParse bpmnParse, ActivityImpl startEventActivity, StartEvent startEvent) {

    ScopeImpl scope = bpmnParse.getCurrentScope();
    Object triggeredByEvent = scope.getProperty("triggeredByEvent");
    boolean isTriggeredByEvent = triggeredByEvent != null && ((Boolean) triggeredByEvent == true);
    
    if (isTriggeredByEvent) { // event subprocess
      
      // all start events of an event subprocess share common behavior
      EventSubProcessStartEventActivityBehavior activityBehavior = 
              bpmnParse.getActivityBehaviorFactory().createEventSubProcessStartEventActivityBehavior(startEvent, startEventActivity.getId()); 
      startEventActivity.setActivityBehavior(activityBehavior);
      
      if (startEvent.getEventDefinitions().size() > 0) {
        EventDefinition eventDefinition = startEvent.getEventDefinitions().get(0);
        
        if (eventDefinition instanceof org.activiti.bpmn.model.ErrorEventDefinition 
                || eventDefinition instanceof MessageEventDefinition
                || eventDefinition instanceof SignalEventDefinition) {
          bpmnParse.getBpmnParserHandlers().parseElement(bpmnParse, eventDefinition);
        } else {
          bpmnParse.getBpmnModel().addProblem("start event of event subprocess must be of type 'error', 'message' or 'signal' ", startEvent);
        }
      }
      
    } else { // "regular" subprocess
      
      if(startEvent.getEventDefinitions().size() > 0) {
        bpmnParse.getBpmnModel().addProblem("event definitions only allowed on start event if subprocess is an event subprocess", startEvent);
      }
      if (scope.getProperty(PROPERTYNAME_INITIAL) == null) {
        scope.setProperty(PROPERTYNAME_INITIAL, startEventActivity);
        startEventActivity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createNoneStartEventActivityBehavior(startEvent));
      } else {
        bpmnParse.getBpmnModel().addProblem("multiple start events not supported for subprocess", bpmnParse.getCurrentSubProcess());
      }
    }

  }

}
