package org.activiti.client.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessDefinitionMeta {
	
	private String processDefinitionId;
    private String name;
    private String description;
    private int version;
    private List<String> users;
    private List<String> groups;
    private List<String []> variables;
    private List<String []> userTasks;
    private List<String []> serviceTasks;
    
    public ProcessDefinitionMeta() {};
    
	public ProcessDefinitionMeta(String id, String name, String description, int version, List<String> users,
			List<String> groups, List<String[]> variables, List<String[]> userTasks, List<String[]> serviceTasks) {
		super();
		this.processDefinitionId = id;
		this.name = name;
		this.description = description;
		this.version = version;
		this.users = users;
		this.groups = groups;
		this.variables = variables;
		this.userTasks = userTasks;
		this.serviceTasks = serviceTasks;
	}

	public String getId() {
		return processDefinitionId;
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

	public List<String[]> getVariables() {
		return variables;
	}

	public List<String[]> getUserTasks() {
		return userTasks;
	}

	public List<String[]> getServiceTasks() {
		return serviceTasks;
	}

}
