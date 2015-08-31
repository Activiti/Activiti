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
package org.activiti.engine.impl.util.tree;

import java.util.Iterator;
import java.util.List;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Joram Barrez
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
    StringBuilder strb = new StringBuilder(100);
    strb.append(getExecutionEntity().getId()).append(" : ").append(getExecutionEntity().getActivityId()).append(", parent id ")
            .append(getExecutionEntity().getParentId()).append("\r\n");
    if (children != null) {
      for (ExecutionTreeNode childNode : children) {
        childNode.internalToString(strb, "", true);
      }
    }
    return strb.toString();
  }

  protected void internalToString(StringBuilder strb, String prefix, boolean isTail) {
    strb.append(prefix).append(isTail ? "└── " : "├── ").append(getExecutionEntity().getId()).append(" : ")
            .append(getExecutionEntity().getActivityId()).append(", parent id ").append(getExecutionEntity().getParentId())
            .append(getExecutionEntity().isScope() ? " (scope)" : "").append("\r\n");
    if (children != null) {
      for (int i = 0; i < children.size() - 1; i++) {
        children.get(i).internalToString(strb, prefix + (isTail ? "    " : "│   "), false);
      }
      if (children.size() > 0) {
        children.get(children.size() - 1).internalToString(strb, prefix + (isTail ? "    " : "│   "), true);
      }
    }
  }

}
