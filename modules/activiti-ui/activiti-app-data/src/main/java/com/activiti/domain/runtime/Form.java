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
package com.activiti.domain.runtime;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.activiti.domain.common.IdBlockSize;
import com.activiti.domain.idm.User;

/**
 * @author Joram Barrez
 */
@Entity
@Table(name = "FORM")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Form {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "formIdGenerator")
	@TableGenerator(name = "formIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name="id")
	private Long id;
	
	@Column(name="name", nullable=false)
	private String name;
	
	@Column(name="description")
	private String description;
	
	@Column(name="created", nullable=false)
	private Date created;
	
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="created_by")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	private User createdBy;
	
	@Column(name="form_definition")
	private String definition;
	
	@Column(name="model_id")
	private Long modelId;
	
	@Column(name="app_definition_id")
    private Long appDefinitionId;
	
	@Column(name="app_deployment_id")
    private Long appDeploymentId;

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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public Long getAppDefinitionId() {
        return appDefinitionId;
    }

    public void setAppDefinitionId(Long appDefinitionId) {
        this.appDefinitionId = appDefinitionId;
    }

    public Long getAppDeploymentId() {
        return appDeploymentId;
    }

    public void setAppDeploymentId(Long appDeploymentId) {
        this.appDeploymentId = appDeploymentId;
    }
}
