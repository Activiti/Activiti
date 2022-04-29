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

package org.activiti.engine.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


public class ExecutionTreeUtil {

  public static ExecutionTree buildExecutionTree(DelegateExecution executionEntity) {

    // Find highest parent
    ExecutionEntity parentExecution = (ExecutionEntity) executionEntity;
    while (parentExecution.getParentId() != null || parentExecution.getSuperExecutionId() != null) {
      if (parentExecution.getParentId() != null) {
        parentExecution = parentExecution.getParent();
      } else {
        parentExecution = parentExecution.getSuperExecution();
      }
    }

    // Collect all child executions now we have the parent
    List<ExecutionEntity> allExecutions = new ArrayList<ExecutionEntity>();
    allExecutions.add(parentExecution);
    collectChildExecutions(parentExecution, allExecutions);
    return buildExecutionTree(allExecutions);
  }

  protected static void collectChildExecutions(ExecutionEntity rootExecutionEntity, List<ExecutionEntity> allExecutions) {
    for (ExecutionEntity childExecutionEntity : rootExecutionEntity.getExecutions()) {
      allExecutions.add(childExecutionEntity);
      collectChildExecutions(childExecutionEntity, allExecutions);
    }

    if (rootExecutionEntity.getSubProcessInstance() != null) {
      allExecutions.add(rootExecutionEntity.getSubProcessInstance());
      collectChildExecutions(rootExecutionEntity.getSubProcessInstance(), allExecutions);
    }
  }

  public static ExecutionTree buildExecutionTree(Collection<ExecutionEntity> executions) {
    ExecutionTree executionTree = new ExecutionTree();

    // Map the executions to their parents. Catch and store the root element (process instance execution) while were at it
    Map<String, List<ExecutionEntity>> parentMapping = new HashMap<String, List<ExecutionEntity>>();
    for (ExecutionEntity executionEntity : executions) {
      String parentId = executionEntity.getParentId();

      // Support for call activity
      if (parentId == null) {
        parentId = executionEntity.getSuperExecutionId();
      }

      if (parentId != null) {
        if (!parentMapping.containsKey(parentId)) {
          parentMapping.put(parentId, new ArrayList<ExecutionEntity>());
        }
        parentMapping.get(parentId).add(executionEntity);
      } else if (executionEntity.getSuperExecutionId() == null){
        executionTree.setRoot(new ExecutionTreeNode(executionEntity));
      }
    }

    fillExecutionTree(executionTree, parentMapping);
    return executionTree;
  }

  public static ExecutionTree buildExecutionTreeForProcessInstance(Collection<ExecutionEntity> executions) {
    ExecutionTree executionTree = new ExecutionTree();
    if (executions.size() == 0) {
      return executionTree;
    }

    // Map the executions to their parents. Catch and store the root element (process instance execution) while were at it
    Map<String, List<ExecutionEntity>> parentMapping = new HashMap<String, List<ExecutionEntity>>();
    for (ExecutionEntity executionEntity : executions) {
      String parentId = executionEntity.getParentId();

      if (parentId != null) {
        if (!parentMapping.containsKey(parentId)) {
          parentMapping.put(parentId, new ArrayList<ExecutionEntity>());
        }
        parentMapping.get(parentId).add(executionEntity);
      } else {
        executionTree.setRoot(new ExecutionTreeNode(executionEntity));
      }
    }

    fillExecutionTree(executionTree, parentMapping);
    return executionTree;
  }

  protected static void fillExecutionTree(ExecutionTree executionTree, Map<String, List<ExecutionEntity>> parentMapping) {
    if (executionTree.getRoot() == null) {
      throw new ActivitiException("Programmatic error: the list of passed executions in the argument of the method should contain the process instance execution");
    }

    // Now build the tree, top-down
    LinkedList<ExecutionTreeNode> executionsToHandle = new LinkedList<ExecutionTreeNode>();
    executionsToHandle.add(executionTree.getRoot());

    while (!executionsToHandle.isEmpty()) {
      ExecutionTreeNode parentNode = executionsToHandle.pop();
      String parentId = parentNode.getExecutionEntity().getId();
      if (parentMapping.containsKey(parentId)) {
        List<ExecutionEntity> childExecutions = parentMapping.get(parentId);
        List<ExecutionTreeNode> childNodes = new ArrayList<ExecutionTreeNode>(childExecutions.size());

        for (ExecutionEntity childExecutionEntity : childExecutions) {

          ExecutionTreeNode childNode = new ExecutionTreeNode(childExecutionEntity);
          childNode.setParent(parentNode);
          childNodes.add(childNode);

          executionsToHandle.add(childNode);
        }

        parentNode.setChildren(childNodes);

      }
    }
  }

}
