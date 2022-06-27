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

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.CollectionUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the BPMN 2.0 subprocess (formally known as 'embedded' subprocess): a subprocess defined within another process definition.
 *

 */
public class SubProcessActivityBehavior extends AbstractBpmnActivityBehavior {

  private static final long serialVersionUID = 1L;

  public void execute(DelegateExecution execution) {
    SubProcess subProcess = getSubProcessFromExecution(execution);

    FlowElement startElement = null;
    if (CollectionUtil.isNotEmpty(subProcess.getFlowElements())) {
      for (FlowElement subElement : subProcess.getFlowElements()) {
        if (subElement instanceof StartEvent) {
          StartEvent startEvent = (StartEvent) subElement;

          // start none event
          if (CollectionUtil.isEmpty(startEvent.getEventDefinitions())) {
            startElement = startEvent;
            break;
          }
        }
      }
    }

    if (startElement == null) {
      throw new ActivitiException("No initial activity found for subprocess " + subProcess.getId());
    }

    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    executionEntity.setScope(true);

    // initialize the template-defined data objects as variables
    Map<String, Object> dataObjectVars = processDataObjects(subProcess.getDataObjects());
    if (dataObjectVars != null) {
      executionEntity.setVariablesLocal(dataObjectVars);
    }

    ExecutionEntity startSubProcessExecution = Context.getCommandContext().getExecutionEntityManager()
        .createChildExecution(executionEntity);
    startSubProcessExecution.setCurrentFlowElement(startElement);
    Context.getAgenda().planContinueProcessOperation(startSubProcessExecution);
  }

  protected SubProcess getSubProcessFromExecution(DelegateExecution execution) {
    FlowElement flowElement = execution.getCurrentFlowElement();
    SubProcess subProcess = null;
    if (flowElement instanceof SubProcess) {
      subProcess = (SubProcess) flowElement;
    } else {
      throw new ActivitiException("Programmatic error: sub process behaviour can only be applied" + " to a SubProcess instance, but got an instance of " + flowElement);
    }
    return subProcess;
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
