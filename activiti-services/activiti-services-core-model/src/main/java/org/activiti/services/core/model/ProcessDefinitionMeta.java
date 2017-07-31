package org.activiti.services.core.model;

import java.util.HashSet;
import java.util.Set;

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
    private Set<String> users;
    private Set<String> groups;
    @JsonDeserialize(using = ProcessDefinitionVariable.class)
    private Set<ProcessDefinitionVariable> variables;
    @JsonDeserialize(using = ProcessDefinitionUserTask.class)
    private Set<ProcessDefinitionUserTask> userTasks;
    @JsonDeserialize(using = ProcessDefinitionServiceTask.class)
    private Set<ProcessDefinitionServiceTask> serviceTasks;

    public ProcessDefinitionMeta() {
    };

    public ProcessDefinitionMeta(String id,
                                 String name,
                                 String description,
                                 int version,
                                 Set<String> users,
                                 Set<String> groups,
                                 Set<ProcessDefinitionVariable> variables,
                                 Set<ProcessDefinitionUserTask> userTasks,
                                 Set<ProcessDefinitionServiceTask> serviceTasks) {
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

    public Set<String> getUsers() {
        return users;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public Set<ProcessDefinitionVariable> getVariables() {
        return variables;
    }

    public Set<ProcessDefinitionUserTask> getUserTasks() {
        return userTasks;
    }

    public Set<ProcessDefinitionServiceTask> getServiceTasks() {
        return serviceTasks;
    }

}
