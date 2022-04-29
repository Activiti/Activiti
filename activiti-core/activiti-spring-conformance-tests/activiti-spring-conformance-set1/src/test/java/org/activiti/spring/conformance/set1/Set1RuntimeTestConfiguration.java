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
package org.activiti.spring.conformance.set1;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.spring.conformance.util.RuntimeTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Set1RuntimeTestConfiguration {

    private static boolean connector1Executed = false;

    private static boolean connector2Executed = false;

    private static IntegrationContext resultIntegrationContext = null;

    @Bean(name = "service-implementation")
    public Connector serviceImplementation() {
        return integrationContext -> {
            connector1Executed = true;

            resultIntegrationContext = integrationContext;

            return integrationContext;
        };
    }

    @Bean(name = "service-implementation-modify-data")
    public Connector serviceImplementationModifyData() {
        return integrationContext -> {
            connector2Executed = true;
            integrationContext.getOutBoundVariables().put("var1", integrationContext.getInBoundVariables().get("var1") + "-modified");
            return integrationContext;
        };
    }


    public static IntegrationContext getResultIntegrationContext() {
        return resultIntegrationContext;
    }


    public static void reset() {
        RuntimeTestConfiguration.collectedEvents.clear();

        resultIntegrationContext = null;
        connector1Executed = false;
        connector2Executed = false;
    }


    public static boolean isConnector1Executed() {
        return connector1Executed;
    }


    public static boolean isConnector2Executed() {
        return connector2Executed;
    }

}
