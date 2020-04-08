package org.activiti.engine.impl.cmd;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.ProcessInstanceHelper;
import org.activiti.engine.runtime.ProcessInstance;

import java.io.Serializable;

public class StartCreatedProcessInstanceCmd<T> implements Command<ProcessInstance>, Serializable {

    private static final long serialVersionUID = 1L;
    private ProcessInstance internalProcessInstance;

    public StartCreatedProcessInstanceCmd(ProcessInstance internalProcessInstance){
        this.internalProcessInstance = internalProcessInstance;
    }

    @Override
    public ProcessInstance execute(CommandContext commandContext) {
        if(this.internalProcessInstance.getStartTime() != null){
            throw new ActivitiIllegalArgumentException("Process instance " + this.internalProcessInstance.getProcessInstanceId() + " has already been started");
        }

        ExecutionEntity processExecution = (ExecutionEntity) this.internalProcessInstance;
        ProcessInstanceHelper processInstanceHelper = commandContext.getProcessEngineConfiguration().getProcessInstanceHelper();
        processInstanceHelper.startProcessInstance(processExecution, commandContext, processExecution.getVariables(), processExecution.getCurrentFlowElement());
        return processExecution;
    }
}
