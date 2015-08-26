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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.activiti.domain.common.IdBlockSize;
import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;

/**
 * Entity representing a process that is shared with a single user.
 */
@Entity
@Table(name = "MODEL_SHARE_INFO")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ModelShareInfo implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modelShareInfoIdGenerator")
    @TableGenerator(name = "modelShareInfoIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name="id")
    private Long id;

    @ManyToOne(optional=true)
    @JoinColumn(name="user_id")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private User user;
    
    @ManyToOne(optional=true)
    @JoinColumn(name="group_id")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Group group;
    
    @Column(name="email")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private String email;
    
    @ManyToOne
    @JoinColumn(name="shared_by")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private User sharedBy;

    @ManyToOne
    @JoinColumn(name="model_id")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Model model;
    
    @Column(name="share_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date shareDate;
    
    @Column(name="permission")
    @Enumerated(EnumType.ORDINAL)
    private SharePermission permission = SharePermission.READ;

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Model getModel() {
        return model;
    }
    
    public void setModel(Model model) {
        this.model = model;
    }
    
    public Date getShareDate() {
        return shareDate;
    }
    
    public void setShareDate(Date shareDate) {
        this.shareDate = shareDate;
    }
    
    public SharePermission getPermission() {
        return permission;
    }
    
    public void setPermission(SharePermission permission) {
        this.permission = permission;
    }
    
    public User getSharedBy() {
        return sharedBy;
    }
    
    public void setSharedBy(User sharedBy) {
        this.sharedBy = sharedBy;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModelShareInfo user = (ModelShareInfo) o;
        return new EqualsBuilder().append(id, user.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        if(id != null) {
            return id.hashCode();
        }
        return super.hashCode();
    }
}
