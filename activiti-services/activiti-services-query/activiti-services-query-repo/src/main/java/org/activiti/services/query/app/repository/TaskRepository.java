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

package org.activiti.services.query.app.repository;

import com.querydsl.core.types.dsl.StringPath;
import org.activiti.services.query.model.QTask;
import org.activiti.services.query.model.Task;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/tasks",
        collectionResourceDescription = @Description("Collection of task instance resources"),
        collectionResourceRel = "tasks",
        itemResourceRel = "task")
public interface TaskRepository extends RestResourceQueryRepository<Task, String>, QuerydslPredicateExecutor<Task>, QuerydslBinderCustomizer<QTask> {

    @Override
    default void customize(QuerydslBindings bindings,
                           QTask root) {

        bindings.bind(String.class).first(
                                          (StringPath path, String value) -> path.eq(value));
        bindings.bind(root.lastModifiedFrom).first((path, value) -> root.lastModified.after(value));
        bindings.bind(root.lastModifiedTo).first((path, value) -> root.lastModified.before(value));
    }
}