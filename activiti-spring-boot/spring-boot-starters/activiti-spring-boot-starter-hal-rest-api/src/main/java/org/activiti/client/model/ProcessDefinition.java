/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 *
 */

package org.activiti.client.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessDefinition {

    private String id;
    private String name;
    private String description;
    private int version;
    private List<String> users;
    private List<String> groups;
    private List<String> variables;
    private List<String> userTasks;
    private List<String> serviceTasks;

    public ProcessDefinition() {
    }
    
    public ProcessDefinition(String id,
					         String name,
					         String description,
					         int version) {
    	this.id = id;
    	this.name = name;
    	this.version = version;
    	this.description = description;
	}

    public ProcessDefinition(String id,
                             String name,
                             String description,
                             int version,
                             List<String> users,
                             List<String> groups,
                             List<String> variables,
                             List<String> userTasks,
                             List<String> serviceTasks) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.users = users;
        this.groups = groups;
        this.variables = variables;
        this.userTasks = userTasks;
        this.serviceTasks = serviceTasks;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getVersion() {
        return version;
    }

	public List<String> getUsers() {
		return users;
	}

	public List<String> getGroups() {
		return groups;
	}

	public List<String> getVariables() {
		return variables;
	}

	public List<String> getUserTasks() {
		return userTasks;
	}

	public List<String> getServiceTasks() {
		return serviceTasks;
	}
    
    
}
