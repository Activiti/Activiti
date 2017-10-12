/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.services.query.qraphql.autoconfigure;

import graphql.GraphQL;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto configuration of Activiti GraphQL Query Service components 
 */
@Configuration
@ConditionalOnClass(GraphQL.class)
@ConditionalOnProperty(name = "spring.activiti.services.query.graphql.enabled",
        havingValue = "true",
        matchIfMissing = false)
@EnableActivitiGraphQLQueryService
public class ActivitiGraphQLAutoConfiguration {

}
