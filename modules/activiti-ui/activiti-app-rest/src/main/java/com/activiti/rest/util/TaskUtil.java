/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.rest.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.task.TaskInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.model.runtime.TaskRepresentation;

public class TaskUtil {
    
    public static void fillPermissionInformation(TaskRepresentation taskRepresentation, TaskInfo task, User currentUser, 
            HistoryService historyService, RepositoryService repositoryService) {
        
        String processInstanceStartUserId = null;
        boolean initiatorCanCompleteTask = true;
        boolean isMemberOfCandidateGroup = false;
        boolean isMemberOfCandidateUsers = false;
        
        if (task.getProcessInstanceId() != null) {
            
            HistoricProcessInstance historicProcessInstance = 
                    historyService.createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            
            if (historicProcessInstance != null && StringUtils.isNotEmpty(historicProcessInstance.getStartUserId())) {
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
                    if ((CollectionUtils.isNotEmpty(userTask.getCandidateGroups()) && userTask.getCandidateGroups().size() == 1 && 
                            userTask.getCandidateGroups().get(0).contains("${taskAssignmentBean.assignTaskToCandidateGroups('")) 
                            || 
                            (CollectionUtils.isNotEmpty(userTask.getCandidateUsers()) && userTask.getCandidateUsers().size() == 1 && 
                            userTask.getCandidateUsers().get(0).contains("${taskAssignmentBean.assignTaskToCandidateUsers('"))) {
                        
                        List<HistoricVariableInstance> processVariables = historyService.createHistoricVariableInstanceQuery()
                                .processInstanceId(task.getProcessInstanceId())
                                .list();
                        if (CollectionUtils.isNotEmpty(processVariables)) {
                            for (HistoricVariableInstance historicVariableInstance : processVariables) {
                                variableMap.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
                            }
                        }
                    }
                    
                    if (CollectionUtils.isNotEmpty(userTask.getCandidateGroups())) {
                        List<Group> groups = currentUser.getGroups();
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
                                    if (fieldValue != null && NumberUtils.isNumber(fieldValue.toString()) && 
                                            String.valueOf(currentUser.getId()).equals(fieldValue.toString())) {
                                        
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
            }
        }
        
        taskRepresentation.setProcessInstanceStartUserId(processInstanceStartUserId);
        taskRepresentation.setInitiatorCanCompleteTask(initiatorCanCompleteTask);
        taskRepresentation.setMemberOfCandidateGroup(isMemberOfCandidateGroup);
        taskRepresentation.setMemberOfCandidateUsers(isMemberOfCandidateUsers);
    }
}
