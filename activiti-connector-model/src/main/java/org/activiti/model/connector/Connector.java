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
}
