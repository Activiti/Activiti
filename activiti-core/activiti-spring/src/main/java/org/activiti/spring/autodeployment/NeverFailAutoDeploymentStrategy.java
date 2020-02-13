package org.activiti.spring.autodeployment;

import org.activiti.core.common.spring.project.ApplicationUpgradeContextService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class NeverFailAutoDeploymentStrategy extends AbstractAutoDeploymentStrategy {

    protected static final Logger LOGGER = LoggerFactory.getLogger(NeverFailAutoDeploymentStrategy.class);

    public static final String DEPLOYMENT_MODE = "never-fail";

    public NeverFailAutoDeploymentStrategy(ApplicationUpgradeContextService applicationUpgradeContextService) {
        super(applicationUpgradeContextService);
    }

    @Override
    protected String getDeploymentMode() {
        return DEPLOYMENT_MODE;
    }

    @Override
    public void deployResources(String deploymentNameHint, Resource[] resources, RepositoryService repositoryService) {
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().enableDuplicateFiltering()
                .name(deploymentNameHint);

        int validProcessCount = 0;
        for (final Resource resource : resources) {
            final String resourceName = determineResourceName(resource);

            if (validateModel(resource, repositoryService)) {
                validProcessCount++;
                deploymentBuilder.addInputStream(resourceName, resource);
            } else {
                LOGGER.error("The following resource wasn't included in the deployment since it is invalid:\n{}",
                              resourceName);
            }
        }

        deploymentBuilder = loadApplicationUpgradeContext(deploymentBuilder);

        if (validProcessCount != 0) {
            deploymentBuilder.deploy();
        }
    }
}
