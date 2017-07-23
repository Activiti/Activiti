package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.commands.Command;
import org.activiti.services.core.model.commands.StartProcessInstanceCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartProcessInstanceCmdExecutor implements CommandExecutor {

    private ProcessEngineWrapper processEngine;

    @Autowired
    public StartProcessInstanceCmdExecutor(ProcessEngineWrapper processEngine) {
        this.processEngine = processEngine;
    }

    @Override
    public Class getHandledType() {
        return StartProcessInstanceCmd.class;
    }

    @Override
    public void execute(Command cmd) {
        ProcessInstance processInstance = processEngine.startProcess((StartProcessInstanceCmd) cmd);
        System.out.println(processInstance.getId() + " -  " + processInstance);
    }
}
