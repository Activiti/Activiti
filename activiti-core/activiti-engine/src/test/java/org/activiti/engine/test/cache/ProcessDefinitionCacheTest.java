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

package org.activiti.engine.test.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.test.AbstractTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

/**
 * Test cases for testing functionality when the process engine is rebooted.
 */
public class ProcessDefinitionCacheTest extends AbstractTestCase {

    // Test for a bug: when the process engine is rebooted the
    // cache is cleaned and the deployed process definition is
    // removed from the process cache. This led to problems because
    // the id wasn't fetched from the DB after a redeploy.
    public void testStartProcessInstanceByIdAfterReboot() {

        // In case this test is run in a test suite, previous engines might
        // have been initialized and cached. First we close the
        // existing process engines to make sure that the db is clean
        // and that there are no existing process engines involved.
        ProcessEngines.destroy();

        // Creating the DB schema (without building a process engine)
        ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
        processEngineConfiguration.setProcessEngineName("reboot-test-schema");
        processEngineConfiguration.setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000");
        ProcessEngine schemaProcessEngine = processEngineConfiguration.buildProcessEngine();

        // Create process engine and deploy test process
        ProcessEngine processEngine = new StandaloneProcessEngineConfiguration().setProcessEngineName("reboot-test").setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE)
                .setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000").setAsyncExecutorActivate(false).buildProcessEngine();

        processEngine.getRepositoryService().createDeployment().addClasspathResource("org/activiti/engine/test/cache/originalProcess.bpmn20.xml").deploy();

        // verify existence of process definition
        List<ProcessDefinition> processDefinitions = processEngine.getRepositoryService().createProcessDefinitionQuery().list();

        assertThat(processDefinitions).hasSize(1);

        // Start a new Process instance
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitions.get(0).getId());
        String processInstanceId = processInstance.getId();
        assertThat(processInstance).isNotNull();

        // Close the process engine
        processEngine.close();
        assertThat(processEngine.getRuntimeService()).isNotNull();

        // Reboot the process engine
        processEngine = new StandaloneProcessEngineConfiguration().setProcessEngineName("reboot-test").setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE)
                .setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000").setAsyncExecutorActivate(false).buildProcessEngine();

        // Check if the existing process instance is still alive
        processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        assertThat(processInstance).isNotNull();

        // Complete the task. That will end the process instance
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().list().get(0);
        taskService.complete(task.getId());

        // Check if the process instance has really ended. This means that the
        // process definition has
        // re-loaded into the process definition cache
        processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        assertThat(processInstance).isNull();

        // Extra check to see if a new process instance can be started as well
        processInstance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitions.get(0).getId());
        assertThat(processInstance).isNotNull();

        // close the process engine
        processEngine.close();

        // Cleanup schema
        schemaProcessEngine.close();
    }

    public void testDeployRevisedProcessAfterDeleteOnOtherProcessEngine() {

        // Setup both process engines
        ProcessEngine processEngine1 = new StandaloneProcessEngineConfiguration().setProcessEngineName("reboot-test-schema")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE).setJdbcUrl("jdbc:h2:mem:activiti-process-cache-test;DB_CLOSE_DELAY=1000")
                .setAsyncExecutorActivate(false).buildProcessEngine();
        RepositoryService repositoryService1 = processEngine1.getRepositoryService();

        ProcessEngine processEngine2 = new StandaloneProcessEngineConfiguration().setProcessEngineName("reboot-test")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE).setJdbcUrl("jdbc:h2:mem:activiti-process-cache-test;DB_CLOSE_DELAY=1000")
                .setAsyncExecutorActivate(false).buildProcessEngine();
        RepositoryService repositoryService2 = processEngine2.getRepositoryService();
        RuntimeService runtimeService2 = processEngine2.getRuntimeService();
        TaskService taskService2 = processEngine2.getTaskService();

        // Deploy first version of process: start->originalTask->end on first
        // process engine
        String deploymentId = repositoryService1.createDeployment().addClasspathResource("org/activiti/engine/test/cache/originalProcess.bpmn20.xml").deploy().getId();

        // Start process instance on second engine
        String processDefinitionId = repositoryService2.createProcessDefinitionQuery().singleResult().getId();
        runtimeService2.startProcessInstanceById(processDefinitionId);
        Task task = taskService2.createTaskQuery().singleResult();
        assertThat(task.getName()).isEqualTo("original task");

        // Delete the deployment on second process engine
        repositoryService2.deleteDeployment(deploymentId,
                                            true);
        assertThat(repositoryService2.createDeploymentQuery().count()).isEqualTo(0);
        assertThat(runtimeService2.createProcessInstanceQuery().count()).isEqualTo(0);

        // deploy a revised version of the process: start->revisedTask->end on
        // first process engine
        //
        // Before the bugfix, this would set the cache on the first process
        // engine,
        // but the second process engine still has the original process
        // definition in his cache.
        // Since there is a deployment delete in between, the new generated
        // process definition id is the same
        // as in the original deployment, making the second process engine using
        // the old cached process definition.
        deploymentId = repositoryService1.createDeployment().addClasspathResource("org/activiti/engine/test/cache/revisedProcess.bpmn20.xml").deploy().getId();

        // Start process instance on second process engine -> must use revised
        // process definition
        repositoryService2.createProcessDefinitionQuery().singleResult().getId();
        runtimeService2.startProcessInstanceByKey("oneTaskProcess");
        task = taskService2.createTaskQuery().singleResult();
        assertThat(task.getName()).isEqualTo("revised task");

        // cleanup
        repositoryService1.deleteDeployment(deploymentId,
                                            true);
        processEngine1.close();
        processEngine2.close();
    }
}
