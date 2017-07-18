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
package org.activiti.engine.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

public class ExecutionGraphUtil {

  /**
   * Takes in a collection of executions belonging to the same process instance. Orders the executions in a list, first elements are the leaf, last element is the root elements.
   */
  public static List<ExecutionEntity> orderFromRootToLeaf(Collection<ExecutionEntity> executions) {
    List<ExecutionEntity> orderedList = new ArrayList<ExecutionEntity>(executions.size());

    // Root elements
    HashSet<String> previousIds = new HashSet<String>();
    for (ExecutionEntity execution : executions) {
      if (execution.getParentId() == null) {
        orderedList.add(execution);
        previousIds.add(execution.getId());
      }
    }

    // Non-root elements
    while (orderedList.size() < executions.size()) {
      for (ExecutionEntity execution : executions) {
        if (!previousIds.contains(execution.getId()) && previousIds.contains(execution.getParentId())) {
          orderedList.add(execution);
          previousIds.add(execution.getId());
        }
      }
    }

    return orderedList;
  }

  public static List<ExecutionEntity> orderFromLeafToRoot(Collection<ExecutionEntity> executions) {
    List<ExecutionEntity> orderedList = orderFromRootToLeaf(executions);
    Collections.reverse(orderedList);
    return orderedList;
  }

  /**
   * Verifies if the element with the given source identifier can reach the element with the target identifier through following sequence flow.
   */
  public static boolean isReachable(String processDefinitionId, String sourceElementId, String targetElementId) {

    // Fetch source and target elements
    Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);

    FlowElement sourceFlowElement = process.getFlowElement(sourceElementId, true);
    FlowNode sourceElement = null;
    if (sourceFlowElement instanceof FlowNode) {
      sourceElement = (FlowNode) sourceFlowElement;
    } else if (sourceFlowElement instanceof SequenceFlow) {
      sourceElement = (FlowNode) ((SequenceFlow) sourceFlowElement).getTargetFlowElement();
    }

    FlowElement targetFlowElement = process.getFlowElement(targetElementId, true);
    FlowNode targetElement = null;
    if (targetFlowElement instanceof FlowNode) {
      targetElement = (FlowNode) targetFlowElement;
    } else if (targetFlowElement instanceof SequenceFlow) {
      targetElement = (FlowNode) ((SequenceFlow) targetFlowElement).getTargetFlowElement();
    }

    if (sourceElement == null) {
      throw new ActivitiException("Invalid sourceElementId '" + sourceElementId + "': no element found for this id n process definition '" + processDefinitionId + "'");
    }
    if (targetElement == null) {
      throw new ActivitiException("Invalid targetElementId '" + targetElementId + "': no element found for this id n process definition '" + processDefinitionId + "'");
    }

    Set<String> visitedElements = new HashSet<String>();
    return isReachable(process, sourceElement, targetElement, visitedElements);
  }

  public static boolean isReachable(Process process, FlowNode sourceElement, FlowNode targetElement, Set<String> visitedElements) {

    // No outgoing seq flow: could be the end of eg . the process or an embedded subprocess
    if (sourceElement.getOutgoingFlows().size() == 0) {
      visitedElements.add(sourceElement.getId());

      FlowElementsContainer parentElement = process.findParent(sourceElement);
      if (parentElement instanceof SubProcess) {
        sourceElement = (SubProcess) parentElement;
      } else {
        return false;
      }
    }

    if (sourceElement.getId().equals(targetElement.getId())) {
      return true;
    }

    // To avoid infinite looping, we must capture every node we visit
    // and check before going further in the graph if we have already
    // visited the node.
    visitedElements.add(sourceElement.getId());

    List<SequenceFlow> sequenceFlows = sourceElement.getOutgoingFlows();
    if (sequenceFlows != null && sequenceFlows.size() > 0) {
      for (SequenceFlow sequenceFlow : sequenceFlows) {
        String targetRef = sequenceFlow.getTargetRef();
        FlowNode sequenceFlowTarget = (FlowNode) process.getFlowElement(targetRef, true);
        if (sequenceFlowTarget != null && !visitedElements.contains(sequenceFlowTarget.getId())) {
          boolean reachable = isReachable(process, sequenceFlowTarget, targetElement, visitedElements);

          if (reachable) {
            return true;
          }
        }
      }
    }

    return false;
  }

}
