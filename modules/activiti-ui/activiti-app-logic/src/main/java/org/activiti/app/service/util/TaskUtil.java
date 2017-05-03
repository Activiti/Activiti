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
package org.activiti.app.service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.app.model.idm.GroupRepresentation;
import org.activiti.app.model.idm.UserRepresentation;
import org.activiti.app.model.runtime.TaskRepresentation;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.task.TaskInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class TaskUtil {

  public static void fillPermissionInformation(TaskRepresentation taskRepresentation, TaskInfo task, User currentUser, 
      IdentityService identityService, HistoryService historyService, RepositoryService repositoryService) {

    String processInstanceStartUserId = null;
    boolean initiatorCanCompleteTask = true;
    boolean isMemberOfCandidateGroup = false;
    boolean isMemberOfCandidateUsers = false;

    if (task.getProcessInstanceId() != null) {

      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();

      if (historicProcessInstance != null ) {
        processInstanceStartUserId = historicProcessInstance.getStartUserId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        if (flowElement != null && flowElement instanceof UserTask) {
          UserTask userTask = (UserTask) flowElement;
          List<ExtensionElement> extensionElements = userTask.getExtensionElements().get("initiator-can-complete");
          if (CollectionUtils.isNotEmpty(extensionElements)) {
            String value = extensionElements.get(0).getElementText();
            if (StringUtils.isNotEmpty(value)) {
              initiatorCanCompleteTask = Boolean.valueOf(value);
            }
          }

          Map<String, Object> variableMap = new HashMap<String, Object>();
          if ((CollectionUtils.isNotEmpty(userTask.getCandidateGroups()) && userTask.getCandidateGroups().size() == 1
              && userTask.getCandidateGroups().get(0).contains("${taskAssignmentBean.assignTaskToCandidateGroups('"))
              || (CollectionUtils.isNotEmpty(userTask.getCandidateUsers()) && userTask.getCandidateUsers().size() == 1
                  && userTask.getCandidateUsers().get(0).contains("${taskAssignmentBean.assignTaskToCandidateUsers('"))) {

            List<HistoricVariableInstance> processVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(task.getProcessInstanceId()).list();
            if (CollectionUtils.isNotEmpty(processVariables)) {
              for (HistoricVariableInstance historicVariableInstance : processVariables) {
                variableMap.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
              }
            }
          }

          if (CollectionUtils.isNotEmpty(userTask.getCandidateGroups())) {
            List<Group> groups = identityService.createGroupQuery().groupMember(currentUser.getId()).list();
            if (CollectionUtils.isNotEmpty(groups)) {

              List<String> groupIds = new ArrayList<String>();
              if (userTask.getCandidateGroups().size() == 1 && userTask.getCandidateGroups().get(0).contains("${taskAssignmentBean.assignTaskToCandidateGroups('")) {

                String candidateGroupString = userTask.getCandidateGroups().get(0);
                candidateGroupString = candidateGroupString.replace("${taskAssignmentBean.assignTaskToCandidateGroups('", "");
                candidateGroupString = candidateGroupString.replace("', execution)}", "");
                String groupsArray[] = candidateGroupString.split(",");
                for (String group : groupsArray) {
                  if (group.contains("field(")) {
                    String fieldCandidate = group.trim().substring(6, group.length() - 1);
                    Object fieldValue = variableMap.get(fieldCandidate);
                    if (fieldValue != null && NumberUtils.isNumber(fieldValue.toString())) {
                      groupIds.add(fieldValue.toString());
                    }

                  } else {
                    groupIds.add(group);
                  }
                }

              } else {
                groupIds.addAll(userTask.getCandidateGroups());
              }

              for (Group group : groups) {
                if (groupIds.contains(String.valueOf(group.getId()))) {
                  isMemberOfCandidateGroup = true;
                  break;
                }
              }
            }
          }

          if (CollectionUtils.isNotEmpty(userTask.getCandidateUsers())) {
            if (userTask.getCandidateUsers().size() == 1 && userTask.getCandidateUsers().get(0).contains("${taskAssignmentBean.assignTaskToCandidateUsers('")) {

              String candidateUserString = userTask.getCandidateUsers().get(0);
              candidateUserString = candidateUserString.replace("${taskAssignmentBean.assignTaskToCandidateUsers('", "");
              candidateUserString = candidateUserString.replace("', execution)}", "");
              String users[] = candidateUserString.split(",");
              for (String user : users) {
                if (user.contains("field(")) {
                  String fieldCandidate = user.substring(6, user.length() - 1);
                  Object fieldValue = variableMap.get(fieldCandidate);
                  if (fieldValue != null && NumberUtils.isNumber(fieldValue.toString()) && String.valueOf(currentUser.getId()).equals(fieldValue.toString())) {

                    isMemberOfCandidateGroup = true;
                    break;
                  }

                } else if (user.equals(String.valueOf(currentUser.getId()))) {
                  isMemberOfCandidateGroup = true;
                  break;
                }
              }

            } else if (userTask.getCandidateUsers().contains(String.valueOf(currentUser.getId()))) {
              isMemberOfCandidateUsers = true;
            }
          }
        }
        
        if (!isMemberOfCandidateGroup && !isMemberOfCandidateUsers) {
          List<String> candidateGroupIds = new ArrayList<String>();
          List<HistoricIdentityLink> links = (List<HistoricIdentityLink>)historyService.getHistoricIdentityLinksForTask(task.getId());
          for (HistoricIdentityLink historicIdentityLink : links) {
              if (!isMemberOfCandidateUsers && StringUtils.isNotEmpty((CharSequence)historicIdentityLink.getUserId()) && String.valueOf(currentUser.getId()).equals(historicIdentityLink.getUserId()) && "candidate".equalsIgnoreCase(historicIdentityLink.getType())) {
                  isMemberOfCandidateUsers = true;
              }
              else {
                  if (!StringUtils.isNotEmpty((CharSequence)historicIdentityLink.getGroupId()) || !"candidate".equalsIgnoreCase(historicIdentityLink.getType())) {
                      continue;
                  }
                  candidateGroupIds.add(historicIdentityLink.getGroupId());
              }
          }
          List<GroupRepresentation> groups2 = (List<GroupRepresentation>)new UserRepresentation(currentUser).getGroups();
          if (groups2 != null) {
              for (GroupRepresentation group3 : groups2) {
                  if (candidateGroupIds.contains(group3.getId().toString())) {
                      isMemberOfCandidateGroup = true;
                      break;
                  }
              }
          }
      }
      }
    }

    taskRepresentation.setProcessInstanceStartUserId(processInstanceStartUserId);
    taskRepresentation.setInitiatorCanCompleteTask(initiatorCanCompleteTask);
    taskRepresentation.setMemberOfCandidateGroup(isMemberOfCandidateGroup);
    taskRepresentation.setMemberOfCandidateUsers(isMemberOfCandidateUsers);
  }
}
