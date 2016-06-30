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
package com.activiti.domain;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "SERVER_CONFIG")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ServerConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "serverConfigIdGenerator")
    @TableGenerator(name = "serverConfigIdGenerator", table = "HIBERNATE_SEQUENCES")
	protected Long id;

	@Column
	@NotNull
	protected String name;

	@Column
	protected String description;

	@Column(name="server_address")
	@NotNull
	protected String serverAddress;

	@Column
	@NotNull
	protected Integer port;

	@Column(name="context_root")
	protected String contextRoot;

	@Column(name="rest_root")
	protected String restRoot;

	@Column(name="user_name")
	@NotNull
	protected String userName;

	@Column
	protected String password;

	@Transient
	@Column(name="cluster_config_id")
	protected Long clusterConfigId;

	@Column(name="tenant_id")
	protected String tenantId;

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

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getContextRoot() {
		return contextRoot;
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}

	public String getRestRoot() {
		return restRoot;
	}

	public void setRestRoot(String restRoot) {
		this.restRoot = restRoot;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerConfig config = (ServerConfig) o;

        if (!id.equals(config.id)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

	@Override
	public String toString() {
		return "ServerConfig [id=" + id + ", name=" + name + ", description="
				+ description + ", serverAddress=" + serverAddress + ", port="
				+ port + ", contextRoot=" + contextRoot + ", restRoot="
				+ restRoot + ", userName=" + userName + ", password="
				+ password + "]";
	}
}
