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
package org.activiti.test.pvm.activities;

import org.activiti.activity.ActivityBehavior;
import org.activiti.activity.ActivityExecution;
import org.activiti.activity.ConcurrencyScope;
import org.activiti.activity.Transition;


/**
 * @author Tom Baeyens
 */
public class Fork implements ActivityBehavior {

  public void execute(ActivityExecution execution) {
    execution.end();

    ConcurrencyScope scopeInstance = execution.getConcurrencyScope();
    for (Transition transition: execution.getOutgoingTransitions()) {
      ActivityExecution concurrentExecution = scopeInstance.createExecution();
      concurrentExecution.take(transition);
    }
  }
}
