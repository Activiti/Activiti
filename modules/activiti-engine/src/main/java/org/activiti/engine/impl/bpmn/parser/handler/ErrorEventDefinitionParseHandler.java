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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Joram Barrez
 */
public class ErrorEventDefinitionParseHandler extends AbstractBpmnParseHandler<ErrorEventDefinition> {
  
  public static final String PROPERTYNAME_INITIAL = "initial";
  
  public Class< ? extends BaseElement> getHandledType() {
    return ErrorEventDefinition.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, ErrorEventDefinition eventDefinition) {

    ErrorEventDefinition modelErrorEvent = (ErrorEventDefinition) eventDefinition;
    if (bpmnParse.getBpmnModel().containsErrorRef(modelErrorEvent.getErrorCode())) {
      String errorCode = bpmnParse.getBpmnModel().getErrors().get(modelErrorEvent.getErrorCode());
      modelErrorEvent.setErrorCode(errorCode);
    }
    
    ScopeImpl scope = bpmnParse.getCurrentScope();
    ActivityImpl activity = bpmnParse.getCurrentActivity();
    if (bpmnParse.getCurrentFlowElement() instanceof StartEvent) {
    
      if (scope.getProperty(PROPERTYNAME_INITIAL) == null) {
        scope.setProperty(PROPERTYNAME_INITIAL, activity);
        
        // the scope of the event subscription is the parent of the event
        // subprocess (subscription must be created when parent is initialized)
        ScopeImpl catchingScope = ((ActivityImpl) scope).getParent();
        
        createErrorStartEventDefinition(modelErrorEvent, activity, catchingScope);
      }
      
    } else if (bpmnParse.getCurrentFlowElement() instanceof BoundaryEvent) {
      
      BoundaryEvent boundaryEvent = (BoundaryEvent) bpmnParse.getCurrentFlowElement();
      boolean interrupting = true; // non-interrupting not yet supported
      activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createBoundaryEventActivityBehavior(boundaryEvent, interrupting, activity));
      ActivityImpl parentActivity = scope.findActivity(boundaryEvent.getAttachedToRefId());
      createBoundaryErrorEventDefinition(modelErrorEvent, interrupting, parentActivity, activity);
      
    }
  }
  
  protected void createErrorStartEventDefinition(ErrorEventDefinition errorEventDefinition, ActivityImpl startEventActivity, ScopeImpl scope) {
    org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition definition = new org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition(startEventActivity.getId());
    if (StringUtils.isNotEmpty(errorEventDefinition.getErrorCode())) {
      definition.setErrorCode(errorEventDefinition.getErrorCode());
    }
    definition.setPrecedence(10);
    addErrorEventDefinition(definition, scope);
  }
  
  public void createBoundaryErrorEventDefinition(ErrorEventDefinition errorEventDefinition, boolean interrupting,
          ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {

    nestedErrorEventActivity.setProperty("type", "boundaryError");
    ScopeImpl catchingScope = nestedErrorEventActivity.getParent();
    ((ActivityImpl) catchingScope).setScope(true);

    org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition definition = 
            new org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition(nestedErrorEventActivity.getId());
    definition.setErrorCode(errorEventDefinition.getErrorCode());

    addErrorEventDefinition(definition, catchingScope);

  }
  
  @SuppressWarnings("unchecked")
  protected void addErrorEventDefinition(org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition errorEventDefinition, ScopeImpl catchingScope) {
    List<org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition> errorEventDefinitions = 
            (List<org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition>) catchingScope.getProperty(PROPERTYNAME_ERROR_EVENT_DEFINITIONS);
    if(errorEventDefinitions == null) {
      errorEventDefinitions = new ArrayList<org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition>();
      catchingScope.setProperty(PROPERTYNAME_ERROR_EVENT_DEFINITIONS, errorEventDefinitions);
    }
    errorEventDefinitions.add(errorEventDefinition);
    Collections.sort(errorEventDefinitions, org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition.comparator);
  }

}
