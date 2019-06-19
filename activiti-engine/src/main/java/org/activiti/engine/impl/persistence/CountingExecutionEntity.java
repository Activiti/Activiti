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
package org.activiti.engine.impl.persistence;

import org.activiti.engine.api.internal.Internal;

/**

 */
@Internal
public interface CountingExecutionEntity {
  
  boolean isCountEnabled();
  void setCountEnabled(boolean isCountEnabled);
  
  void setEventSubscriptionCount(int eventSubscriptionCount);
  int getEventSubscriptionCount();
  
  void setTaskCount(int taskcount);
  int getTaskCount();
  
  void setJobCount(int jobCount);
  int getJobCount();
  
  void setTimerJobCount(int timerJobCount);
  int getTimerJobCount();
  
  void setSuspendedJobCount(int suspendedJobCount);
  int getSuspendedJobCount();
  
  void setDeadLetterJobCount(int deadLetterJobCount);
  int getDeadLetterJobCount();
  
  void setVariableCount(int variableCount);
  int getVariableCount();
  
  void setIdentityLinkCount(int identityLinkCount);
  int getIdentityLinkCount();
  
}