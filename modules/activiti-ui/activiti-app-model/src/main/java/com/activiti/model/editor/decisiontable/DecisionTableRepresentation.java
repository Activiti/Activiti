/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.model.editor.decisiontable;

import java.util.Date;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.model.common.AbstractRepresentation;

/**
 * Created by yvoswillens on 14/08/15.
 */
public class DecisionTableRepresentation extends AbstractRepresentation {

    protected Long id;
    protected String name;
    protected String description;
    protected Integer version;
    protected Long lastUpdatedBy;
	protected String lastUpdatedByFullName;
	protected Date lastUpdated;
	protected Long referenceId;
    protected DecisionTableDefinitionRepresentation decisionTableDefinition;

    public DecisionTableRepresentation(AbstractModel model) {
        this.id = model.getId();
        this.name = model.getName();
        this.description = model.getDescription();
        this.version = model.getVersion();
        this.lastUpdated = model.getLastUpdated();
        this.lastUpdatedBy = model.getLastUpdatedBy().getId();
        this.referenceId = model.getReferenceId();
        this.lastUpdatedByFullName = model.getLastUpdatedBy().getFullName();
        this.referenceId = model.getReferenceId();
    }

    public DecisionTableRepresentation() {}

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(Long lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getLastUpdatedByFullName() {
		return lastUpdatedByFullName;
	}

	public void setLastUpdatedByFullName(String lastUpdatedByFullName) {
		this.lastUpdatedByFullName = lastUpdatedByFullName;
	}

    public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public DecisionTableDefinitionRepresentation getDecisionTableDefinition() {
        return decisionTableDefinition;
    }

    public void setDecisionTableDefinition(DecisionTableDefinitionRepresentation decisionTableDefinition) {
        this.decisionTableDefinition = decisionTableDefinition;
    }
}
