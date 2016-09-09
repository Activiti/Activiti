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
package org.activiti.app.model.runtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.app.model.common.AbstractRepresentation;
import org.activiti.app.model.idm.UserRepresentation;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * REST representation of a process instance.
 * 
 * @author Tijs Rademakers
 */
public class ProcessInstanceRepresentation extends AbstractRepresentation {

    protected String id;
    protected String name;
    protected String businessKey;
    protected String processDefinitionId;
    protected String tenantId;
    protected Date started;
    protected Date ended;
    protected UserRepresentation startedBy;
    protected String processDefinitionName;
    protected String processDefinitionDescription;
    protected String processDefinitionKey;
    protected String processDefinitionCategory;
    protected int processDefinitionVersion;
    protected String processDefinitionDeploymentId;
    protected boolean graphicalNotationDefined;
    protected boolean startFormDefined;
    
    protected List<RestVariable> variables = new ArrayList<RestVariable>();

    public ProcessInstanceRepresentation(ProcessInstance processInstance, ProcessDefinition processDefinition, boolean graphicalNotation, User startedBy) {
        this(processInstance, graphicalNotation, startedBy);
        mapProcessDefinition(processDefinition);
    }

    public ProcessInstanceRepresentation(ProcessInstance processInstance, boolean graphicalNotation, User startedBy) {
        this.id = processInstance.getId();
        this.name = processInstance.getName();
        this.businessKey = processInstance.getBusinessKey();
        this.processDefinitionId = processInstance.getProcessDefinitionId();
        this.tenantId = processInstance.getTenantId();
        this.graphicalNotationDefined = graphicalNotation;
        this.startedBy = new UserRepresentation(startedBy);
    }

    public ProcessInstanceRepresentation(HistoricProcessInstance processInstance, ProcessDefinition processDefinition, boolean graphicalNotation, User startedBy) {
        this(processInstance, graphicalNotation, startedBy);
        mapProcessDefinition(processDefinition);
    }
    
    public ProcessInstanceRepresentation(HistoricProcessInstance processInstance, boolean graphicalNotation, User startedBy) {
        this.id = processInstance.getId();
        this.name = processInstance.getName();
        this.businessKey = processInstance.getBusinessKey();
        this.processDefinitionId = processInstance.getProcessDefinitionId();
        this.tenantId = processInstance.getTenantId();
        this.graphicalNotationDefined = graphicalNotation;
        this.started = processInstance.getStartTime();
        this.ended = processInstance.getEndTime();
        this.startedBy = new UserRepresentation(startedBy);
    }

    protected void mapProcessDefinition(ProcessDefinition processDefinition) {
        if (processDefinition != null) {
            this.processDefinitionName = processDefinition.getName();
            this.processDefinitionDescription = processDefinition.getDescription();
            this.processDefinitionKey = processDefinition.getKey();
            this.processDefinitionCategory = processDefinition.getCategory();
            this.processDefinitionVersion = processDefinition.getVersion();
            this.processDefinitionDeploymentId = processDefinition.getDeploymentId();
        }
    }
    public ProcessInstanceRepresentation() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public UserRepresentation getStartedBy() {
        return startedBy;
    }
    
    public void setStartedBy(UserRepresentation startedBy) {
      this.startedBy = startedBy;
  }
    
    public String getBusinessKey() {
        return businessKey;
    }
    
    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public Date getStarted() {
        return started;
    }
    
    public void setStarted(Date started) {
        this.started = started;
    }
    
    public Date getEnded() {
        return ended;
    }
    
    public void setEnded(Date ended) {
        this.ended = ended;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public String getProcessDefinitionDescription() {
        return processDefinitionDescription;
    }

    public void setProcessDefinitionDescription(String processDefinitionDescription) {
        this.processDefinitionDescription = processDefinitionDescription;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessDefinitionCategory() {
        return processDefinitionCategory;
    }

    public void setProcessDefinitionCategory(String processDefinitionCategory) {
        this.processDefinitionCategory = processDefinitionCategory;
    }

    public int getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(int processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }

    public String getProcessDefinitionDeploymentId() {
        return processDefinitionDeploymentId;
    }

    public void setProcessDefinitionDeploymentId(String processDefinitionDeploymentId) {
        this.processDefinitionDeploymentId = processDefinitionDeploymentId;
    }
    
    public List<RestVariable> getVariables() {
        return variables;
    }
  
    public void setVariables(List<RestVariable> variables) {
        this.variables = variables;
    }
  
    public void addVariable(RestVariable variable) {
        variables.add(variable);
    }
    
    public boolean isGraphicalNotationDefined() {
        return graphicalNotationDefined;
    }
    
    public void setGraphicalNotationDefined(boolean graphicalNotationDefined) {
        this.graphicalNotationDefined = graphicalNotationDefined;
    }

    public boolean isStartFormDefined() {
        return startFormDefined;
    }

    public void setStartFormDefined(boolean startFormDefined) {
        this.startFormDefined = startFormDefined;
    }
}
