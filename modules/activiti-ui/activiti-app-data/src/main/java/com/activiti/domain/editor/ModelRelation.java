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
package com.activiti.domain.editor;

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

@Entity
@Table(name="MODEL_RELATION")
public class ModelRelation {
	
	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modelRelationIdGenerator")
    @TableGenerator(name = "modelRelationIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name="id")
    private Long id;

    @Column(name="parent_model_id")
    private Long parentModelId;
    
    // Only needed for HQL queries. Not using it!
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="parent_model_id", insertable=false, updatable=false)
    private Model parentModel;
    
    @Column(name="model_id")
    private Long modelId;
    
    // Only needed for HQL queries. Not using it!
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="model_id", insertable=false, updatable=false)
    private Model model;
    
    @Column(name="relation_type")
    private String type;
    
    public ModelRelation() {
    	
    }
    
    public ModelRelation(Long parentModelId, Long modelId, String type) {
    	this.parentModelId = parentModelId;
    	this.modelId = modelId;
    	this.type = type;
    }

	public Long getParentModelId() {
		return parentModelId;
	}

	public void setParentModelId(Long parentModelId) {
		this.parentModelId = parentModelId;
	}

	public Model getParentModel() {
		return parentModel;
	}

	public void setParentModel(Model parentModel) {
		this.parentModel = parentModel;
	}

	public Long getModelId() {
		return modelId;
	}

	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
