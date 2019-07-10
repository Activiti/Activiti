package org.activiti.spring.boot.test.util;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class ProcessCleanUpUtil {

    private static final int MAX_ITEMS = 100;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    public void cleanUpWithAdmin() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processes = processAdminRuntime.processInstances(Pageable.of(0,
                                                                                           MAX_ITEMS));
        for (ProcessInstance p : processes.getContent()) {
            //only delete parent process instances: children instances will be deleted on cascade
            if (p.getParentId() == null) {
                processAdminRuntime.delete(ProcessPayloadBuilder
                                                   .delete()
                                                   .withProcessInstance(p)
                                                   .withReason("test clean up")
                                                   .build());
            }
        }
        assertThat(processAdminRuntime.processInstances(Pageable.of(0,
                                                                    MAX_ITEMS)).getContent())
                .as("fail to clean up all the process instances")
                .isEmpty();
    }
}
