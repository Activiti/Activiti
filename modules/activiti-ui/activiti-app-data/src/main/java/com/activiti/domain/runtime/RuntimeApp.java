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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.activiti.domain.common.IdBlockSize;
import com.activiti.domain.idm.User;

/**
 * An entity connecting a {@link User} with a {@link RuntimeAppDefinition}. Indicates
 * a user sees the deployed app on the landing page.
 * 
 * @author Frederik Heremans
 */
@Entity
@Table(name="RUNTIME_APP")
public class RuntimeApp {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "runtimeAppIdGenerator")
    @TableGenerator(name = "runtimeAppIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name="id")
    private Long id;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="app_definition")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    protected RuntimeAppDefinition appDefinition;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="app_user")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    protected User user;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
}
