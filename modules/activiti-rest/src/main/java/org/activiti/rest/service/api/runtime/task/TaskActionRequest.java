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

package org.activiti.rest.service.api.runtime.task;


import java.util.List;

import org.activiti.rest.service.api.RestActionRequest;
import org.activiti.rest.service.api.engine.variable.RestVariable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * @author Frederik Heremans
 */
public class TaskActionRequest extends RestActionRequest {

  public static final String ACTION_COMPLETE = "complete";
  public static final String ACTION_CLAIM = "claim";
  public static final String ACTION_DELEGATE = "delegate";
  public static final String ACTION_RESOLVE = "resolve";
  
  private String assignee;
  private List<RestVariable> variables;
  
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }
  public String getAssignee() {
    return assignee;
  }
  public void setVariables(List<RestVariable> variables) {
    this.variables = variables;
  }
  @JsonTypeInfo(use=Id.CLASS, defaultImpl=RestVariable.class)
  public List<RestVariable> getVariables() {
    return variables;
  }
}
