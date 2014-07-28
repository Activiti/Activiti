/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.spring.boot;


import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author Josh Long
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
public class ProcessEngineAutoConfigurationTest {


    /**
     * Activiti needs a {@link javax.sql.DataSource} to work, so this will look for a
     * DS <EM>after</EM> the {@link org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration}
     * auto-configuration is run. It discovers process definitions
     * in the {@literal src/main/resources/processes/*bpmn.xml} directory.
     */
    @Configuration
    @EnableAutoConfiguration
    public static class ProcessEngineConfiguration {
    }

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void testProcessDefinitionDeployment() {

        List<ProcessDefinition> processDefinitionList =
                this.repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey("waiter")
                        .list();

        Assert.assertNotNull(processDefinitionList);
        Assert.assertTrue(processDefinitionList.size() > 0);

        ProcessDefinition processDefinition =
                processDefinitionList.iterator().next();
        Assert.assertEquals(processDefinition.getKey(), "waiter");

    }

    @Autowired
    RuntimeService runtimeService;

    @Test
    public void testProcessEngineCreated() throws Throwable {
        Assert.assertNotNull(this.processEngine);
        Assert.assertNotNull(this.repositoryService);
        Assert.assertNotNull(this.runtimeService);
    }

}
