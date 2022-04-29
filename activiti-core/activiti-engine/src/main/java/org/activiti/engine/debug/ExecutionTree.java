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

import java.util.Iterator;
import java.util.List;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


public class ExecutionTree implements Iterable<ExecutionTreeNode> {

  protected ExecutionTreeNode root;

  public ExecutionTree() {

  }

  public ExecutionTreeNode getRoot() {
    return root;
  }

  public void setRoot(ExecutionTreeNode root) {
    this.root = root;
  }

  /**
   * Looks up the {@link ExecutionEntity} for a given id.
   */
  public ExecutionTreeNode getTreeNode(String executionId) {
    return getTreeNode(executionId, root);
  }

  protected ExecutionTreeNode getTreeNode(String executionId, ExecutionTreeNode currentNode) {
    if (currentNode.getExecutionEntity().getId().equals(executionId)) {
      return currentNode;
    }

    List<ExecutionTreeNode> children = currentNode.getChildren();
    if (currentNode.getChildren() != null && children.size() > 0) {
      int index = 0;
      while (index < children.size()) {
        ExecutionTreeNode result = getTreeNode(executionId, children.get(index));
        if (result != null) {
          return result;
        }
        index++;
      }
    }

    return null;
  }

  @Override
  public Iterator<ExecutionTreeNode> iterator() {
    return new ExecutionTreeBfsIterator(this.getRoot());
  }

  public ExecutionTreeBfsIterator bfsIterator() {
    return new ExecutionTreeBfsIterator(this.getRoot());
  }

  /**
   * Uses an {@link ExecutionTreeBfsIterator}, but returns the leafs first (so flipped order of BFS)
   */
  public ExecutionTreeBfsIterator leafsFirstIterator() {
    return new ExecutionTreeBfsIterator(this.getRoot(), true);
  }

  @Override
  public String toString() {
    return root != null ? root.toString() : "";
  }

}
