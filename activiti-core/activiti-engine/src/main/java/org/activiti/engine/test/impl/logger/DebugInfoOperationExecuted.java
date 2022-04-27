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

package org.activiti.engine.test.impl.logger;

import java.sql.Date;
import java.text.SimpleDateFormat;

import org.activiti.engine.impl.agenda.AbstractOperation;
import org.slf4j.Logger;


public class DebugInfoOperationExecuted extends AbstractDebugInfo {

  protected long preExecutionTime;
  protected long postExecutionTime;
  protected AbstractOperation operation;
  protected String executionId;
  protected String flowElementId;
  protected Class<?> flowElementClass;

  protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");

  public DebugInfoOperationExecuted(AbstractOperation operation) {
    this.operation = operation;

    // Need to capture data here, as it will change when other steps are executed
    if (operation.getExecution() != null) {
      this.executionId = operation.getExecution().getId();

      if (operation.getExecution().getCurrentFlowElement() != null) {
        this.flowElementId = operation.getExecution().getCurrentFlowElement().getId();
        this.flowElementClass = operation.getExecution().getCurrentFlowElement().getClass();
      }
    }
  }

  @Override
  public void printOut(Logger logger) {
    printOperationInfo(logger);

    if (getExecutionTrees().size() > 0) {
      for (DebugInfoExecutionTree executionTree : getExecutionTrees()) {
        executionTree.getProcessInstance().print(logger);
      }
    }
  }

  protected void printOperationInfo(Logger logger) {
    StringBuilder strb = new StringBuilder(35);

    // Timing info
    strb.append("[").append(dateFormat.format(new Date(getPreExecutionTime()))).append(" - ")
            .append(dateFormat.format(new Date(getPostExecutionTime()))).append(" (")
            .append(getPostExecutionTime() - getPreExecutionTime()).append("ms)]");

    // Operation info
    strb.append(" ").append(getOperation().getClass().getSimpleName()).append(" ");

    // Execution info
    if (getExecutionId() != null) {
      strb.append("with execution ").append(getExecutionId());

      if (getFlowElementId() != null) {
        strb.append(" at flow element ").append(getFlowElementId()).append(" (").append(getFlowElementClass().getSimpleName()).append(")");
      }
    }

    logger.info(strb.toString());
  }

  public long getPreExecutionTime() {
    return preExecutionTime;
  }

  public void setPreExecutionTime(long preExecutionTime) {
    this.preExecutionTime = preExecutionTime;
  }

  public long getPostExecutionTime() {
    return postExecutionTime;
  }

  public void setPostExecutionTime(long postExecutionTime) {
    this.postExecutionTime = postExecutionTime;
  }

  public AbstractOperation getOperation() {
    return operation;
  }

  public void setOperation(AbstractOperation operation) {
    this.operation = operation;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public String getFlowElementId() {
    return flowElementId;
  }

  public void setFlowElementId(String flowElementId) {
    this.flowElementId = flowElementId;
  }

  public Class<?> getFlowElementClass() {
    return flowElementClass;
  }

  public void setFlowElementClass(Class<?> flowElementClass) {
    this.flowElementClass = flowElementClass;
  }

}
