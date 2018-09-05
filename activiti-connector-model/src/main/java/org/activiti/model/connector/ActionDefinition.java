package org.activiti.model.connector;

import java.util.List;

public class ActionDefinition {

    private String id;

    private String name;

    private String description;

    private List<VariableDefinition> input;

    private List<VariableDefinition> output;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<VariableDefinition> getInput() {
        return input;
    }

    public List<VariableDefinition> getOutput() {
        return output;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInput(List<VariableDefinition> input) {
        this.input = input;
    }

    public void setOutput(List<VariableDefinition> output) {
        this.output = output;
    }
}
