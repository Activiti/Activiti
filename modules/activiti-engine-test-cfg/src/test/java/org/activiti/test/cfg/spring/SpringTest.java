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

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Tom Baeyens
 * @author Dave Syer
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringTest {

//  @Autowired
//  private ProcessEngine processEngine;
//
//  @Autowired
//  private UserBean userBean;
//
//  @Autowired
//  private PlatformTransactionManager transactionManager;
//
//  @Rule
//  public ExpectedException exception = ExpectedException.none();
//
//  static {
//    LogUtil.readJavaUtilLoggingConfigFromClasspath();
//  }
//
//  @Test
//  public void testProcessExecutionWithTaskAssignedFromExpression() {
//
//    int before = processEngine.getTaskService().findAssignedTasks("kermit").size();
//    ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("taskAssigneeExpressionProcess");
//    assertEquals("[theTask]", processInstance.findActivityIds().toString());
//    ProcessInstanceEntity processInstanceEntity = (ProcessInstanceEntity) processInstance;
//    ActivityInstanceImpl activityInstance = processInstanceEntity.getActivityInstances().iterator().next();
//    assertEquals("${user}", ((TaskDefinition) ReflectionTestUtils.getField(((ActivityImpl) activityInstance.getActivity()).getActivityBehavior(),
//            "taskDefinition")).getAssignee());
//    List<Task> tasks = processEngine.getTaskService().findAssignedTasks("kermit");
//    assertEquals(before + 1, tasks.size());
//
//    processEngine.getRuntimeService().endProcessInstance(processInstance.getId());
//    
//  }
//
//  @Test
//  public void testJavaServiceDelegation() {
//    RuntimeService runtimeService = processEngine.getRuntimeService();
//    ProcessInstance pi = runtimeService.startProcessInstanceByKey("javaServiceDelegation", 
//            CollectionUtil.singletonMap("input", "Activiti BPM Engine"));
//    ActivityInstance activityInstance = runtimeService.findExecutionByProcessInstanceIdAndActivityId(pi.getId(), "waitState");
//    assertEquals("ACTIVITI BPM ENGINE", runtimeService.getVariable(activityInstance.getId(), "input"));
//    processEngine.getRuntimeService().endProcessInstance(activityInstance.getId());
//  }
//
//  @Test
//  public void testSpringTransaction() {
//    int before = processEngine.getRepositoryService().findDeployments().size();
//    userBean.doTransactional();
//    assertEquals(before + 1, processEngine.getRepositoryService().findDeployments().size());
//  }
//
//  @Test
//  public void testSpringTransactionRollback() {
//    int before = processEngine.getRepositoryService().findDeployments().size();
//    userBean.setFail(true);
//    exception.expect(ActivitiException.class);
//    exception.expectMessage("aprocess");
//    try {
//      new TransactionTemplate(transactionManager).execute(new TransactionCallback<Object>() {
//
//        public Object doInTransaction(TransactionStatus status) {
//          userBean.doTransactional();
//          return null;
//        }
//      });
//
//    } finally {
//      assertEquals(before, processEngine.getRepositoryService().findDeployments().size());
//    }
//  }
//
//  @Test
//  @DirtiesContext
//  public void testSaveDeployment() {
//
//    int before = processEngine.getRepositoryService().findDeployments().size();
//
//    String resource = ClassUtils.addResourcePathToPackagePath(getClass(), "testProcess.bpmn20.xml");
//    RepositoryService repositoryService = processEngine.getRepositoryService();
//    Deployment deployment = repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
//
//    assertEquals(before + 1, processEngine.getRepositoryService().findDeployments().size());
//    deployment = repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
//    assertEquals(before + 1, processEngine.getRepositoryService().findDeployments().size());
//
//    if (deployment != null) {
//      repositoryService.deleteDeployment(deployment.getId());
//    }
//
//  }
//
//  @Test
//  @DirtiesContext
//  public void testConfigureDeploymentCheck() throws Exception {
//
//    int before = processEngine.getRepositoryService().findDeployments().size();
//
//    RepositoryService repositoryService = processEngine.getRepositoryService();
//
//    // (N.B. Spring 3 resolves Resource[] from a pattern to FileSystemResource
//    // instances)
//    FileSystemResource resource = new FileSystemResource(new ClassPathResource("staticProcess.bpmn20.xml", getClass()).getFile());
//    String path = resource.getFile().getAbsolutePath();
//
//    Deployment deployment = repositoryService.createDeployment().name(path).addInputStream(path, resource.getInputStream()).deploy();
//    // Should be identical to resource configured in factory bean so no new
//    // deployment
//    assertEquals(before, processEngine.getRepositoryService().findDeployments().size());
//
//    if (deployment != null) {
//      repositoryService.deleteDeployment(deployment.getId());
//    }
//
//  }
}
