/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.runtime.api.model.builder.CompleteTaskPayload;
import org.activiti.runtime.api.model.builder.TaskCreator;

public interface FluentTask extends Task {

    <T> void variable(String name,
                      T value);

    void variables(Map<String, Object> variables);

    <T> void localVariable(String name,
                           T value);

    void localVariables(Map<String, Object> variables);

    List<VariableInstance> variables();

    List<VariableInstance> localVariables();

    void complete();

    CompleteTaskPayload completeWith();

    void claim(String username);

    void release();

    void updateName(String name);

    void updateDescription(String description);

    void updateDueDate(Date dueDate);

    void updatePriority(int priority);

    void updateParentTaskId(String parentTaskId);

    void delete(String reason);

    TaskCreator createSubTaskWith();

    //TODO should we use a page here? in this case we need to add the possibility
    // to filter based on the parent task id in the internal task query
    List<FluentTask> subTasks();
}
