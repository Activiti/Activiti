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
import java.util.LinkedList;

/**
 * Iterates over an {@link ExecutionTree} using breadth-first search
 *

 */
public class ExecutionTreeBfsIterator implements Iterator<ExecutionTreeNode> {

  protected ExecutionTreeNode rootNode;
  protected boolean reverseOrder;

  protected LinkedList<ExecutionTreeNode> flattenedList;
  protected Iterator<ExecutionTreeNode> flattenedListIterator;

  public ExecutionTreeBfsIterator(ExecutionTreeNode executionTree) {
    this(executionTree, false);
  }

  public ExecutionTreeBfsIterator(ExecutionTreeNode rootNode, boolean reverseOrder) {
    this.rootNode = rootNode;
    this.reverseOrder = reverseOrder;
  }

  protected void flattenTree() {
    flattenedList = new LinkedList<ExecutionTreeNode>();

    LinkedList<ExecutionTreeNode> nodesToHandle = new LinkedList<ExecutionTreeNode>();
    nodesToHandle.add(rootNode);
    while (!nodesToHandle.isEmpty()) {

      ExecutionTreeNode currentNode = nodesToHandle.pop();
      if (reverseOrder) {
        flattenedList.addFirst(currentNode);
      } else {
        flattenedList.add(currentNode);
      }

      if (currentNode.getChildren() != null && currentNode.getChildren().size() > 0) {
        for (ExecutionTreeNode childNode : currentNode.getChildren()) {
          nodesToHandle.add(childNode);
        }
      }
    }

    flattenedListIterator = flattenedList.iterator();
  }

  @Override
  public boolean hasNext() {
    if (flattenedList == null) {
      flattenTree();
    }
    return flattenedListIterator.hasNext();
  }

  @Override
  public ExecutionTreeNode next() {
    if (flattenedList == null) {
      flattenTree();
    }
    return flattenedListIterator.next();
  }

  @Override
  public void remove() {
    if (flattenedList == null) {
      flattenTree();
    }
    flattenedListIterator.remove();
  }

}
