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


package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the BPMN 2.0 event subprocess start event.
 *

 */
public class EventSubProcessErrorStartEventActivityBehavior extends AbstractBpmnActivityBehavior {

  private static final long serialVersionUID = 1L;

  public void execute(DelegateExecution execution) {
    StartEvent startEvent = (StartEvent) execution.getCurrentFlowElement();
    EventSubProcess eventSubProcess = (EventSubProcess) startEvent.getSubProcess();
    execution.setCurrentFlowElement(eventSubProcess);
    execution.setScope(true);

    // initialize the template-defined data objects as variables
    Map<String, Object> dataObjectVars = processDataObjects(eventSubProcess.getDataObjects());
    if (dataObjectVars != null) {
      execution.setVariablesLocal(dataObjectVars);
    }

    ExecutionEntity startSubProcessExecution = Context.getCommandContext()
        .getExecutionEntityManager().createChildExecution((ExecutionEntity) execution);
    startSubProcessExecution.setCurrentFlowElement(startEvent);
    Context.getAgenda().planTakeOutgoingSequenceFlowsOperation(startSubProcessExecution, true);
  }

  protected Map<String, Object> processDataObjects(Collection<ValuedDataObject> dataObjects) {
    Map<String, Object> variablesMap = new HashMap<String, Object>();
    // convert data objects to process variables
    if (dataObjects != null) {
      for (ValuedDataObject dataObject : dataObjects) {
        variablesMap.put(dataObject.getName(), dataObject.getValue());
      }
    }
    return variablesMap;
  }
}
