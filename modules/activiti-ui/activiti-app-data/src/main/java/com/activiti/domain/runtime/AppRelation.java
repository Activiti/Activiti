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

import com.activiti.domain.common.IdBlockSize;
import com.activiti.domain.editor.ModelHistory;

@Entity
@Table(name="APP_RELATION")
public class AppRelation {
	
	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "appRelationIdGenerator")
    @TableGenerator(name = "appRelationIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name="id")
    private Long id;

    @Column(name="runtime_app_id")
    private Long runtimeAppId;
    
    // Only needed for HQL queries. Not using it!
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="runtime_app_id", insertable=false, updatable=false)
    private RuntimeAppDeployment runtimeAppDeployment;
    
    @Column(name="model_id")
    private Long modelId;
    
    // Only needed for HQL queries. Not using it!
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="model_id", insertable=false, updatable=false)
    private ModelHistory modelHistory;
    
    @Column(name="relation_type")
    private String type;
    
    @Column(name="metadata")
    private String metaData;
    
    public AppRelation() {
    	
    }
    
    public AppRelation(Long runtimeAppId, Long modelId, String type) {
    	this.runtimeAppId = runtimeAppId;
    	this.modelId = modelId;
    	this.type = type;
    }
    
    public AppRelation(Long runtimeAppId, Long modelId, String type, String metaData) {
    	this(runtimeAppId, modelId, type);
    	this.metaData = metaData;
    }
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRuntimeAppId() {
		return runtimeAppId;
	}

	public void setRuntimeAppId(Long runtimeAppId) {
		this.runtimeAppId = runtimeAppId;
	}
	
	public RuntimeAppDeployment getRuntimeAppDeployment() {
		return runtimeAppDeployment;
	}

	public void setRuntimeAppDeployment(RuntimeAppDeployment runtimeAppDeployment) {
		this.runtimeAppDeployment = runtimeAppDeployment;
	}

	public Long getModelId() {
		return modelId;
	}

	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}
	
	public ModelHistory getModelHistory() {
		return modelHistory;
	}

	public void setModelHistory(ModelHistory modelHistory) {
		this.modelHistory = modelHistory;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMetaData() {
		return metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

}
