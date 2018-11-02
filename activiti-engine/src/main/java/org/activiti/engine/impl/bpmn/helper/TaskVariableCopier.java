package org.activiti.engine.impl.bpmn.helper;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.task.Task;

public class TaskVariableCopier {

    public static void copyVariablesIntoTaskLocal(Task task, CommandContext commandContext){

        //TODO: would like to filter which variables copy much as with subProcesses

        commandContext.getProcessEngineConfiguration().getTaskService().setVariablesLocal(task.getId(),commandContext.getProcessEngineConfiguration().getTaskService().getVariables(task.getId()));

    }

    public static void copyVariablesOutFromTaskLocal(Task task, CommandContext commandContext){

        //TODO: would like to filter which variables copy much as with subProcesses

        commandContext.getProcessEngineConfiguration().getRuntimeService().setVariables(task.getProcessInstanceId(),commandContext.getProcessEngineConfiguration().getTaskService().getVariablesLocal(task.getId()));
    }
}
