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
package org.activiti.dmn.engine.impl;

import org.activiti.dmn.api.DmnRepositoryService;
import org.activiti.dmn.api.DmnRuleService;
import org.activiti.dmn.engine.DmnEngine;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.DmnEngines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tijs Rademakers
 */
public class DmnEngineImpl implements DmnEngine {

    private static Logger log = LoggerFactory.getLogger(DmnEngineImpl.class);

    protected String name;
    protected DmnRepositoryService dmnRepositoryService;
    protected DmnRuleService dmnRuleService;
    protected DmnEngineConfiguration dmnEngineConfiguration;

    public DmnEngineImpl(DmnEngineConfiguration dmnEngineConfiguration) {
        this.dmnEngineConfiguration = dmnEngineConfiguration;
        this.name = dmnEngineConfiguration.getDmnEngineName();
        this.dmnRepositoryService = dmnEngineConfiguration.getDmnRepositoryService();
        this.dmnRuleService = dmnEngineConfiguration.getDmnRuleService();

        if (name == null) {
            log.info("default activiti DmnEngine created");
        } else {
            log.info("DmnEngine {} created", name);
        }

        DmnEngines.registerDmnEngine(this);
    }

    public void close() {
        DmnEngines.unregister(this);
    }

    // getters and setters
    // //////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public DmnRepositoryService getDmnRepositoryService() {
        return dmnRepositoryService;
    }

    public DmnRuleService getDmnRuleService() {
        return dmnRuleService;
    }

    public DmnEngineConfiguration getDmnEngineConfiguration() {
        return dmnEngineConfiguration;
    }
}
