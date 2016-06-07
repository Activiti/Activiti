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
package org.activiti.spring.test.fieldinjection;

import java.util.concurrent.atomic.AtomicInteger;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateHelper;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.springframework.stereotype.Component;

/**
 * @author Joram Barrez
 */
@Component("testExecutionListener")
public class TestExecutionListener implements ExecutionListener {
  
  public static AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);
  
  public TestExecutionListener() {
    INSTANCE_COUNT.incrementAndGet();
  }

  @Override
  public void notify(DelegateExecution execution) {
    Expression inputExpression = DelegateHelper.getFieldExpression(execution, "input");
    Number input = (Number) inputExpression.getValue(execution);
    
    int result = input.intValue() * 100;
    
    Expression resultVarExpression = DelegateHelper.getFieldExpression(execution, "resultVar");
    execution.setVariable(resultVarExpression.getValue(execution).toString(), result);
  }

}
