package org.activiti.bpmn.model;

public class Error {

    private String id;
    private String name;
    private String errorCode;

    public Error(String id,
                 String name,
                 String errorCode) {
        this.id = id;
        this.name = name;
        this.errorCode = errorCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
