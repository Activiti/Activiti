package org.activiti.model.connector;

public class Variable {

    private String id;

    private String name;

    private String description;

    private String type;

    private boolean required;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }
}
