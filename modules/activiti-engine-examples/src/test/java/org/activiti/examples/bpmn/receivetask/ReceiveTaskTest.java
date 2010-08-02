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

package org.activiti.examples.bpmn.receivetask;

import static org.junit.Assert.assertNotNull;

import org.activiti.engine.ActivityInstance;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.Rule;
import org.junit.Test;


/**
 * @author Joram Barrez
 */
public class ReceiveTaskTest {
  
  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();
  
  @Test
  @Deployment
  public void testWaitStateBehavior() {
    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("receiveTask");
    ActivityInstance activityInstance = deployer.getProcessService().findActivityInstanceByProcessInstanceIdAndActivityId(pi.getId(), "waitState");
    assertNotNull(activityInstance);
    
    deployer.getProcessService().signal(activityInstance.getId());
    deployer.assertProcessEnded(pi.getId());
  }

}
