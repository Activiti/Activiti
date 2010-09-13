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

package org.activiti.spring.test;

import org.activiti.engine.RepositoryService;
import org.activiti.pvm.test.PvmTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @author Tom Baeyens
 */
public class SpringTransactionIntegrationTest extends PvmTestCase {

  public void testBasicActivitiSpringIntegration() {
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("org/activiti/spring/test/SpringTransactionIntegrationTest-context.xml");
    
    RepositoryService repositoryService = (RepositoryService) applicationContext.getBean("repositoryService");
    String deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/activiti/spring/test/hello.bpmn20.xml")
      .deploy()
      .getId();

    UserBean userBean = (UserBean) applicationContext.getBean("userBean");
    userBean.hello();
    
    repositoryService.deleteDeploymentCascade(deploymentId);
    
    applicationContext.destroy();
  }
}
