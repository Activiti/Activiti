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
package org.activiti.app.rest.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.runtime.ProcessDefinitionRepresentation;
import org.activiti.app.service.runtime.PermissionService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.form.api.FormRepositoryService;
import org.activiti.form.model.FormDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractProcessDefinitionsResource {

  @Autowired
  protected RepositoryService repositoryService;

  @Autowired
  protected FormRepositoryService formRepositoryService;

  @Autowired
  protected PermissionService permissionService;

  public ResultListDataRepresentation getProcessDefinitions(Boolean latest, String deploymentKey) {

    ProcessDefinitionQuery definitionQuery = repositoryService.createProcessDefinitionQuery();

    if (deploymentKey != null) {
      Deployment deployment = repositoryService.createDeploymentQuery().deploymentKey(deploymentKey).latest().singleResult();
      
      if (deployment != null) {
        definitionQuery.deploymentId(deployment.getId());
      } else {
        return new ResultListDataRepresentation(new ArrayList<ProcessDefinitionRepresentation>());
      }

    } else {

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
