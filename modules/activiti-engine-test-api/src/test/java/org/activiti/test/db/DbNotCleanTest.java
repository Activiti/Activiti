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
package org.activiti.test.db;

import static org.junit.Assert.assertNotNull;

import org.activiti.engine.Deployment;
import org.activiti.test.ProcessEngineBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Joram Barrez
 * @author Dave Syer
 */
public class DbNotCleanTest {

  @Rule
  public ProcessEngineBuilder processEngineBuilder = new ProcessEngineBuilder();

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testDbNotCleanAfterTest() {

    Deployment deployment = processEngineBuilder.getProcessEngine().getProcessService().createDeployment().addString("test.bpmn20.xml",
            "<definitions xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL' " + "targetNamespace='http://www.activiti.org/bpmn2.0' />").deploy();
    assertNotNull(deployment);

    exception.expect(AssertionError.class);
    exception.expectMessage("Database not clean");

    try {
      // Manually call the check on db cleaning check
      processEngineBuilder.assertDatabaseIsClean();
    } finally {
      processEngineBuilder.getProcessEngine().getProcessService().deleteDeploymentCascade(deployment.getId());
    }

  }

}
