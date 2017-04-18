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
package org.activiti.test.serviceTask;

import java.util.List;

import org.activiti.engine.DynamicBpmnService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.cfg.ProcessEngineConfigurator;
import org.activiti.engine.cfg.security.CommandExecutorContext;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import junit.framework.AssertionFailedError;

/**
 * @author Vasile Dirla
 */
public abstract class ServiceTaskBaseTest {

    protected ProcessEngine processEngine;
    protected RuntimeService runtimeService;
    protected RepositoryService repositoryService;
    protected TaskService taskService;
    protected HistoryService historyService;
    protected DynamicBpmnService dynamicBpmnService;


    protected enum OsType {
        LINUX, WINDOWS, MAC, SOLARIS, UNKOWN
    }

    protected OsType osType;

    OsType getSystemOsType() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win"))
            return OsType.WINDOWS;
        else if (osName.contains("mac"))
            return OsType.MAC;
        else if ((osName.contains("nix")) || (osName.contains("nux")))
            return OsType.LINUX;
        else if (osName.contains("sunos"))
            return OsType.SOLARIS;
        else
            return OsType.UNKOWN;
    }

    @Before
    public void initProcessEngine() {
        CommandExecutorContext.setShellExecutorContextFactory(null);

        osType = getSystemOsType();

        StandaloneInMemProcessEngineConfiguration processEngineConfiguration = (StandaloneInMemProcessEngineConfiguration) new StandaloneInMemProcessEngineConfiguration()
                .setDatabaseSchemaUpdate("create-drop")
                .setEnableProcessDefinitionInfoCache(true);

        List<ProcessEngineConfigurator> configurators = getConfigurators();

        if (configurators != null) {
            for (ProcessEngineConfigurator processEngineConfigurator : configurators) {
                processEngineConfiguration.addConfigurator(processEngineConfigurator);
            }
        }

        this.processEngine = processEngineConfiguration.buildProcessEngine();

        this.runtimeService = processEngine.getRuntimeService();
        this.repositoryService = processEngine.getRepositoryService();
        this.taskService = processEngine.getTaskService();
        this.historyService = processEngine.getHistoryService();
        this.dynamicBpmnService = processEngine.getDynamicBpmnService();
    }

    protected List<ProcessEngineConfigurator> getConfigurators(){
        return null;
    };

    @After
    public void shutdownProcessEngine() {

        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }

        this.taskService = null;
        this.repositoryService = null;
        this.runtimeService = null;

        this.processEngine.close();
        this.processEngine = null;
    }

    protected void deployProcessDefinition(String classpathResource) {
        repositoryService.createDeployment().addClasspathResource(classpathResource).deploy();
    }

}
