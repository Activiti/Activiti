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
package org.activiti.cdi;

import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;

/**
 * The type of a business process event. Indicates what is happening/has
 * happened, i.e. whether a transition is taken, an activity is entered or left.
 * 
 * @author Daniel Meyer
 */
public interface BusinessProcessEventType {

  /** Signifies that a transition is being taken / was taken **/
  public static final BusinessProcessEventType TAKE = new DefaultBusinessProcessEventType(ExecutionListener.EVENTNAME_TAKE);
  
  /** Signifies that an activity is being entered / war entered **/
  public static final BusinessProcessEventType START_ACTIVITY = new DefaultBusinessProcessEventType(ExecutionListener.EVENTNAME_START);
  
  /** Signifies that an activity is being left / was left **/
  public static final BusinessProcessEventType END_ACTIVITY = new DefaultBusinessProcessEventType(ExecutionListener.EVENTNAME_END);
  
  /** Signifies that a task has been created **/
  public static final BusinessProcessEventType CREATE_TASK = new DefaultBusinessProcessEventType(TaskListener.EVENTNAME_CREATE);
  
  /** Signifies that a task has been created **/
  public static final BusinessProcessEventType ASSIGN_TASK = new DefaultBusinessProcessEventType(TaskListener.EVENTNAME_ASSIGNMENT);
  
  /** Signifies that a task has been created **/
  public static final BusinessProcessEventType COMPLETE_TASK = new DefaultBusinessProcessEventType(TaskListener.EVENTNAME_COMPLETE);

  /** Signifies that a task has been deleted **/
  public static final BusinessProcessEventType DELETE_TASK = new DefaultBusinessProcessEventType(TaskListener.EVENTNAME_DELETE);
  
  static class DefaultBusinessProcessEventType implements BusinessProcessEventType {

    protected final String typeName;

    public DefaultBusinessProcessEventType(String typeName) {
      this.typeName = typeName;
    }

    @Override
    public String getTypeName() {
      return typeName;
    }
    
    @Override
    public String toString() {
      return typeName;
    }
    
  }

  String getTypeName();

}
