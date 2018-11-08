package org.activiti.spring.boot.test.util;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessCleanUpUtil {

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    public void cleanUpWithAdmin() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processes = processAdminRuntime.processInstances(Pageable.of(0,
                50));
        for (ProcessInstance p : processes.getContent()) {
            processAdminRuntime.delete(ProcessPayloadBuilder
                    .delete()
                    .withProcessInstance(p)
                    .withReason("test clean up")
                    .build());
        }
    }
}
