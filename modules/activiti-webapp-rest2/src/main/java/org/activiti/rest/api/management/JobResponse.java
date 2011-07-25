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

package org.activiti.rest.api.management;

import org.activiti.engine.runtime.Job;
import org.activiti.rest.api.RequestUtil;

/**
 * @author Tijs Rademakers
 */
public class JobResponse {

  String id;
  String executionId;
  String processInstanceId;
  String dueDate;
  int retries;
  String exceptionMessage;
  String stacktrace;
  
  public JobResponse(Job job) {
    setId(job.getId());
    setExecutionId(job.getExecutionId());
    setProcessInstanceId(job.getProcessInstanceId());
    setDueDate(RequestUtil.dateToString(job.getDuedate()));
    setRetries(job.getRetries());
    setExceptionMessage(job.getExceptionMessage());
  }
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getExecutionId() {
    return executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  public String getDueDate() {
    return dueDate;
  }
  public void setDueDate(String dueDate) {
    this.dueDate = dueDate;
  }
  public int getRetries() {
    return retries;
  }
  public void setRetries(int retries) {
    this.retries = retries;
  }
  public String getExceptionMessage() {
    return exceptionMessage;
  }
  public void setExceptionMessage(String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }
  public String getStacktrace() {
    return stacktrace;
  }
  public void setStacktrace(String stacktrace) {
    this.stacktrace = stacktrace;
  }
}
