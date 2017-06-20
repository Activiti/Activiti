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
package org.activiti.engine.test.api.v6;

import java.util.concurrent.atomic.AtomicInteger;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**

 */
public class CountingServiceTaskTestDelegate implements JavaDelegate {

  public static AtomicInteger CALL_COUNT = new AtomicInteger(0);

  @Override
  public void execute(DelegateExecution execution) {
    Integer counter = (Integer) execution.getVariable("counter");
    counter = counter + 1;
    execution.setVariable("counter", counter);

    if (CALL_COUNT.get() % 1000 == 0) {
      System.out.println("Call count: " + CALL_COUNT);
    }

    CALL_COUNT.incrementAndGet();
  }

}
