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
package org.activiti.impl.bpmn;

import org.activiti.ActivitiException;
import org.activiti.BpmnActivityBehavior;
import org.activiti.BpmnExecution;
import org.activiti.impl.util.ReflectUtil;
import org.activiti.pvm.ActivityExecution;

/**
 * Implementation of the BPMN 2.0 serviceTask.
 * 
 * A service task is an automated activity that calls a service defined in the
 * BPMN 2.0 definitions file. This class is the implementation of the Java
 * service invocation, where the target service is a Java class.
 * 
 * @author Joram Barrez
 */
public class JavaServiceTaskActivity extends TaskActivity {
  
  /**
   * An object of this class will be created during execution.
   */
  protected String className;
  
  
  public JavaServiceTaskActivity() {
    
  }
  
  public JavaServiceTaskActivity(String className) {
    setClassName(className);
  }

  public void execute(ActivityExecution execution) throws Exception {
    Object object = ReflectUtil.instantiate(className);
    if ( !(object instanceof BpmnActivityBehavior) ) {
      throw new ActivitiException("Class " + className + " does not implement the "
              + BpmnActivityBehavior.class.getCanonicalName() + " interface");
    }
    BpmnActivityBehavior activityBehavior = (BpmnActivityBehavior) object;
    activityBehavior.execute((BpmnExecution) execution);
    leave(execution, true);
  }

  public void setClassName(String className) {
    this.className = className;
  }

}
