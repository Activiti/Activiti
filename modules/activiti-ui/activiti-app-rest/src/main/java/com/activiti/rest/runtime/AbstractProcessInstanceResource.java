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

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.Form;
import com.activiti.model.editor.form.FormDefinitionRepresentation;
import com.activiti.model.idm.LightUserRepresentation;
import com.activiti.model.runtime.ProcessInstanceRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.UserCache;
import com.activiti.service.api.UserCache.CachedUser;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.runtime.FormProcessingService;
import com.activiti.service.runtime.PermissionService;
import com.activiti.service.runtime.ProcessInstanceService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractProcessInstanceResource {

    private static final Logger logger = LoggerFactory.getLogger(AbstractProcessInstanceResource.class);

    @Inject
    protected RepositoryService repositoryService;
    
    @Inject
    protected HistoryService historyService;
    
    @Inject
    protected RuntimeService runtimeService;
    
    @Inject
    protected PermissionService permissionService;
    
    @Inject
    protected ProcessInstanceService processInstanceService;
    
    @Inject
    protected FormProcessingService formProcessingService;
    
    @Inject
    protected UserCache userCache;

    public ProcessInstanceRepresentation getProcessInstance(String processInstanceId, HttpServletResponse response) {
    	
        HistoricProcessInstance processInstance = 
                historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        
    	if (!permissionService.hasReadPermissionOnProcessInstance(SecurityUtils.getCurrentUserObject(), processInstance, processInstanceId)) {
    		 throw new NotFoundException("Process with id: " + processInstanceId + " does not exist or is not available for this user");
    	}
    	
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(
                processInstance.getProcessDefinitionId());
        
        LightUserRepresentation userRep = null;
        if (processInstance.getStartUserId() != null) {
            CachedUser user = userCache.getUser(Long.parseLong(processInstance.getStartUserId()));
            if (user != null && user.getUser() != null) {
                userRep = new LightUserRepresentation(user.getUser());
            }
        }
        
        ProcessInstanceRepresentation processInstanceResult = new ProcessInstanceRepresentation(processInstance, processDefinition, 
                processDefinition.isGraphicalNotationDefined(), userRep);
        
        Form form = formProcessingService.getStartForm(processInstance.getProcessDefinitionId());
        if (form != null) {
            processInstanceResult.setStartFormDefined(true);
        }
        
        return processInstanceResult;
    }
    
    public FormDefinitionRepresentation getProcessInstanceStartForm(String processInstanceId, HttpServletResponse response) {
        
        HistoricProcessInstance processInstance = 
                historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        
        if (!permissionService.hasReadPermissionOnProcessInstance(SecurityUtils.getCurrentUserObject(), processInstance, processInstanceId)) {
             throw new NotFoundException("Process with id: " + processInstanceId + " does not exist or is not available for this user");
        }
        
        return formProcessingService.getStartFormDefinition(processInstanceId);
    }
    
    public void deleteProcessInstance(String processInstanceId) {
        
        User currentUser = SecurityUtils.getCurrentUserObject();
        
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .startedBy(String.valueOf(currentUser.getId())) // Permission check not needed since we do involved user here
                .singleResult();
        
        if (processInstance == null) {
            throw new NotFoundException("Process with id: " + processInstanceId + " does not exist or is not started by this user");
        }
        
        if (processInstance.getEndTime() != null) {
            // Check if a hard delete of process instance is allowed
            if (!permissionService.canDeleteProcessInstance(currentUser, processInstance)) {
                throw new NotFoundException("Process with id: " + processInstanceId + " is already completed and can't be deleted");
            }
            
            // Delete cascade behavior in separate service to share a single transaction for all nested service-calls
            processInstanceService.deleteProcessInstance(processInstanceId);
            
        } else {
            runtimeService.deleteProcessInstance(processInstanceId, "Cancelled by " + SecurityUtils.getCurrentUserId());
        }
    }

}
