/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.impl.event;

import java.util.Map;

import org.activiti.pvm.event.ProcessInstanceEvent;

/**
 * The abstract base class for a {@link org.activiti.pvm.event.ProcessInstanceEvent},
 * supporting the basic relations like process definition, instance and
 * activity, the event is related to.
 *
 * @author Micha Kiener
 */
public abstract class AbstractProcessInstanceEvent<T> extends AbstractProcessEvent<T> implements ProcessInstanceEvent<T> {

  private final String processDefinitionId;
  private final String processInstanceId;
  private final String activityId;

  /**
   * Standard constructor used to create a new process event based on the given
   * relations.
   *
   * @param processDefinitionId the id of the process definition this event is
   * related to (must not be <code>null</code>)
   * @param processInstanceId the id of the process instance this event is
   * related to (must not be <code>null</code>)
   * @param activityId the optional id of the activity this event is created in
   * @param headerAttributesMap the optional map of header attributes
   * @param payload the optional payload
   */
  protected AbstractProcessInstanceEvent(String processDefinitionId, String processInstanceId, String activityId, Map<String, Object> headerAttributesMap, T payload) {
    super(headerAttributesMap, payload);

    this.processDefinitionId = processDefinitionId;
    this.processInstanceId = processInstanceId;
    this.activityId = activityId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getActivityId() {
    return activityId;
  }
}