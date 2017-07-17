package org.activiti.services.core;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.services.audit.producer.app.MessageProducerActivitiEventListener;
import org.activiti.services.model.commands.SignalInfo;
import org.activiti.services.model.commands.StartProcessInfo;
import org.activiti.services.core.pageable.PageableProcessInstanceService;
import org.activiti.services.model.ProcessInstance;
import org.activiti.services.model.converter.ProcessInstanceConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ProcessEngineWrapper {

    private final ProcessInstanceConverter processInstanceConverter;

    private final RuntimeService runtimeService;

    private PageableProcessInstanceService pageableProcessInstanceService;

    @Autowired
    public ProcessEngineWrapper(ProcessInstanceConverter processInstanceConverter,
                                RuntimeService runtimeService,
                                PageableProcessInstanceService pageableProcessInstanceService,
                                MessageProducerActivitiEventListener listener) {
        this.processInstanceConverter = processInstanceConverter;
        this.runtimeService = runtimeService;
        this.pageableProcessInstanceService = pageableProcessInstanceService;
        this.runtimeService.addEventListener(listener);
    }

    public Page<ProcessInstance> getProcessInstances(Pageable pageable) {
        return pageableProcessInstanceService.getProcessInstances(pageable);
    }

    public ProcessInstance startProcess(StartProcessInfo info) {
        ProcessInstanceBuilder builder = runtimeService.createProcessInstanceBuilder();
        builder.processDefinitionKey(info.getProcessDefinitionKey());
        builder.variables(info.getVariables());
        return processInstanceConverter.from(builder.start());
    }

    public void signal(SignalInfo signalInfo) {
        runtimeService.signalEventReceived(signalInfo.getName(),
                                           signalInfo.getInputVariables());
    }

    public void suspend(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    public void activate(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    public ProcessInstance getProcessInstance(String processInstanceId) {
        org.activiti.engine.runtime.ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        return processInstanceConverter.from(processInstance);
    }
}
