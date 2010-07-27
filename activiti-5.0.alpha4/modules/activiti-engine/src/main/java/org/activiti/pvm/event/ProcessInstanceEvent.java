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

package org.activiti.pvm.event;

/**
 * This interface is common to all process events being fired in the context of
 * a specific process instance and handled by the {@link
 * org.activiti.pvm.event.ProcessEventBus}. A process instance event is always
 * related to a {@link org.activiti.ProcessInstance} and optionally to an {@link
 * org.activiti.pvm.Activity}.
 *
 * @author Christian Stettler
 */
public interface ProcessInstanceEvent<T> extends ProcessEvent<T> {
  /**
   * @return the id of the process definition this event is related to (never
   *         <code>null</code>)
   */
  String getProcessDefinitionId();

  /**
   * @return the id of the process instance this event is related to (never
   *         <code>null</code>)
   */
  String getProcessInstanceId();

  /**
   * @return the id of the optional activity this event is related to (might be
   *         <code>null</code>)
   */
  String getActivityId();
}
