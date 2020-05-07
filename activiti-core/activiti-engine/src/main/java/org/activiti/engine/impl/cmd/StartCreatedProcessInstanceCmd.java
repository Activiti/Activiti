package org.activiti.engine.impl.cmd;

import java.util.Map;
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
    private Map<String, Object> variables;

    public StartCreatedProcessInstanceCmd(ProcessInstance internalProcessInstance, Map<String, Object> variables){
        this.internalProcessInstance = internalProcessInstance;
        this.variables = variables;
    }

    @Override
    public ProcessInstance execute(CommandContext commandContext) {
        if(this.internalProcessInstance.getStartTime() != null){
            throw new ActivitiIllegalArgumentException("Process instance " + this.internalProcessInstance.getProcessInstanceId() + " has already been started");
        }

        ExecutionEntity processExecution = (ExecutionEntity) this.internalProcessInstance;

        if (variables != null) {
            for (String varName : variables.keySet()) {
                processExecution.setVariable(varName, variables.get(varName));
            }
        }
        ProcessInstanceHelper processInstanceHelper = commandContext.getProcessEngineConfiguration().getProcessInstanceHelper();
//        processInstanceHelper.
        processInstanceHelper.startProcessInstance(processExecution, commandContext, processExecution.getVariables(), processExecution.getCurrentFlowElement());
        return processExecution;
    }


}
