package org.activiti.runtime.api.model.impl;

import org.activiti.api.process.model.Deployment;
import org.activiti.api.runtime.model.impl.DeploymentImpl;

public class APIDeploymentConverter extends ListConverter<org.activiti.engine.repository.Deployment, Deployment>
        implements ModelConverter<org.activiti.engine.repository.Deployment, Deployment>{

    @Override
    public Deployment from(org.activiti.engine.repository.Deployment internalDeployment){
        DeploymentImpl deployment = new DeploymentImpl();

        deployment.setId(internalDeployment.getId());
        deployment.setName(internalDeployment.getName());
        deployment.setVersion(internalDeployment.getVersion());
        deployment.setProjectReleaseVersion(internalDeployment.getProjectReleaseVersion());

        return deployment;
    }


}
