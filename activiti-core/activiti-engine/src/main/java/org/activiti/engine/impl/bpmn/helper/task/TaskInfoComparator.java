/*
 * Copyright 2010-2025 Alfresco Software, Ltd.
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
package org.activiti.engine.impl.bpmn.helper.task;

import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.task.TaskInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.function.Function;

public class TaskInfoComparator implements TaskComparator<TaskInfo>{

    private TaskEntityImpl initialTaskInfo;

    @Override
    public TaskInfo getOriginalTask() {
        return initialTaskInfo;
    }

    @Override
    public void setOriginalTask(TaskInfo originalTask) {
        initialTaskInfo = new TaskEntityImpl();
        if (originalTask !=null) {
            initialTaskInfo.setName(originalTask.getName());
        }
    }

    @Override
    public boolean hasTaskNameChanged(TaskInfo newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, TaskInfo::getName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskDescriptionChanged(TaskInfo newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, TaskInfo::getDescription)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskOwnerChanged(TaskInfo newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, TaskInfo::getOwner)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskPriorityChanged(TaskInfo newTask) {
        if (newTask!=null) {
            if( hasIntegerFieldChanged(newTask, TaskInfo::getPriority)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskCategoryChanged(TaskInfo newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, TaskInfo::getCategory)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskFormKeyChanged(TaskInfo newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, TaskInfo::getFormKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskParentIdChanged(TaskInfo newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, TaskInfo::getParentTaskId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskDefinitionKeyChanged(TaskInfo newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, TaskInfo::getTaskDefinitionKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskAssigneeChanged(TaskInfo newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, TaskInfo::getAssignee)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskDueDateChanged(TaskInfo newTask) {
        if (newTask!=null) {
            if( hasDateFieldChanged(newTask, TaskInfo::getDueDate)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasStringFieldChanged(TaskInfo task, Function<TaskInfo, String> function) {
        if (task!=null) {
            if( !StringUtils.equals(function.apply(initialTaskInfo), function.apply(task) )) {
                return true;
            }
        }
        return false;
    }

    private boolean hasIntegerFieldChanged(TaskInfo task, Function<TaskInfo, Integer> function) {
        if (task!=null) {
            if (function.apply(initialTaskInfo)!=function.apply(task)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDateFieldChanged(TaskInfo task, Function<TaskInfo, Date> function) {
        if (task!=null) {
            Date originalDate = function.apply(initialTaskInfo);
            Date newDate = function.apply(task);

            if ((originalDate == null && newDate != null)
                || (originalDate != null && newDate == null)
                || (originalDate != null && !originalDate.equals(newDate))) {
                return true;
            }
        }
        return false;
    }
}
