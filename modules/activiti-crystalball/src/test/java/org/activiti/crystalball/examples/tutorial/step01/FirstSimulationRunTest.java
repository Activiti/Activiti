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
package org.activiti.crystalball.examples.tutorial.step01;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.test.Deployment;

/**
 * This class provides the first insight into simulation run driven by process definition
 *
 * @author martin.grofcik
 */
public class FirstSimulationRunTest extends ResourceActivitiTestCase {

  public FirstSimulationRunTest() {
    super("org/activiti/crystalball/examples/tutorial/step01/FirstSimulationRunTest.cfg.xml");
  }

  @Deployment
  public void testSimulationRun() {
    runtimeService.startProcessInstanceByKey("basicSimulationRun");
    // all simulationManager executions are finished
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // simulation run check (Simulation run has side effect. The counter value is increased)
    assertThat(Counter.value.get(), is(1l));
  }

  @Override
  protected void closeDownProcessEngine() {
    super.closeDownProcessEngine();
    ProcessEngines.destroy();
  }
}
