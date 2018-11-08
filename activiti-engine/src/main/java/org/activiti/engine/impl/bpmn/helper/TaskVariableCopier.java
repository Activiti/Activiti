package org.activiti.engine.impl.bpmn.helper;

import org.activiti.engine.impl.persistence.entity.TaskEntity;

public class TaskVariableCopier {

    public static void copyVariablesIntoTaskLocal(TaskEntity task){

        //TODO: would like to filter which variables copy much as with subProcesses

        task.setVariablesLocal(task.getVariables());


    }

    public static void copyVariablesOutFromTaskLocal(TaskEntity task){

        //TODO: would like to filter which variables copy much as with subProcesses

        //provided not a standalone task
        if(task.getProcessInstance()!=null) {
            task.getProcessInstance().setVariables(task.getVariablesLocal());
        }
    }
}
