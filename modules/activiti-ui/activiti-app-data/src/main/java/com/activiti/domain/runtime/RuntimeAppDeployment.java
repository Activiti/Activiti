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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.activiti.domain.common.IdBlockSize;
import com.activiti.domain.idm.User;

/**
 * A definition of an app deployed in the activiti engine, based on an app definition model,
 * created in the editor.
 * 
 * @author Tijs Rademakers
 */
@Entity
@Table(name="RUNTIME_APP_DEPLOYMENT")
public class RuntimeAppDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "runtimeAppDeploymentIdGenerator")
    @TableGenerator(name = "runtimeAppDeploymentIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name="id")
    protected Long id;
    
    @ManyToOne
    @JoinColumn(name="app_id")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    protected RuntimeAppDefinition appDefinition;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created")
    protected Date created;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="created_by")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    protected User createdBy;
    
    @Column(name="deployment_id")
    protected String deploymentId;
    
    @Column(name="app_definition")
    protected String definition;
    
    @Column(name="model_id")
    protected Long modelId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RuntimeAppDefinition getAppDefinition() {
        return appDefinition;
    }

    public void setAppDefinition(RuntimeAppDefinition appDefinition) {
        this.appDefinition = appDefinition;
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

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
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
}
