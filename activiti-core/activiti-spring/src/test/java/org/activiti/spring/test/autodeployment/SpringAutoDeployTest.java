/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.spring.test.autodeployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.test.AbstractTestCase;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringAutoDeployTest extends AbstractTestCase {

    protected static final String CTX_PATH = "org/activiti/spring/test/autodeployment/SpringAutoDeployTest-context.xml";
    protected static final String CTX_NO_DROP_PATH = "org/activiti/spring/test/autodeployment/SpringAutoDeployTest-no-drop-context.xml";
    protected static final String CTX_CREATE_DROP_CLEAN_DB = "org/activiti/spring/test/autodeployment/SpringAutoDeployTest-create-drop-clean-db-context.xml";
    protected static final String CTX_DEPLOYMENT_MODE_DEFAULT = "org/activiti/spring/test/autodeployment/SpringAutoDeployTest-deploymentmode-default-context.xml";
    protected static final String CTX_DEPLOYMENT_MODE_SINGLE_RESOURCE = "org/activiti/spring/test/autodeployment/SpringAutoDeployTest-deploymentmode-single-resource-context.xml";
    protected static final String CTX_DEPLOYMENT_MODE_RESOURCE_PARENT_FOLDER = "org/activiti/spring/test/autodeployment/SpringAutoDeployTest-deploymentmode-resource-parent-folder-context.xml";

    protected ApplicationContext applicationContext;
    protected RepositoryService repositoryService;

    protected void createAppContext(String path) {
        this.applicationContext = new ClassPathXmlApplicationContext(path);
        this.repositoryService = applicationContext.getBean(RepositoryService.class);
    }

    protected void tearDown() throws Exception {
        removeAllDeployments();
        this.applicationContext = null;
        this.repositoryService = null;
        super.tearDown();
    }

    public void testBasicActivitiSpringIntegration() {
        createAppContext("org/activiti/spring/test/autodeployment/SpringAutoDeployTest-context.xml");
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

        Set<String> processDefinitionKeys = new HashSet<String>();
        for (ProcessDefinition processDefinition : processDefinitions) {
            processDefinitionKeys.add(processDefinition.getKey());
        }

        Set<String> expectedProcessDefinitionKeys = new HashSet<String>();
        expectedProcessDefinitionKeys.add("a");
        expectedProcessDefinitionKeys.add("b");
        expectedProcessDefinitionKeys.add("c");

        assertThat(processDefinitionKeys).isEqualTo(expectedProcessDefinitionKeys);
    }

    public void testNoRedeploymentForSpringContainerRestart() throws Exception {
        createAppContext(CTX_PATH);
        DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
        assertThat(deploymentQuery.count()).isEqualTo(1);
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        assertThat(processDefinitionQuery.count()).isEqualTo(3);

        // Creating a new app context with same resources doesn't lead to more
        // deployments
        new ClassPathXmlApplicationContext(CTX_NO_DROP_PATH);
        assertThat(deploymentQuery.count()).isEqualTo(1);
        assertThat(processDefinitionQuery.count()).isEqualTo(3);
    }

    // Updating the bpmn20 file should lead to a new deployment when restarting
    // the Spring container
    public void testResourceRedeploymentAfterProcessDefinitionChange() throws Exception {
        createAppContext(CTX_PATH);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1);
        ((AbstractXmlApplicationContext) applicationContext).close();

        String filePath = "org/activiti/spring/test/autodeployment/autodeploy.a.bpmn20.xml";
        String originalBpmnFileContent = IoUtil.readFileAsString(filePath);
        String updatedBpmnFileContent = originalBpmnFileContent.replace("flow1",
                                                                        "fromStartToEndFlow");
        assertThat(updatedBpmnFileContent.length() > originalBpmnFileContent.length()).isTrue();
        IoUtil.writeStringToFile(updatedBpmnFileContent, filePath);

        // Classic produced/consumer problem here:
        // The file is already written in Java, but not yet completely persisted
        // by
        // the OS
        // Constructing the new app context reads the same file which is
        // sometimes
        // not yet fully written to disk
        waitUntilFileIsWritten(filePath,
                               updatedBpmnFileContent.length());

        try {
            applicationContext = new ClassPathXmlApplicationContext(CTX_NO_DROP_PATH);
            repositoryService = (RepositoryService) applicationContext.getBean("repositoryService");
        } finally {
            // Reset file content such that future test are not seeing something
            // funny
            IoUtil.writeStringToFile(originalBpmnFileContent,
                                     filePath);
        }

        // Assertions come AFTER the file write! Otherwise the process file is
        // messed up if the assertions fail.
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(2);
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(6);
    }

    public void testAutoDeployWithCreateDropOnCleanDb() {
        createAppContext(CTX_CREATE_DROP_CLEAN_DB);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1);
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(3);
    }

    public void testAutoDeployWithDeploymentModeDefault() {
        createAppContext(CTX_DEPLOYMENT_MODE_DEFAULT);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1);
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(3);
    }

    public void testAutoDeployWithDeploymentModeSingleResource() {
        createAppContext(CTX_DEPLOYMENT_MODE_SINGLE_RESOURCE);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(3);
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(3);
    }

    public void testAutoDeployWithDeploymentModeResourceParentFolder() {
        createAppContext(CTX_DEPLOYMENT_MODE_RESOURCE_PARENT_FOLDER);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(2);
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(4);
    }

    // --Helper methods
    // ----------------------------------------------------------

    private void removeAllDeployments() {
        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    private boolean waitUntilFileIsWritten(String filePath,
                                           int expectedBytes) throws Exception {
        while (IoUtil.getFile(filePath).length() != (long) expectedBytes) {
            wait(100L);
        }
        return true;
    }
}
