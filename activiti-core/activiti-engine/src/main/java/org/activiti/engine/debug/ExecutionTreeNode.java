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
package org.activiti.engine.debug;

import java.util.Iterator;
import java.util.List;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**

 */
public class ExecutionTreeNode implements Iterable<ExecutionTreeNode> {

  protected ExecutionEntity executionEntity;
  protected ExecutionTreeNode parent;
  protected List<ExecutionTreeNode> children;

  public ExecutionTreeNode(ExecutionEntity executionEntity) {
    this.executionEntity = executionEntity;
  }

  public ExecutionEntity getExecutionEntity() {
    return executionEntity;
  }

  public void setExecutionEntity(ExecutionEntity executionEntity) {
    this.executionEntity = executionEntity;
  }

  public ExecutionTreeNode getParent() {
    return parent;
  }

  public void setParent(ExecutionTreeNode parent) {
    this.parent = parent;
  }

  public List<ExecutionTreeNode> getChildren() {
    return children;
  }

  public void setChildren(List<ExecutionTreeNode> children) {
    this.children = children;
  }

  @Override
  public Iterator<ExecutionTreeNode> iterator() {
    return new ExecutionTreeBfsIterator(this);
  }

  public ExecutionTreeBfsIterator leafsFirstIterator() {
    return new ExecutionTreeBfsIterator(this, true);
  }

  /* See http://stackoverflow.com/questions/4965335/how-to-print-binary-tree-diagram */
  @Override
  public String toString() {
    StringBuilder strb = new StringBuilder();
    strb.append(getExecutionEntity().getId());
    if (getExecutionEntity().getActivityId() != null) {
      strb.append(" : " + getExecutionEntity().getActivityId());
    }
    if (getExecutionEntity().getParentId() != null) {
      strb.append(", parent id " + getExecutionEntity().getParentId());
    }
    if (getExecutionEntity().isProcessInstanceType()) {
      strb.append(" (process instance)");
    }
    strb.append(System.lineSeparator());
    if (children != null) {
      for (ExecutionTreeNode childNode : children) {
        childNode.internalToString(strb, "", true);
      }
    }
    return strb.toString();
  }

  protected void internalToString(StringBuilder strb, String prefix, boolean isTail) {
    strb.append(prefix + (isTail ? "└── " : "├── ") + getExecutionEntity().getId() + " : " 
        + getCurrentFlowElementId()
        + ", parent id " + getExecutionEntity().getParentId() 
        + (getExecutionEntity().isActive() ? " (active)" : " (not active)")
        + (getExecutionEntity().isScope() ? " (scope)" : "")
        + (getExecutionEntity().isMultiInstanceRoot() ? " (multi instance root)" : "")
        + (getExecutionEntity().isEnded() ? " (ended)" : "")
        + System.lineSeparator());
    if (children != null) {
      for (int i = 0; i < children.size() - 1; i++) {
        children.get(i).internalToString(strb, prefix + (isTail ? "    " : "│   "), false);
      }
      if (children.size() > 0) {
        children.get(children.size() - 1).internalToString(strb, prefix + (isTail ? "    " : "│   "), true);
      }
    }
  }
  
  protected String getCurrentFlowElementId() {
    FlowElement flowElement = getExecutionEntity().getCurrentFlowElement();
    if (flowElement instanceof SequenceFlow) {
      SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
      return sequenceFlow.getSourceRef() + " -> " + sequenceFlow.getTargetRef();
    } else if (flowElement != null) {
      return flowElement.getId() + " (" + flowElement.getClass().getSimpleName();
    } else {
      return "";
    }
  }

}