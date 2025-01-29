package org.activiti.engine.impl.bpmn.helper.task;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.function.Function;

public class DelegateTaskComparator implements TaskComparator<DelegateTask>{

    private DelegateTask initialDelegateTask;

    @Override
    public DelegateTask getOriginalTask() {
        return initialDelegateTask;
    }

    @Override
    public void setOriginalTask(DelegateTask originalTask) {
        initialDelegateTask = new TaskEntityImpl();
        if (originalTask !=null) {
            initialDelegateTask.setName(originalTask.getName());
        }
    }

    @Override
    public boolean hasTaskNameChanged(DelegateTask newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, DelegateTask::getName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskDescriptionChanged(DelegateTask newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, DelegateTask::getDescription)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskOwnerChanged(DelegateTask newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, DelegateTask::getOwner)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskPriorityChanged(DelegateTask newTask) {
        if (newTask!=null) {
            if( hasIntegerFieldChanged(newTask, DelegateTask::getPriority)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskCategoryChanged(DelegateTask newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, DelegateTask::getCategory)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskFormKeyChanged(DelegateTask newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, DelegateTask::getFormKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskParentIdChanged(DelegateTask newTask) {
        // DelegateTask doesn't have 'parentId' field to check against
        return false;
    }

    @Override
    public boolean hasTaskDefinitionKeyChanged(DelegateTask newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, DelegateTask::getTaskDefinitionKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskAssigneeChanged(DelegateTask newTask) {
        if (newTask!=null) {
            if( hasStringFieldChanged(newTask, DelegateTask::getAssignee)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTaskDueDateChanged(DelegateTask newTask) {
        if (newTask!=null) {
            if( hasDateFieldChanged(newTask, DelegateTask::getDueDate)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasStringFieldChanged(DelegateTask task, Function<DelegateTask, String> function) {
        if (task!=null) {
            if( !StringUtils.equals(function.apply(initialDelegateTask), function.apply(task) )) {
                return true;
            }
        }
        return false;
    }

    private boolean hasIntegerFieldChanged(DelegateTask task, Function<DelegateTask, Integer> function) {
        if (task!=null) {
            if( function.apply(initialDelegateTask)!=function.apply(task)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDateFieldChanged(DelegateTask task, Function<DelegateTask, Date> function) {
        if (task!=null) {
            Date originalDate = function.apply(initialDelegateTask);
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
