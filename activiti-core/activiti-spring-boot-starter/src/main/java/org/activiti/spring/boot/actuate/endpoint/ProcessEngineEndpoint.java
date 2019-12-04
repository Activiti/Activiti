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
package org.activiti.spring.boot.actuate.endpoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.deploy.DefaultDeploymentCache;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.engine.repository.ProcessDefinition;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Registers a Boot Actuator endpoint that provides information on the
 * running process instance and renders BPMN diagrams of the deployed processes.
 */
@ConfigurationProperties(prefix = "endpoints.activiti")
@Endpoint(id = "activiti")
public class ProcessEngineEndpoint {

    private final ProcessEngine processEngine;

    public ProcessEngineEndpoint(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @ReadOperation
    public Map<String, Object> invoke() {

        Map<String, Object> metrics = new HashMap<String, Object>();

        // Process definitions
        metrics.put("processDefinitionCount",
                    processEngine.getRepositoryService().createProcessDefinitionQuery().count());

        // List of all process definitions
        List<ProcessDefinition> processDefinitions = processEngine.getRepositoryService().createProcessDefinitionQuery().orderByProcessDefinitionKey().asc().list();
        List<String> processDefinitionKeys = new ArrayList<String>();
        for (ProcessDefinition processDefinition : processDefinitions) {
            processDefinitionKeys.add(processDefinition.getKey() + " (v" + processDefinition.getVersion() + ")");
        }
        metrics.put("deployedProcessDefinitions",
                    processDefinitionKeys);

        // Process instances
        Map<String, Object> processInstanceCountMap = new HashMap<String, Object>();
        metrics.put("runningProcessInstanceCount",
                    processInstanceCountMap);
        for (ProcessDefinition processDefinition : processDefinitions) {
            processInstanceCountMap.put(processDefinition.getKey() + " (v" + processDefinition.getVersion() + ")",
                                        processEngine.getRuntimeService().createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).count());
        }
        Map<String, Object> completedProcessInstanceCountMap = new HashMap<String, Object>();
        metrics.put("completedProcessInstanceCount",
                    completedProcessInstanceCountMap);
        for (ProcessDefinition processDefinition : processDefinitions) {
            completedProcessInstanceCountMap.put(processDefinition.getKey() + " (v" + processDefinition.getVersion() + ")",
                                                 processEngine.getHistoryService().createHistoricProcessInstanceQuery().finished().processDefinitionId(processDefinition.getId()).count());
        }

        // Open tasks
        metrics.put("openTaskCount",
                    processEngine.getTaskService().createTaskQuery().count());
        metrics.put("completedTaskCount",
                    processEngine.getHistoryService().createHistoricTaskInstanceQuery().finished().count());

        // Tasks completed today
        metrics.put("completedTaskCountToday",
                    processEngine.getHistoryService().createHistoricTaskInstanceQuery().finished().taskCompletedAfter(
                            new Date(System.currentTimeMillis() - secondsForDays(1))).count());

        // Process steps
        metrics.put("completedActivities",
                    processEngine.getHistoryService().createHistoricActivityInstanceQuery().finished().count());

        // Process definition cache
        DeploymentCache<ProcessDefinitionCacheEntry> deploymentCache = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getProcessDefinitionCache();
        if (deploymentCache instanceof DefaultDeploymentCache) {
            metrics.put("cachedProcessDefinitionCount",
                        ((DefaultDeploymentCache) deploymentCache).size());
        }
        return metrics;
    }

    private long secondsForDays(int days) {
        int hour = 60 * 60 * 1000;
        int day = 24 * hour;
        return days * day;
    }
}
