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

import javax.persistence.EntityManager;

import org.activiti.services.query.model.ProcessInstance;
import org.activiti.services.query.qraphql.web.ActivitiGraphQLController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

import graphql.GraphQL;

@Configuration
@PropertySource("classpath:/org/activiti/services/query/qraphql/default.properties")
@ConditionalOnClass(GraphQL.class)
@ConditionalOnProperty(name="spring.activiti.services.query.graphql.enabled", havingValue="true", matchIfMissing=false)
public class ActivitiGraphQLAutoConfiguration {
    
    @Configuration
    @Import(ActivitiGraphQLController.class)
    @EntityScan(basePackageClasses=ProcessInstance.class)
    @EnableConfigurationProperties(ActivitiGraphQLSchemaProperties.class)
    static class DefaultActivitiGraphQLJpaConfiguration implements ImportAware  {
        
        @Autowired
        ActivitiGraphQLSchemaProperties properties;

        @Bean
        @ConditionalOnMissingBean(GraphQLExecutor.class)
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        @ConditionalOnMissingBean(GraphQLSchemaBuilder.class)
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
            Assert.notNull(properties.getName(), "GraphQL schema name cannot be null.");
            Assert.notNull(properties.getDescription(), "GraphQL schema description cannot be null.");
            
            return new GraphQLJpaSchemaBuilder(entityManager)
                .name(properties.getName())
                .description(properties.getDescription());
        }

        @Override
        public void setImportMetadata(AnnotationMetadata importMetadata) {
            this.properties.setEnabled(true);
        }

    }
}
