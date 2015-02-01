package org.activiti.spring.test.components.scope;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;


public class Delegate1 implements JavaDelegate, InitializingBean {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProcessInstance processInstance;

    @Autowired
    private StatefulObject statefulObject;


    public void execute(DelegateExecution execution) throws Exception {

        String pid = this.processInstance.getId();

        log.info("the processInstance#id is {}", pid);

        Assert.assertNotNull("the 'scopedCustomer' reference can't be null", statefulObject);
        String uuid = UUID.randomUUID().toString();
        statefulObject.setName(uuid);
        log.info("the 'uuid' value given to the ScopedCustomer#name property is '{}' in {}", uuid, getClass().getName());

        this.statefulObject.increment();
    }

    public void afterPropertiesSet() throws Exception {
        Assert.assertNotNull("the processInstance must not be null", this.processInstance);
    }
}
