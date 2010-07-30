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

package org.activiti.engine.impl.bpmn;

import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.repository.ProcessDefinitionEntity;
import org.activiti.pvm.activity.ActivityContext;


/**
 * Implementation of the BPMN 2.0 call activity
 * (limited currently to calling a subprocess and not (yet) a global task).
 * 
 * @author Joram Barrez
 */
public class CallActivityBehaviour extends AbstractBpmnActivity {
  
  protected String processDefinitonKey;
  
  public CallActivityBehaviour(String processDefinitionKey) {
    this.processDefinitonKey = processDefinitionKey;
  }
  
  public void start(ActivityContext activityContext) throws Exception {
    RepositorySession repositorySession = CommandContext.getCurrent().getRepositorySession();
    ProcessDefinitionEntity processDefinition =  repositorySession.findDeployedLatestProcessDefinitionByKey(processDefinitonKey);

    throw new UnsupportedOperationException("please implement me");
//    ObjectProcessInstance processInstance = execution.createSubProcessInstance(processDefinition);
//    processInstance.start();
  }
  
  public void signal(ActivityContext activityContext, String signalName, Object signalData) throws Exception {
    leave(activityContext);
  }

}
