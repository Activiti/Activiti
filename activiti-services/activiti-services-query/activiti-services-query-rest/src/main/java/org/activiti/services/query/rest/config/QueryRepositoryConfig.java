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

package org.activiti.services.query.rest.config;

import org.activiti.services.query.model.ProcessInstance;
import org.activiti.services.query.model.Task;
import org.activiti.services.query.model.Variable;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy.RepositoryDetectionStrategies;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

@Configuration
public class QueryRepositoryConfig extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
    	// Configure base path for api endpoint
    	config.setBasePath("/v1");
    	
    	// Expose only repositories annotated with @RepositoryRestResource
    	config.setRepositoryDetectionStrategy(RepositoryDetectionStrategies.ANNOTATED);
    	
        //by default the ids are not exposed the the REST API
        config.exposeIdsFor(ProcessInstance.class);
        config.exposeIdsFor(Task.class);
        config.exposeIdsFor(Variable.class);
    }

}