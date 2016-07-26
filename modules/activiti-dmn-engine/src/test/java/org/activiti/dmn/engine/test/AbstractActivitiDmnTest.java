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

import org.activiti.dmn.api.DmnRepositoryService;
import org.activiti.dmn.api.DmnRuleService;
import org.activiti.dmn.engine.DmnEngine;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.junit.Before;
import org.junit.Rule;

/**
 * Parent class for internal Activiti DMN tests.
 * 
 * Boots up a dmn engine and caches it.
 * 
 * When using H2 and the default schema name, it will also boot the H2 webapp
 * (reachable with browser on http://localhost:8082/)
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class AbstractActivitiDmnTest {

    public static String H2_TEST_JDBC_URL = "jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000";

    @Rule
    public ActivitiDmnRule activitiRule = new ActivitiDmnRule();

    protected static DmnEngine cachedDmnEngine;
    protected DmnEngineConfiguration dmnEngineConfiguration;
    protected DmnRepositoryService repositoryService;
    protected DmnRuleService ruleService;

    @Before
    public void initDmnEngine() {
        if (cachedDmnEngine == null) {
            cachedDmnEngine = activitiRule.getDmnEngine();
        }

        this.dmnEngineConfiguration = cachedDmnEngine.getDmnEngineConfiguration();
        this.repositoryService = cachedDmnEngine.getDmnRepositoryService();
        this.ruleService = cachedDmnEngine.getDmnRuleService();
    }

}
