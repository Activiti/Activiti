package org.activiti.api.runtime.shared.query;

public class Filter {
    private final String property;
    private final String value;

    public Filter(String property, String value) {
        this.property = property;
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }

    public static Filter by(String property, String value) {
        return new Filter(property, value);
    }
}
