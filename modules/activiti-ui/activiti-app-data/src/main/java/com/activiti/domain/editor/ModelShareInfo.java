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
