package org.activiti.api.runtime.model.impl;

import org.activiti.api.process.model.Deployment;

public class DeploymentImpl implements Deployment {

    String id;
    String name;
    Integer version;
    String projectReleaseVersion;

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setProjectReleaseVersion(String projectReleaseVersion) {
        this.projectReleaseVersion = projectReleaseVersion;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public String getProjectReleaseVersion() {
        return projectReleaseVersion;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
