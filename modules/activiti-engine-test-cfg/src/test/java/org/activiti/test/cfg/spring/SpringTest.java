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

import org.activiti.ActivitiException;
import org.activiti.Deployment;
import org.activiti.ProcessEngine;
import org.activiti.ProcessService;
import org.activiti.impl.util.LogUtil;
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
  public void testSpringTransaction() {
    int before = processEngine.getProcessService().findDeployments().size();
    userBean.doTransactional();
    assertEquals(before + 1, processEngine.getProcessService().findDeployments().size());
  }

  @Test
  public void testSpringTransactionRollback() {
    int before = processEngine.getProcessService().findDeployments().size();
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
      assertEquals(before, processEngine.getProcessService().findDeployments().size());
    }
  }

  @Test
  @DirtiesContext
  public void testSaveDeployment() {

    int before = processEngine.getProcessService().findDeployments().size();

    String resource = ClassUtils.addResourcePathToPackagePath(getClass(), "testProcess.bpmn20.xml");
    ProcessService processService = processEngine.getProcessService();
    Deployment deployment = processService.createDeployment().name(resource).addClasspathResource(resource).deploy();

    assertEquals(before + 1, processEngine.getProcessService().findDeployments().size());
    deployment = processService.createDeployment().name(resource).addClasspathResource(resource).deploy();
    assertEquals(before + 1, processEngine.getProcessService().findDeployments().size());

    if (deployment != null) {
      processService.deleteDeploymentCascade(deployment.getId());
    }

  }

  @Test
  @DirtiesContext
  public void testConfigureDeploymentCheck() throws Exception {

    int before = processEngine.getProcessService().findDeployments().size();

    ProcessService processService = processEngine.getProcessService();

    // (N.B. Spring 3 resolves Resource[] from a pattern to FileSystemResource instances)
    FileSystemResource resource = new FileSystemResource(new ClassPathResource("staticProcess.bpmn20.xml", getClass()).getFile());
    String path = resource.getFile().getAbsolutePath();

    Deployment deployment = processService.createDeployment().name(path).addInputStream(path, resource.getInputStream()).deploy();
    // Should be identical to resource configured in factory bean so no new deployment
    assertEquals(before, processEngine.getProcessService().findDeployments().size());

    if (deployment != null) {
      processService.deleteDeploymentCascade(deployment.getId());
    }

  }
}
