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

package org.activiti.services;

import org.activiti.client.model.Task;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.TaskQuery;
import org.activiti.model.converter.TaskConverter;
import org.activiti.services.sort.TaskSortApplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PageableTaskService {

    private final TaskService taskService;
    private final TaskConverter taskConverter;
    private final PageRetriever pageRetriever;
    private final TaskSortApplier sortApplier;

    @Autowired
    public PageableTaskService(TaskService taskService,
                               TaskConverter taskConverter,
                               PageRetriever pageRetriever,
                               TaskSortApplier sortApplier) {
        this.taskService = taskService;
        this.taskConverter = taskConverter;
        this.pageRetriever = pageRetriever;
        this.sortApplier = sortApplier;
    }

    public Page<Task> getTasks(Pageable pageable) {
        TaskQuery query = taskService.createTaskQuery();
        sortApplier.applySort(query, pageable);
        return pageRetriever.loadPage(query, pageable, taskConverter);
    }

}
