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
package org.activiti.test.cfg.spring;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.Deployment;
import org.activiti.engine.Execution;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.ProcessService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.Task;
import org.activiti.impl.definition.ActivityImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.task.TaskDefinition;
import org.activiti.impl.util.LogUtil;
import org.activiti.util.CollectionUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ClassUtils;

/**
 * @author Tom Baeyens
 * @author Dave Syer
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringTest {

  @Autowired
  private ProcessEngine processEngine;

  @Autowired
  private UserBean userBean;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }

  @Test
  public void testProcessExecutionWithTaskAssignedFromExpression() {

    int before = processEngine.getTaskService().findAssignedTasks("kermit").size();
    ProcessInstance execution = processEngine.getProcessService().startProcessInstanceByKey("taskAssigneeExpressionProcess");
    assertEquals("[theTask]", execution.getActivityNames().toString());
    assertEquals("${user}", ((TaskDefinition) ReflectionTestUtils.getField(((ActivityImpl) ((ExecutionImpl) execution).getActivity()).getActivityBehavior(),
            "taskDefinition")).getAssignee());
    List<Task> tasks = processEngine.getTaskService().findAssignedTasks("kermit");
    assertEquals(before + 1, tasks.size());

    processEngine.getProcessService().deleteProcessInstance(execution.getId());
    
  }

  @Test
  public void testJavaServiceDelegation() {
    ProcessService processService = processEngine.getProcessService();
    ProcessInstance pi = processService.startProcessInstanceByKey("javaServiceDelegation", 
            CollectionUtil.singletonMap("input", "Activiti BPM Engine"));
    Execution execution = processService.findExecutionInActivity(pi.getId(), "waitState");
    assertEquals("ACTIVITI BPM ENGINE", processService.getVariable(execution.getId(), "input"));
    processEngine.getProcessService().deleteProcessInstance(execution.getId());
  }

  @Test
  public void testSpringTransaction() {
    int before = processEngine.getRepositoryService().findDeployments().size();
    userBean.doTransactional();
    assertEquals(before + 1, processEngine.getRepositoryService().findDeployments().size());
  }

  @Test
  public void testSpringTransactionRollback() {
    int before = processEngine.getRepositoryService().findDeployments().size();
    userBean.setFail(true);
    exception.expect(ActivitiException.class);
    exception.expectMessage("aprocess");
    try {
      new TransactionTemplate(transactionManager).execute(new TransactionCallback<Object>() {

        public Object doInTransaction(TransactionStatus status) {
          userBean.doTransactional();
          return null;
        }
      });

    } finally {
      assertEquals(before, processEngine.getRepositoryService().findDeployments().size());
    }
  }

  @Test
  @DirtiesContext
  public void testSaveDeployment() {

    int before = processEngine.getRepositoryService().findDeployments().size();

    String resource = ClassUtils.addResourcePathToPackagePath(getClass(), "testProcess.bpmn20.xml");
    RepositoryService repositoryService = processEngine.getRepositoryService();
    Deployment deployment = repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();

    assertEquals(before + 1, processEngine.getRepositoryService().findDeployments().size());
    deployment = repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    assertEquals(before + 1, processEngine.getRepositoryService().findDeployments().size());

    if (deployment != null) {
      repositoryService.deleteDeploymentCascade(deployment.getId());
    }

  }

  @Test
  @DirtiesContext
  public void testConfigureDeploymentCheck() throws Exception {

    int before = processEngine.getRepositoryService().findDeployments().size();

    RepositoryService repositoryService = processEngine.getRepositoryService();

    // (N.B. Spring 3 resolves Resource[] from a pattern to FileSystemResource
    // instances)
    FileSystemResource resource = new FileSystemResource(new ClassPathResource("staticProcess.bpmn20.xml", getClass()).getFile());
    String path = resource.getFile().getAbsolutePath();

    Deployment deployment = repositoryService.createDeployment().name(path).addInputStream(path, resource.getInputStream()).deploy();
    // Should be identical to resource configured in factory bean so no new
    // deployment
    assertEquals(before, processEngine.getRepositoryService().findDeployments().size());

    if (deployment != null) {
      repositoryService.deleteDeploymentCascade(deployment.getId());
    }

  }
}
