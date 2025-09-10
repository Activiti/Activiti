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

package org.activiti.engine.impl.cfg.configuration;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.standalone.StandaloneProcessEngineConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify that configuration classes are properly initialized
 * and can be customized.
 */
public class ConfigurationTest {

    @Test
    public void testConfigurationClassesAreInitialized() {
        ProcessEngineConfigurationImpl config = new StandaloneProcessEngineConfiguration();
        
        // Initialize internal configurations
        config.init();
        
        // Verify that configuration classes are properly initialized
        assertNotNull(config.getCoreConfiguration());
        assertNotNull(config.getJobConfiguration());
        
        // Verify that components are initialized
        assertNotNull(config.getClock());
        assertNotNull(config.getExpressionManager());
        assertNotNull(config.getEngineAgendaFactory());
        assertNotNull(config.getBusinessCalendarManager());
        assertNotNull(config.getJobHandlers());
        assertNotNull(config.getJobManager());
        assertNotNull(config.getAsyncExecutor());
    }

    @Test
    public void testConfigurationClassesCanBeCustomized() {
        ProcessEngineConfigurationImpl config = new StandaloneProcessEngineConfiguration();
        
        // Create custom configuration classes
        CoreConfiguration customCoreConfig = new CoreConfiguration(config);
        JobConfiguration customJobConfig = new JobConfiguration(config);
        
        // Set custom configurations
        config.setCoreConfiguration(customCoreConfig);
        config.setJobConfiguration(customJobConfig);
        
        // Verify they were set
        assertSame(customCoreConfig, config.getCoreConfiguration());
        assertSame(customJobConfig, config.getJobConfiguration());
    }
}