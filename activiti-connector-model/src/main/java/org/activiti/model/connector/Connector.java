package org.activiti.model.connector;

import java.util.Map;

public class Connector {

    private String id;

    private String name;

    private String description;

    private String icon;

    private Map<String, Action> actions;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public Map<String, Action> getActions() {
        return actions;
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

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setActions(Map<String, Action> actions) {
        this.actions = actions;
    }
}
