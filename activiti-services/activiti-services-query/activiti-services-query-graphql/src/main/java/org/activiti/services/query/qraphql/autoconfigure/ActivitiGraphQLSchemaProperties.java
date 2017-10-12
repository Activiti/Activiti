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

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix="spring.activiti.services.query.graphql")
@Validated
public class ActivitiGraphQLSchemaProperties {
    /**
     * Provides the name of GraphQL schema. This is required attribute.
     */
    @NotEmpty
    private String name;
    
    /**
     * Provides the description of GraphQL schema. Cannot be null.
     */
    @NotEmpty
    private String description;

    /**
     * Specifies type of the GraphQLSchema builder. Reserved for future extension
     */
    @NotNull
    private String type;
    
    /**
     * Enable or disable QraphQL module services.
     */
    private boolean enabled;
    
    /**
     * QraphQL query executor REST endpoint. Default value is /graphql
     */
    @NotEmpty
    private String path = "/graphql";

    /**
     * Default constructor
     */
    ActivitiGraphQLSchemaProperties() { }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the endpoint
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setPath(String path) {
        this.path = path;
    }

}
