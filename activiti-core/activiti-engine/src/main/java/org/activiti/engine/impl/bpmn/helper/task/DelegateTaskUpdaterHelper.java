package org.activiti.engine.impl.bpmn.helper.task;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.CommandContext;

public class DelegateTaskUpdaterHelper implements TaskUpdaterHelper<DelegateTask>{

    private final CommandContext commandContext;

    public DelegateTaskUpdaterHelper(CommandContext commandContext) {
        this.commandContext = commandContext;
    }
    public void updateTask(DelegateTask originalTask, DelegateTask task) {
        TaskComparator taskComparator = new TaskInfoComparator();
        taskComparator.setOriginalTask(originalTask);

        // Only update history if history is enabled
        if (commandContext.getProcessEngineConfiguration().getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            if (taskComparator.hasTaskNameChanged(task)) {
                commandContext.getHistoryManager().recordTaskNameChange(task.getId(), task.getName());
            }
            if (taskComparator.hasTaskDescriptionChanged(task)) {
                commandContext.getHistoryManager().recordTaskDescriptionChange(task.getId(), task.getDescription());
            }
            if (taskComparator.hasTaskDueDateChanged(task)) {
                commandContext.getHistoryManager().recordTaskDueDateChange(task.getId(), task.getDueDate());
            }
            if (taskComparator.hasTaskPriorityChanged(task)) {
                commandContext.getHistoryManager().recordTaskPriorityChange(task.getId(), task.getPriority());
            }
            if (taskComparator.hasTaskCategoryChanged(task)) {
                commandContext.getHistoryManager().recordTaskCategoryChange(task.getId(), task.getCategory());
            }
            if (taskComparator.hasTaskFormKeyChanged(task)) {
                commandContext.getHistoryManager().recordTaskFormKeyChange(task.getId(), task.getFormKey());
            }
            /*
            if (taskComparator.hasTaskParentIdChanged(task)) {
                commandContext.getHistoryManager().recordTaskParentTaskIdChange(task.getId(), task.getParentTaskId());
            }
             */
            if (taskComparator.hasTaskDefinitionKeyChanged(task)) {
                commandContext.getHistoryManager().recordTaskDefinitionKeyChange(task.getId(), task.getTaskDefinitionKey());
            }
        }

        /*
        DelegateTask doesn't have ProcessInstance() nor taskAssignment
         */
        /*
        if (taskComparator.hasTaskOwnerChanged(task)) {
            if (task.getProcessInstanceId() != null) {
                commandContext.getIdentityLinkEntityManager().involveUser(task.getProcessInstance(), task.getOwner(), IdentityLinkType.PARTICIPANT);
            }
            commandContext.getHistoryManager().recordTaskOwnerChange(task.getId(), task.getOwner());
        }
        if (taskComparator.hasTaskAssigneeChanged(task)) {
            if (task.getProcessInstanceId() != null) {
                commandContext.getIdentityLinkEntityManager().involveUser(task.getProcessInstance(), task.getAssignee(), IdentityLinkType.PARTICIPANT);
            }
            commandContext.getHistoryManager().recordTaskAssigneeChange(task.getId(), task.getAssignee());

            commandContext.getProcessEngineConfiguration().getListenerNotificationHelper().executeTaskListeners(task, TaskListener.EVENTNAME_ASSIGNMENT);
            commandContext.getHistoryManager().recordTaskAssignment(task);

            if (commandContext.getEventDispatcher().isEnabled()) {
                commandContext.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_ASSIGNED, task));
            }
        }
         */
    }
}
