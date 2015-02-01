package org.activiti.spring.test.components.scope;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Josh Long
 * @since 5, 3
 */

public class Delegate2 implements JavaDelegate {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private StatefulObject statefulObject;

    public void execute(DelegateExecution execution) throws Exception {

        this.statefulObject.increment();

        Assert.assertNotNull("the 'scopedCustomer' reference can't be null", this.statefulObject);
        Assert.assertNotNull("the 'scopedCustomer.name' property should be non-null, since it was set in a previous delegate bound to this very thread", this.statefulObject.getName());
        log.info("the 'uuid' value retrieved from the ScopedCustomer#name property is '{}' in {}", this.statefulObject.getName(), getClass().getName());
    }
}
