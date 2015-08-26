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
package com.activiti.model.runtime;

import com.activiti.model.common.AbstractRepresentation;
import com.activiti.model.idm.LightUserRepresentation;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    protected LightUserRepresentation startedBy;
    protected String processDefinitionName;
    protected String processDefinitionDescription;
    protected String processDefinitionKey;
    protected String processDefinitionCategory;
    protected int processDefinitionVersion;
    protected String processDefinitionDeploymentId;
    protected boolean graphicalNotationDefined;
    protected boolean startFormDefined;
    
    protected List<RestVariable> variables = new ArrayList<RestVariable>();

    public ProcessInstanceRepresentation(ProcessInstance processInstance, ProcessDefinition processDefinition, boolean graphicalNotation, LightUserRepresentation startedBy) {
        this(processInstance, graphicalNotation, startedBy);
        mapProcessDefinition(processDefinition);
    }

    public ProcessInstanceRepresentation(ProcessInstance processInstance, boolean graphicalNotation, LightUserRepresentation startedBy) {
        this.id = processInstance.getId();
        this.name = processInstance.getName();
        this.businessKey = processInstance.getBusinessKey();
        this.processDefinitionId = processInstance.getProcessDefinitionId();
        this.tenantId = processInstance.getTenantId();
        this.graphicalNotationDefined = graphicalNotation;
        this.startedBy = startedBy;
    }

    public ProcessInstanceRepresentation(HistoricProcessInstance processInstance, ProcessDefinition processDefinition, boolean graphicalNotation, LightUserRepresentation startedBy) {
        this(processInstance, graphicalNotation, startedBy);
        mapProcessDefinition(processDefinition);
    }
    
    public ProcessInstanceRepresentation(HistoricProcessInstance processInstance, boolean graphicalNotation, LightUserRepresentation startedBy) {
        this.id = processInstance.getId();
        this.name = processInstance.getName();
        this.businessKey = processInstance.getBusinessKey();
        this.processDefinitionId = processInstance.getProcessDefinitionId();
        this.tenantId = processInstance.getTenantId();
        this.graphicalNotationDefined = graphicalNotation;
        this.started = processInstance.getStartTime();
        this.ended = processInstance.getEndTime();
        this.startedBy = startedBy;
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

    public LightUserRepresentation getStartedBy() {
        return startedBy;
    }
    
    public String getBusinessKey() {
        return businessKey;
    }

    public void setStartedBy(LightUserRepresentation startedBy) {
        this.startedBy = startedBy;
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
