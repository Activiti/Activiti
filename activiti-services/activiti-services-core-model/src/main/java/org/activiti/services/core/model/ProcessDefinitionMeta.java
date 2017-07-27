package org.activiti.services.core.model;

import java.util.HashSet;

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
    private HashSet<String> users;
    private HashSet<String> groups;
    @JsonDeserialize(using = ProcessDefinitionVariable.class)
    private HashSet<ProcessDefinitionVariable> variables;
    @JsonDeserialize(using = ProcessDefinitionUserTask.class)
    private HashSet<ProcessDefinitionUserTask> userTasks;
    @JsonDeserialize(using = ProcessDefinitionServiceTask.class)
    private HashSet<ProcessDefinitionServiceTask> serviceTasks;

    public ProcessDefinitionMeta() {
    };

    public ProcessDefinitionMeta(String id,
                                 String name,
                                 String description,
                                 int version,
                                 HashSet<String> users,
                                 HashSet<String> groups,
                                 HashSet<ProcessDefinitionVariable> variables,
                                 HashSet<ProcessDefinitionUserTask> userTasks,
                                 HashSet<ProcessDefinitionServiceTask> serviceTasks) {
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

    public HashSet<String> getUsers() {
        return users;
    }

    public HashSet<String> getGroups() {
        return groups;
    }

    public HashSet<ProcessDefinitionVariable> getVariables() {
        return variables;
    }

    public HashSet<ProcessDefinitionUserTask> getUserTasks() {
        return userTasks;
    }

    public HashSet<ProcessDefinitionServiceTask> getServiceTasks() {
        return serviceTasks;
    }

}
