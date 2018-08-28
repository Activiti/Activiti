package org.activiti.model.connector;

import java.util.List;

public class Action {

    private String id;

    private String name;

    private String description;

    private List<Variable> input;

    private List<Variable> output;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Variable> getInput() {
        return input;
    }

    public List<Variable> getOutput() {
        return output;
    }
}
