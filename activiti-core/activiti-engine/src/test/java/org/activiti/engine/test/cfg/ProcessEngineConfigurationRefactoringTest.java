/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.engine.test.cfg;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.configurators.BpmnConfigurator;
import org.activiti.engine.impl.cfg.configurators.CommandConfigurator;
import org.activiti.engine.impl.cfg.configurators.CoreConfigurator;
import org.activiti.engine.impl.cfg.configurators.DatabaseConfigurator;
import org.activiti.engine.impl.cfg.configurators.EventConfigurator;
import org.activiti.engine.impl.cfg.configurators.JobConfigurator;
import org.activiti.engine.impl.cfg.configurators.ServiceConfigurator;
import org.activiti.engine.impl.cfg.configurators.SessionConfigurator;
import org.junit.jupiter.api.Test;

/**
 * Test to verify that the ProcessEngineConfiguration refactoring maintains backward compatibility
 * and that the new configurators are properly initialized.
 */
public class ProcessEngineConfigurationRefactoringTest {

    @Test
    public void testConfiguratorsAreInitialized() {
        StandaloneProcessEngineConfiguration configuration = new StandaloneProcessEngineConfiguration();
        configuration.setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000");
        configuration.setJdbcUsername("sa");
        configuration.setJdbcPassword("");
        configuration.setJdbcDriver("org.h2.Driver");
        configuration.setDatabaseSchemaUpdate("create-drop");
        
        // Initialize the configuration (which calls initInternalConfigurators)
        configuration.init();
        
        // Verify that all configurators are properly initialized
        assertNotNull(configuration.getCoreConfigurator(), "CoreConfigurator should be initialized");
        assertNotNull(configuration.getDatabaseConfigurator(), "DatabaseConfigurator should be initialized");
        assertNotNull(configuration.getCommandConfigurator(), "CommandConfigurator should be initialized");
        assertNotNull(configuration.getServiceConfigurator(), "ServiceConfigurator should be initialized");
        assertNotNull(configuration.getBpmnConfigurator(), "BpmnConfigurator should be initialized");
        assertNotNull(configuration.getJobConfigurator(), "JobConfigurator should be initialized");
        assertNotNull(configuration.getSessionConfigurator(), "SessionConfigurator should be initialized");
        assertNotNull(configuration.getEventConfigurator(), "EventConfigurator should be initialized");
    }
    
    @Test
    public void testCustomConfiguratorCanBeSet() {
        StandaloneProcessEngineConfiguration configuration = new StandaloneProcessEngineConfiguration();
        
        // Create custom configurators
        CoreConfigurator customCoreConfigurator = new CoreConfigurator(configuration);
        DatabaseConfigurator customDatabaseConfigurator = new DatabaseConfigurator(configuration);
        
        // Set custom configurators
        configuration.setCoreConfigurator(customCoreConfigurator);
        configuration.setDatabaseConfigurator(customDatabaseConfigurator);
        
        // Verify they are set correctly
        assertNotNull(configuration.getCoreConfigurator(), "Custom CoreConfigurator should be set");
        assertNotNull(configuration.getDatabaseConfigurator(), "Custom DatabaseConfigurator should be set");
    }
}