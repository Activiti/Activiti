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
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.DelegateHelper;

/**
 * @author Joram Barrez
 */
public class PrototypeDelegateExpressionBean implements JavaDelegate {
  
  public static AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);
  
  private Expression fieldA;
  private Expression fieldB;
  private Expression resultVariableName;
  
  public PrototypeDelegateExpressionBean() {
    INSTANCE_COUNT.incrementAndGet();
  }
  
  @Override
  public void execute(DelegateExecution execution) {
    
    // just a quick check to avoid creating a specific test for it
    int nrOfFieldExtensions = DelegateHelper.getFields(execution).size();
    if (nrOfFieldExtensions != 3) {
      throw new RuntimeException("Error: 3 field extensions expected, but was " + nrOfFieldExtensions);
    }
    
    Number fieldAValue = (Number) fieldA.getValue(execution);
    Number fieldValueB = (Number) fieldB.getValue(execution);
    
    int result = fieldAValue.intValue() + fieldValueB.intValue();
    execution.setVariable(resultVariableName.getValue(execution).toString(), result);
  }

}
