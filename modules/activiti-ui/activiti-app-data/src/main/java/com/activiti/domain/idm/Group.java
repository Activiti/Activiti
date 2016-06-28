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

import com.activiti.domain.common.IdBlockSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A tenant.
 * 
 * @author Joram Barrez
 */
@Entity
@Table(name = "GROUPS")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Group implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "groupIdGenerator")
	@TableGenerator(name = "groupIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name="id")
    protected Long id;
	
	@Column(name="name", nullable=false)
	protected String name;

	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(
			name = "USER_GROUP",
	        joinColumns = {@JoinColumn(name = "group_id", referencedColumnName = "id")},
	        inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	protected Set<User> users;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="last_update")
	private Date lastUpdate;

	public Group() {
		
	}
	
	public Group(String name) {
		this.name = name;
	}
	
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

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

}
