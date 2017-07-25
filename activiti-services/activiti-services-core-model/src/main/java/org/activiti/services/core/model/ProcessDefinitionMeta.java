package org.activiti.services.core.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessDefinitionMeta {

    private String id;
    private String name;
    private String description;
    private int version;
    private List<String> users;
    private List<String> groups;
    @JsonDeserialize(using = ProcessDefinitionVariable.class)
    private List<ProcessDefinitionVariable> variables;
    @JsonDeserialize(using = ProcessDefinitionUserTask.class)
    private List<ProcessDefinitionUserTask> userTasks;
    @JsonDeserialize(using = ProcessDefinitionServiceTask.class)
    private List<ProcessDefinitionServiceTask> serviceTasks;

    public ProcessDefinitionMeta() {
    };

    public ProcessDefinitionMeta(String id, String name, String description, int version, List<String> users, List<String> groups, List<ProcessDefinitionVariable> variables, List<ProcessDefinitionUserTask> userTasks, List<ProcessDefinitionServiceTask> serviceTasks) {
        super();
        this.id = id;
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

    public List<ProcessDefinitionVariable> getVariables() {
        return variables;
    }

    public List<ProcessDefinitionUserTask> getUserTasks() {
        return userTasks;
    }

    public List<ProcessDefinitionServiceTask> getServiceTasks() {
        return serviceTasks;
    }

}
