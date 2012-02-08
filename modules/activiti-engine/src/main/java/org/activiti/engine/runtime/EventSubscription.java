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

package org.activiti.engine.runtime;


/**
 * @author Daniel Meyer
 */
public interface EventSubscription {
  
  /**
   * returns the type of the event subscription, such as 'message' or 'signal'
   */
  String getEventType();
  
  /**
   * returns the execution id of the event subscription
   */
  String getExecutionId();
  
  /**
   * returns the name of the event subscription as specified in the event definition
   */
  String getEventName();
  
  /**
   * the id of the catching event activity 
   */
  String getActivityId();

}
