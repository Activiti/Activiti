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


package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.AdhocSubProcess;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


public class GetEnabledActivitiesForAdhocSubProcessCmd implements Command<List<FlowNode>>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String executionId;

  public GetEnabledActivitiesForAdhocSubProcessCmd(String executionId) {
    this.executionId = executionId;
  }

  public List<FlowNode> execute(CommandContext commandContext) {
    ExecutionEntity execution = commandContext.getExecutionEntityManager().findById(executionId);
    if (execution == null) {
      throw new ActivitiObjectNotFoundException("No execution found for id '" + executionId + "'", ExecutionEntity.class);
    }

    if (!(execution.getCurrentFlowElement() instanceof AdhocSubProcess)) {
      throw new ActivitiException("The current flow element of the requested execution is not an ad-hoc sub process");
    }

    List<FlowNode> enabledFlowNodes = new ArrayList<FlowNode>();

    AdhocSubProcess adhocSubProcess = (AdhocSubProcess) execution.getCurrentFlowElement();

    // if sequential ordering, only one child execution can be active, so no enabled activities
    if (adhocSubProcess.hasSequentialOrdering()) {
      if (execution.getExecutions().size() > 0) {
        return enabledFlowNodes;
      }
    }

    for (FlowElement flowElement : adhocSubProcess.getFlowElements()) {
      if (flowElement instanceof FlowNode) {
        FlowNode flowNode = (FlowNode) flowElement;
        if (flowNode.getIncomingFlows().size() == 0) {
          enabledFlowNodes.add(flowNode);
        }
      }
    }

    return enabledFlowNodes;
  }

}
