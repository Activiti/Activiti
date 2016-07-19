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
package com.activiti.rest.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.form.engine.FormRepositoryService;
import org.activiti.form.model.FormDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.domain.runtime.RuntimeAppDeployment;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.runtime.ProcessDefinitionRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.RuntimeAppDefinitionService;
import com.activiti.service.exception.NotPermittedException;
import com.activiti.service.runtime.PermissionService;

public abstract class AbstractProcessDefinitionsResource {

  @Autowired
  protected RepositoryService repositoryService;

  @Autowired
  protected FormRepositoryService formRepositoryService;

  @Autowired
  protected RuntimeAppDefinitionService runtimeAppDefinitionService;

  @Autowired
  protected PermissionService permissionService;

  public ResultListDataRepresentation getProcessDefinitions(Boolean latest, Long appDefinitionId) {

    ProcessDefinitionQuery definitionQuery = repositoryService.createProcessDefinitionQuery();

    User currentUser = SecurityUtils.getCurrentUserObject();

    if (appDefinitionId != null) {
      List<RuntimeAppDeployment> appDeployments = runtimeAppDefinitionService.getRuntimeAppDeploymentsForAppId(appDefinitionId);
      if (CollectionUtils.isNotEmpty(appDeployments)) {
        RuntimeAppDeployment latestAppDeployment = null;
        for (RuntimeAppDeployment runtimeAppDeployment : appDeployments) {
          if (latestAppDeployment == null || runtimeAppDeployment.getCreated().after(latestAppDeployment.getCreated())) {
            latestAppDeployment = runtimeAppDeployment;
          }
        }

        if (permissionService.hasReadPermissionOnRuntimeApp(currentUser, appDefinitionId)) {
          if (StringUtils.isNotEmpty(latestAppDeployment.getDeploymentId())) {
            definitionQuery.deploymentId(latestAppDeployment.getDeploymentId());
          } else {
            return new ResultListDataRepresentation();
          }
        } else {
          throw new NotPermittedException();
        }
      } else {
        return new ResultListDataRepresentation(new ArrayList<ProcessDefinitionRepresentation>());
      }

    } else {
      List<RuntimeAppDefinition> appDefinitions = runtimeAppDefinitionService.getDefinitionsForUser(currentUser);
      if (CollectionUtils.isNotEmpty(appDefinitions)) {
        Set<String> deploymentIds = new HashSet<String>();
        for (RuntimeAppDefinition runtimeAppDefinition : appDefinitions) {
          deploymentIds.add(runtimeAppDefinition.getDeploymentId());
        }
        // TODO: UI6 REFACTOR
        // definitionQuery.deploymentIds(deploymentIds);
      } else {
        // When the user doesn't have any apps, don't execute the query and simply return an empty list
        return new ResultListDataRepresentation(new ArrayList<ProcessDefinitionRepresentation>());
      }

      if (latest != null && latest) {
        definitionQuery.latestVersion();
      }
    }

    List<ProcessDefinition> definitions = definitionQuery.list();
    ResultListDataRepresentation result = new ResultListDataRepresentation(convertDefinitionList(definitions));
    return result;
  }

  protected List<ProcessDefinitionRepresentation> convertDefinitionList(List<ProcessDefinition> definitions) {
    Map<String, Boolean> startFormMap = new HashMap<String, Boolean>();
    List<ProcessDefinitionRepresentation> result = new ArrayList<ProcessDefinitionRepresentation>();
    if (CollectionUtils.isNotEmpty(definitions)) {
      for (ProcessDefinition processDefinition : definitions) {
        if (startFormMap.containsKey(processDefinition.getId()) == false) {
          BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
          List<StartEvent> startEvents = bpmnModel.getMainProcess().findFlowElementsOfType(StartEvent.class, false);
          boolean hasStartForm = false;
          for (StartEvent startEvent : startEvents) {
            if (StringUtils.isNotEmpty(startEvent.getFormKey())) {
              FormDefinition formDefinition = formRepositoryService.getFormDefinitionByKey(startEvent.getFormKey());
              if (formDefinition != null) {
                hasStartForm = true;
                break;
              }
            }
          }

          startFormMap.put(processDefinition.getId(), hasStartForm);
        }
        
        ProcessDefinitionRepresentation rep = new ProcessDefinitionRepresentation(processDefinition);
        rep.setHasStartForm(startFormMap.get(processDefinition.getId()));
        result.add(rep);
      }
    }
    return result;
  }
}
