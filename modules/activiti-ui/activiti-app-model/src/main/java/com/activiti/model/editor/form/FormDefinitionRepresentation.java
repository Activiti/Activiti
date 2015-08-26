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
package com.activiti.model.editor.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author Joram Barrez
 */
@JsonInclude(Include.NON_NULL)
public class FormDefinitionRepresentation {
	
    private Long id;
    private String name;
    private String processDefinitionId;
    private String processDefinitionName;
    private String processDefinitionKey;
    private String taskId;
    private String taskName;
    private String taskDefinitionKey;
	private List<FormFieldRepresentation> fields;
	private String outcomeTarget;
	private List<FormOutcomeRepresentation> outcomes;
	private boolean gridsterForm = true;
	
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    /**
     * Do not use this method for logical operations since it only return the top level fields.
     * I.e. A "container" field's sub fields are not returned.
     * For verifying and listing all fields from a form use instead listAllFields().
     *
     * @return The top level fields, a container's sub fields are not returned.
     */
    public List<FormFieldRepresentation> getFields() {
		return fields;
	}

	public void setFields(List<FormFieldRepresentation> fields) {
		this.fields = fields;
	}
	
	public String getOutcomeTarget() {
		return outcomeTarget;
	}

	public void setOutcomeTarget(String outcomeTarget) {
		this.outcomeTarget = outcomeTarget;
	}

	public List<FormOutcomeRepresentation> getOutcomes() {
		return outcomes;
	}

	public void setOutcomes(List<FormOutcomeRepresentation> outcomes) {
		this.outcomes = outcomes;
	}

    public boolean isGridsterForm() {
        return gridsterForm;
    }

    public void setGridsterForm(boolean gridsterForm) {
        this.gridsterForm = gridsterForm;
    }

    /*
	 * Helper methods
	 */
	public Map<String, FormFieldRepresentation> allFieldsAsMap() {
		Map<String, FormFieldRepresentation> result = new HashMap<String, FormFieldRepresentation>();
        List<FormFieldRepresentation> allFields = listAllFields();
		if (allFields != null) {
			for (FormFieldRepresentation field : allFields) {
			    if (result.containsKey(field.getId()) == false || ("readonly".equals(field.getType()) == false && 
			            "readonly-text".equals(field.getType()) == false)) {
			        
			        result.put(field.getId(), field);
			    }
			}
		}
		return result;
	}

    public List<FormFieldRepresentation> listAllFields() {
        List<FormFieldRepresentation> listOfAllFields = new ArrayList<FormFieldRepresentation>();
        collectSubFields(fields, listOfAllFields);
        return listOfAllFields;
    }

    protected void collectSubFields(List<FormFieldRepresentation> fields, List<FormFieldRepresentation> listOfAllFields) {
        if (CollectionUtils.isNotEmpty(fields)) {
            for (FormFieldRepresentation field : fields) {
                listOfAllFields.add(field);
                if (field instanceof ContainerRepresentation) {
                    ContainerRepresentation container = (ContainerRepresentation) field;
                    Map<String, List<FormFieldRepresentation>> subFieldMap = container.getFields();
                    if (subFieldMap != null) {
                        for (List<FormFieldRepresentation> subFields : subFieldMap.values()) {
                            if (subFields != null) {
                                collectSubFields(subFields, listOfAllFields);
                            }
                        }
                    }
                }
            }
        }
    }
 	
}
