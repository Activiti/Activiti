/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.delegate.invocation;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.helper.task.TaskComparatorImpl;
import org.activiti.engine.impl.bpmn.helper.task.TaskUpdater;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.task.TaskInfo;

/**
 * Class handling invocations of {@link TaskListener TaskListeners}
 *

 */
public class TaskListenerInvocation extends DelegateInvocation {

    protected final TaskListener executionListenerInstance;
    protected final DelegateTask delegateTask;

    public TaskListenerInvocation(TaskListener executionListenerInstance, DelegateTask delegateTask) {
        this.executionListenerInstance = executionListenerInstance;
        this.delegateTask = delegateTask;
    }

    protected void invoke() {
        TaskComparatorImpl taskComparator = new TaskComparatorImpl();
        taskComparator.setOriginalTask((TaskInfo)delegateTask);

        executionListenerInstance.notify(delegateTask);

        TaskUpdater taskUpdater = new TaskUpdater(Context.getCommandContext(), false);
        taskUpdater.updateTask(taskComparator.getOriginalTask(), (TaskInfo) delegateTask);
    }

    public Object getTarget() {
        return executionListenerInstance;
    }
}
