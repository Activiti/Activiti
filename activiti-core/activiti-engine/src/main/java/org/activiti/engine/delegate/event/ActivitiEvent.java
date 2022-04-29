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

package org.activiti.engine.delegate.event;


/**
 * Describes an event that occurred in the Activiti Engine which is dispatched to external listeners, if any.
 *

 *
 */
public interface ActivitiEvent {

  /**
   * @return type of event.
   */
  ActivitiEventType getType();

  /**
   * @return the id of the execution this event is associated with. Returns null, if the event was not dispatched from within an active execution.
   */
  String getExecutionId();

  /**
   * @return the id of the process instance this event is associated with. Returns null, if the event was not dispatched from within an active execution.
   */
  String getProcessInstanceId();

  /**
   * @return the id of the process definition this event is associated with. Returns null, if the event was not dispatched from within an active execution.
   */
  String getProcessDefinitionId();

}
