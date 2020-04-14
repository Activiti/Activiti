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
package org.activiti.engine.impl.test;

import static java.util.Collections.synchronizedList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

/**
 */
public class NoOpServiceTask implements JavaDelegate {

  public static AtomicInteger CALL_COUNT = new AtomicInteger(0);
  public static List<String> NAMES = synchronizedList(new ArrayList<String>());

  protected Expression name;

  @Override
  public void execute(DelegateExecution execution) {
    CALL_COUNT.incrementAndGet();
    NAMES.add((String) name.getValue(execution));
  }

  public Expression getName() {
    return name;
  }

  public void setName(Expression name) {
    this.name = name;
  }

  public static void reset() {
    CALL_COUNT.set(0);
    NAMES.clear();
  }

}
