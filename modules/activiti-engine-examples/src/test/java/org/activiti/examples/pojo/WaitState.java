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
package org.activiti.examples.pojo;

import org.activiti.activity.ActivityExecution;
import org.activiti.activity.EventActivityBehavior;


/**
 * @author Tom Baeyens
 */
public class WaitState implements EventActivityBehavior {

  public void execute(ActivityExecution execution) {
    // By default, the execution will not propagate.
    // So if no method like take(Transition) is called on execution
    // then the activity will behave as a wait state.  The execution is currently 
    // pointing to the activity.  The original call to execution.start()
    // or execution.event() will return.  Then the execution object will 
    // remain pointing to the current activity until execution.event(Object) is called.
    // That method will delegate to the method below.  
  }

  public void event(ActivityExecution execution, Object event) {
    execution.take((String)event);
  }
}
