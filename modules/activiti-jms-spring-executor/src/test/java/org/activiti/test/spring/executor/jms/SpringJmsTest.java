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
package org.activiti.test.spring.executor.jms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.jms.ConnectionFactory;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.spring.impl.test.CleanTestExecutionListener;
import org.activiti.test.spring.executor.jms.config.SpringJmsConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jayway.awaitility.Awaitility;

@TestExecutionListeners(value = CleanTestExecutionListener.class, mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
@ContextConfiguration(classes = SpringJmsConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringJmsTest {
  
  @Autowired
  private ProcessEngine processEngine;
  
  @Autowired
  private ConnectionFactory connectionFactory;
  
  @Test
  public void testMessageQueueAsyncExecutor() {
    processEngine.getRepositoryService().createDeployment()
      .addClasspathResource("org/activiti/test/spring/executor/jms/SpringJmsTest.testMessageQueueAsyncExecutor.bpmn20.xml")
      .deploy();
    
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("input1", 123);
    vars.put("input2", 456);
    processEngine.getRuntimeService().startProcessInstanceByKey("AsyncProcess", vars);
    
    // Wait until the process is completely finished
    Awaitility.waitAtMost(1, TimeUnit.MINUTES).pollInterval(500, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
      public Boolean call() throws Exception {
        return processEngine.getRuntimeService().createProcessInstanceQuery().count() == 0;
      }
    });
    
    Assert.assertEquals(0L, processEngine.getRuntimeService().createProcessInstanceQuery().count());
    
    for (String activityName : Arrays.asList("A", "B", "C", "D", "E", "F", "After boundary", "The user task", "G", "G1", "G2", "G3", "H", "I", "J", "K", "L")) {
      System.out.println(activityName + " flerp");
      Assert.assertNotNull(processEngine.getHistoryService().createHistoricActivityInstanceQuery().activityName(activityName).singleResult());
    }
    
    Assert.assertNull( ((DefaultAsyncJobExecutor) processEngine.getProcessEngineConfiguration().getAsyncExecutor()).getExecutorService());
  }

}
