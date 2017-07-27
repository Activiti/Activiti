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

package org.activiti.services.core.pageable.sort;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.TaskQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.TaskQuery;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class TaskSortApplier extends BaseSortApplier<TaskQuery> {

    private Map<String, TaskQueryProperty> orderByProperties = new HashMap<>();

    public TaskSortApplier() {
        orderByProperties.put("id", TaskQueryProperty.TASK_ID);
        orderByProperties.put("name", TaskQueryProperty.NAME);
        orderByProperties.put("assignee", TaskQueryProperty.ASSIGNEE);
    }

    @Override
    protected void applyDefaultSort(TaskQuery query) {
        query.orderByTaskId().asc();
    }

    @Override
    protected QueryProperty getOrderByProperty(Sort.Order order) {
        return orderByProperties.get(order.getProperty());
    }

}
