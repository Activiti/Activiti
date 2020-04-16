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
package org.activiti.standalone.parsing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.test.Deployment;

/**

 */
public class CustomActivityBehaviorFactoryTest extends ResourceActivitiTestCase {

  public CustomActivityBehaviorFactoryTest() {
    super("org/activiti/standalone/parsing/custom.activitybehaviorfactory.activiti.cfg.xml");
  }

  // The custom activity factory will change this value
  public static AtomicInteger COUNTER = new AtomicInteger(0);

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    COUNTER.set(0);
  }

  @Deployment
  public void testCustomActivityBehaviorFactory() {
    int nrOfProcessInstances = 6;
    for (int i = 0; i < nrOfProcessInstances; i++) {
      runtimeService.startProcessInstanceByKey("oneTaskProcess");
    }

    assertThat(COUNTER.get()).isEqualTo(nrOfProcessInstances);
  }

}
