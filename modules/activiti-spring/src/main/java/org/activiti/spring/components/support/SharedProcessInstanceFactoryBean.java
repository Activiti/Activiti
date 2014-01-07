package org.activiti.spring.components.support;

import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

public class SharedProcessInstanceFactoryBean implements FactoryBean<ProcessInstance> {

    private ProcessInstance processInstance;

    public SharedProcessInstanceFactoryBean(SharedProcessInstanceHolder holder) {
        Assert.notNull(holder, "the SharedProcessInstanceHolder should not be null");
        this.processInstance = holder.sharedProcessInstance();
        Assert.notNull(this.processInstance, "the processInstance obtained from " +
                "the SharedProcessInstanceHolder must not be null");
    }

    @Override
    public ProcessInstance getObject() throws Exception {
        return this.processInstance;
    }

    @Override
    public Class<?> getObjectType() {
        return ProcessInstance.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
