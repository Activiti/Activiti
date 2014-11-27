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
package org.activiti.spring.test.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.el.FixedValue;

/**
 * @author Joram Barrez
 * @author Bernd Ruecker (camunda)
 */
public class MyExecutionListenerBean implements ExecutionListener {

    private FixedValue someField;

    public void notify(DelegateExecution execution) throws Exception {
        execution.setVariable("executionListenerVar", "working");
        if (someField != null) {
            execution.setVariable("executionListenerField", someField.getValue(execution));
        }
    }

    public FixedValue getSomeField() {
        return someField;
    }

    public void setSomeField(FixedValue someField) {
        this.someField = someField;
    }

}
