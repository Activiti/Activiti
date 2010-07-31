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

import org.activiti.engine.ProcessInstance;
import org.activiti.pvm.impl.process.ActivityImpl;

/**
 * @author Christian Stettler
 */
public class ActivityStartedEvent extends AbstractProcessInstanceEvent<ActivityImpl> {

  public ActivityStartedEvent(ProcessInstance processInstance, ActivityImpl activity) {
    super(processInstance.getProcessDefinitionId(), processInstance.getId(), activity.getId(), null, activity);
  }

}