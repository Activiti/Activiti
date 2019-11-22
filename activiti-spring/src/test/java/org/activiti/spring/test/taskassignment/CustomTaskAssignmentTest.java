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
package org.activiti.spring.test.taskassignment;

import java.util.List;

import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

/**

 */
@ContextConfiguration("classpath:org/activiti/spring/test/taskassignment/taskassignment-context.xml")
public class CustomTaskAssignmentTest extends SpringActivitiTestCase {

    private void cleanUp() {
        List<org.activiti.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.activiti.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId(),
                                               true);
        }
    }

    @Override
    public void tearDown() {
        cleanUp();
    }

    @Deployment
    public void testSetAssigneeThroughSpringService() {
        runtimeService.startProcessInstanceByKey("assigneeThroughSpringService",
                                                 CollectionUtil.singletonMap("emp",
                                                                             "fozzie"));
        assertEquals(1,
                     taskService.createTaskQuery().taskAssignee("Kermit The Frog").count());
    }

    @Deployment
    public void testSetCandidateUsersThroughSpringService() {
        runtimeService.startProcessInstanceByKey("candidateUsersThroughSpringService",
                                                 CollectionUtil.singletonMap("emp",
                                                                             "fozzie"));
        assertEquals(1,
                     taskService.createTaskQuery().taskCandidateUser("kermit").count());
        assertEquals(1,
                     taskService.createTaskQuery().taskCandidateUser("fozzie").count());
        assertEquals(1,
                     taskService.createTaskQuery().taskCandidateUser("gonzo").count());
        assertEquals(0,
                     taskService.createTaskQuery().taskCandidateUser("mispiggy").count());
    }

    @Deployment
    public void testSetCandidateGroupsThroughSpringService() {
        runtimeService.startProcessInstanceByKey("candidateUsersThroughSpringService",
                                                 CollectionUtil.singletonMap("emp",
                                                                             "fozzie"));
        assertEquals(1,
                     taskService.createTaskQuery().taskCandidateGroup("management").count());
        assertEquals(1,
                     taskService.createTaskQuery().taskCandidateGroup("directors").count());
        assertEquals(1,
                     taskService.createTaskQuery().taskCandidateGroup("accountancy").count());
        assertEquals(0,
                     taskService.createTaskQuery().taskCandidateGroup("sales").count());
    }
}
