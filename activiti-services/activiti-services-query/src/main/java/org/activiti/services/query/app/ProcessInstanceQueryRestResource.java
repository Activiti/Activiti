/*
 * Copyright 2017 Alfresco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.services.query.app;

import com.querydsl.core.types.dsl.StringPath;
import org.activiti.services.query.app.model.ProcessInstance;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.CrudRepository;
import org.activiti.services.query.app.model.QProcessInstance;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;


interface ProcessInstanceQueryRestResource extends CrudRepository<ProcessInstance, String>, QuerydslPredicateExecutor<ProcessInstance>, QuerydslBinderCustomizer<QProcessInstance> {

    @Override
    default public void customize(QuerydslBindings bindings, QProcessInstance root) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value));

    }
}
