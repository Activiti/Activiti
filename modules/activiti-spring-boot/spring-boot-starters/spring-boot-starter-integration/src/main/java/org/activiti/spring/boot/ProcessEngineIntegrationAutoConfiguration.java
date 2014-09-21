/* * Copyright 2012-2014 the original author or authors. * * Licensed under the Apache License, Version 2.0 (the "License"); * you may not use this file except in compliance with the License. * You may obtain a copy of the License at * * http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */
package org.activiti.spring.boot;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration and starter for Spring Integration adapter * support for Activiti.
 * https://github.com/Activiti/Activiti/blob/master/modules/activiti-engine/src/main/java/org/activiti/engine/impl/bpmn/behavior/ReceiveTaskActivityBehavior.java
 * https://github.com/spring-projects/spring-integration-extensions/blob/master/spring-integration-java-dsl/src/test/java/org/springframework/integration/dsl/test/flows/IntegrationFlowTests.java#L1899
 * https://github.com/spring-projects/spring-integration-extensions/wiki/Spring-Integration-Java-DSL-Reference#adapters
 * https://github.com/joshlong/spring-integration-activiti/blob/master/src/main/java/org/springframework/integration/activiti/gateway/AbstractActivityBehaviorMessagingGateway.java
 * https://github.com/joshlong/spring-integration-activiti/tree/master/src/main/java/org/springframework/integration/activiti/gateway
 */
@Configuration
@AutoConfigureAfter(BasicProcessEngineAutoConfiguration.class)
public class ProcessEngineIntegrationAutoConfiguration {
}

