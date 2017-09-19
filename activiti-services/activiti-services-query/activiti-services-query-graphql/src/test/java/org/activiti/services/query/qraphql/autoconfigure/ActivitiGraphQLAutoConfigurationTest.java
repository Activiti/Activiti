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

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.services.query.qraphql.web.ActivitiGraphQLController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ActivitiGraphQLAutoConfigurationTest {

    @Autowired(required=false)
    private ActivitiGraphQLSchemaProperties  graphQLProperties;

    @Autowired(required=false)
    private GraphQLExecutor graphQLExecutor;

    @Autowired(required=false)
    private GraphQLSchemaBuilder graphQLSchemaBuilder;

    @Autowired(required=false)
    private ActivitiGraphQLController graphQLController;

    @SpringBootApplication
    static class Application {
    }
    
    @Test
    public void contextIsAutoConfigured() {
        assertThat(graphQLExecutor).isInstanceOf(GraphQLJpaExecutor.class);
        assertThat(graphQLSchemaBuilder).isInstanceOf(GraphQLJpaSchemaBuilder.class);
        assertThat(graphQLController).isNotNull();
        assertThat(graphQLProperties).isNotNull();
        
        assertThat(graphQLProperties.getName()).isEqualTo("ActivitiGraphQLSchema");
        assertThat(graphQLProperties.getPath()).isEqualTo("/graphql");
        assertThat(graphQLProperties.isEnabled()).isEqualTo(true);
        
    }
}