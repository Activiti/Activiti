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
import java.util.List;

import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.runtime.ProcessInstanceRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.UserCache;
import org.activiti.app.service.api.UserCache.CachedUser;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractProcessInstanceQueryResource {

  private static final int DEFAULT_PAGE_SIZE = 25;

  @Autowired
  protected RepositoryService repositoryService;

  @Autowired
  protected HistoryService historyService;

  @Autowired
  protected UserCache userCache;

  public ResultListDataRepresentation getProcessInstances(ObjectNode requestNode) {

    HistoricProcessInstanceQuery instanceQuery = historyService.createHistoricProcessInstanceQuery();

    User currentUser = SecurityUtils.getCurrentUserObject();
    instanceQuery.involvedUser(String.valueOf(currentUser.getId()));

    // Process definition
    JsonNode processDefinitionIdNode = requestNode.get("processDefinitionId");
    if (processDefinitionIdNode != null && processDefinitionIdNode.isNull() == false) {
      instanceQuery.processDefinitionId(processDefinitionIdNode.asText());
    }

    JsonNode deploymentKeyNode = requestNode.get("deploymentKey");
    if (deploymentKeyNode != null && deploymentKeyNode.isNull() == false) {
      // Results need to be filtered in an app-context. We need to fetch the deployment id for this app and use that in the query
      List<Deployment> deployments = repositoryService.createDeploymentQuery().deploymentKey(deploymentKeyNode.asText()).list();

      List<String> deploymentIds = new ArrayList<String>();
      for (Deployment deployment : deployments) {
        deploymentIds.add(deployment.getId());
      }

      instanceQuery.deploymentIdIn(deploymentIds);
    }

    // State filtering
    JsonNode stateNode = requestNode.get("state");
    if (stateNode != null && !stateNode.isNull()) {
      String state = stateNode.asText();
      if ("running".equals(state)) {
        instanceQuery.unfinished();
      } else if ("completed".equals(state)) {
        instanceQuery.finished();
      } else if (!"all".equals(state)) {
        throw new BadRequestException("Illegal state filter value passed, only 'running', 'completed' or 'all' are supported");
      }
    } else {
      // Default filtering, only running
      instanceQuery.unfinished();
    }

    // Sort and ordering
    JsonNode sortNode = requestNode.get("sort");
    if (sortNode != null && !sortNode.isNull()) {

      if ("created-desc".equals(sortNode.asText())) {
        instanceQuery.orderByProcessInstanceStartTime().desc();
      } else if ("created-asc".equals(sortNode.asText())) {
        instanceQuery.orderByProcessInstanceStartTime().asc();
      } else if ("ended-desc".equals(sortNode.asText())) {
        instanceQuery.orderByProcessInstanceEndTime().desc();
      } else if ("ended-asc".equals(sortNode.asText())) {
        instanceQuery.orderByProcessInstanceEndTime().asc();
      }

    } else {
      // Revert to default
      instanceQuery.orderByProcessInstanceStartTime().desc();
    }

    int page = 0;
    JsonNode pageNode = requestNode.get("page");
    if (pageNode != null && !pageNode.isNull()) {
      page = pageNode.asInt(0);
    }

    int size = DEFAULT_PAGE_SIZE;
    JsonNode sizeNode = requestNode.get("size");
    if (sizeNode != null && !sizeNode.isNull()) {
      size = sizeNode.asInt(DEFAULT_PAGE_SIZE);
    }

    List<HistoricProcessInstance> instances = instanceQuery.listPage(page * size, size);
    ResultListDataRepresentation result = new ResultListDataRepresentation(convertInstanceList(instances));

    // In case we're not on the first page and the size exceeds the page size, we need to do an additional count for the total
    if (page != 0 || instances.size() == size) {
      Long totalCount = instanceQuery.count();
      result.setTotal(Long.valueOf(totalCount.intValue()));
      result.setStart(page * size);
    }
    return result;
  }

  protected List<ProcessInstanceRepresentation> convertInstanceList(List<HistoricProcessInstance> instances) {
    List<ProcessInstanceRepresentation> result = new ArrayList<ProcessInstanceRepresentation>();
    if (CollectionUtils.isNotEmpty(instances)) {

      for (HistoricProcessInstance processInstance : instances) {
        User userRep = null;
        if (processInstance.getStartUserId() != null) {
          CachedUser user = userCache.getUser(processInstance.getStartUserId());
          if (user != null && user.getUser() != null) {
            userRep = user.getUser();
          }
        }

        ProcessDefinitionEntity procDef = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());
        ProcessInstanceRepresentation instanceRepresentation = new ProcessInstanceRepresentation(processInstance, procDef, procDef.isGraphicalNotationDefined(), userRep);
        result.add(instanceRepresentation);
      }

    }
    return result;
  }
}
