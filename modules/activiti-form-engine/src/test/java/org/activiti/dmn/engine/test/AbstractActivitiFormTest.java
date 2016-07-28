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
package org.activiti.dmn.engine.test;

import org.activiti.form.api.FormRepositoryService;
import org.activiti.form.api.FormService;
import org.activiti.form.engine.FormEngine;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.test.ActivitiFormRule;
import org.junit.Before;
import org.junit.Rule;

/**
 * Parent class for internal Activiti Form tests.
 * 
 * Boots up a dmn engine and caches it.
 * 
 * When using H2 and the default schema name, it will also boot the H2 webapp (reachable with browser on http://localhost:8082/)
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class AbstractActivitiFormTest {

  public static String H2_TEST_JDBC_URL = "jdbc:h2:mem:activitiform;DB_CLOSE_DELAY=1000";

  @Rule
  public ActivitiFormRule activitiRule = new ActivitiFormRule();

  protected static FormEngine cachedFormEngine;
  protected FormEngineConfiguration formEngineConfiguration;
  protected FormRepositoryService repositoryService;
  protected FormService formService;

  @Before
  public void initFormEngine() {
    if (cachedFormEngine == null) {
      cachedFormEngine = activitiRule.getFormEngine();
    }

    this.formEngineConfiguration = cachedFormEngine.getFormEngineConfiguration();
    this.repositoryService = cachedFormEngine.getFormRepositoryService();
    this.formService = cachedFormEngine.getFormService();
  }

}
