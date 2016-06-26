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
package com.activiti.domain.idm;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.activiti.domain.common.IdBlockSize;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A user.
 */
@Entity
@Table(name = "USERS")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "userIdGenerator")
	@TableGenerator(name = "userIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name="id")
	private Long id;

    @JsonIgnore
    @Size(min = 0, max = 100)
    @Column(name="pass_word")
    private String password;

    @Size(min = 0, max = 50)
    @Column(name = "first_name")
    private String firstName;

    @Size(min = 0, max = 50)
    @Column(name = "last_name")
    private String lastName;

    @Column(name="email", nullable=true)
    private String email;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="last_update")
    private Date lastUpdate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created")
    private Date created;
    
    @Size(min = 0, max = 100)
    @Column(name = "company")
    private String company;

    @ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(
			name = "USER_GROUP",
	        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
	        inverseJoinColumns = {@JoinColumn(name = "group_id", referencedColumnName = "id")})
	protected List<Group> groups;
    
    @Column(name="manager_user_id")
    private Long managerUserId;
    
    @Column(name="picture_image_id")
    private Long pictureImageId;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
    	// We store email always lowercased
    	if (email != null) {
    		this.email = email.toLowerCase();
    	} else {
    		this.email = null;
    	}
    }

    public Date getCreated() {
        return created;
    }
    
    public void setCreated(Date created) {
        this.created = created;
    }
    
    public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public void setCompany(String company) {
        this.company = company;
    }
    
    public String getCompany() {
        return company;
    }
    
    public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public Long getManagerUserId() {
		return managerUserId;
	}

	public void setManagerUserId(Long managerUserId) {
		this.managerUserId = managerUserId;
	}

	public Long getPictureImageId() {
		return pictureImageId;
	}

	public void setPictureImageId(Long pictureImageId) {
		this.pictureImageId = pictureImageId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        if (!id.equals(user.getId())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        if(id != null) {
            return super.hashCode();
        }
        return id.hashCode();
    }

    public String getFullName() {
        return StringUtils.join(new String[] {firstName, lastName}, ' ');
    }
}
