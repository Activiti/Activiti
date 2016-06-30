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
package com.activiti.web.rest.dto;

import com.activiti.domain.ServerConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * @author Tijs Rademakers
 */
public class ServerConfigRepresentation {
	
	protected Long id;
	protected String name;
	protected String description;
	protected String serverAddress;
	protected Integer serverPort;
	protected String contextRoot;
	protected String restRoot;
	protected String userName;
	protected String password;
	
	public ServerConfigRepresentation() {
	}
	
	public ServerConfigRepresentation(ServerConfig serverConfig) {
	    this.id = serverConfig.getId();
	    this.name = serverConfig.getName();
	    this.description = serverConfig.getDescription();
	    this.serverAddress = serverConfig.getServerAddress();
	    this.serverPort = serverConfig.getPort();
	    this.contextRoot = serverConfig.getContextRoot();
	    this.restRoot = serverConfig.getRestRoot();
	    this.userName = serverConfig.getUserName();
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
	public Integer getServerPort() {
		return serverPort;
	}
	public void setServerPort(Integer serverPort) {
		this.serverPort = serverPort;
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
	
	@JsonInclude(Include.NON_NULL)
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
