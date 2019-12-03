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
package org.activiti.engine.test.jobexecutor;

import java.util.Date;
import java.util.concurrent.Callable;

import org.activiti.engine.impl.test.JobTestHelper;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.TimerJobQuery;
import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;

/**

 */
public class JobExecutorExceptionsTest extends PluggableActivitiTestCase {

  @Test
  @Deployment(resources = { "org/activiti/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml" })
  public void testQueryByExceptionWithRealJobExecutor() {
    TimerJobQuery query = managementService.createTimerJobQuery().withException();
    Assert.assertEquals(0, query.count());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    // Timer is set for 4 hours, so move clock 5 hours
    processEngineConfiguration.getClock().setCurrentTime(new Date(new Date().getTime() + 5 * 60 * 60 * 1000));

    // The execution is waiting in the first usertask. This contains a
    // boundary timer event which we will execute manual for testing purposes.
    JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration, 5000L, 100L, new Callable<Boolean>() {
      public Boolean call() throws Exception {
        return managementService.createTimerJobQuery().withException().count() == 1;
      }
    });

    query = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).withException();
    Assert.assertEquals(1, query.count());
  }

}
