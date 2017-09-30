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

package org.activiti.services.query.rest;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.activiti.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.services.query.model.ProcessInstance;
import org.activiti.services.query.rest.config.QueryRepositoryConfig;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;


/**
 * Enables Configuration of Activiti Query Rest Service Components
 * 
 */
@Documented
@Retention( RUNTIME )
@Target( TYPE )
@EnableSpringDataWebSupport
@Import(QueryRepositoryConfig.class)
@EntityScan(basePackageClasses=ProcessInstance.class)
@EnableJpaRepositories(basePackageClasses=ProcessInstanceRepository.class)
public @interface EnableActivitiRestQueryService {
	
}
