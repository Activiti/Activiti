package org.activiti.spring.components.support;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.runtime.ProcessInstance;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * <EM>Not for external use.</EM>
 * <p/>
 * Guards thread-local state for {@link org.activiti.engine.runtime.ProcessInstance processInstance}.
 *
 * @author Josh Long
 * @see org.activiti.engine.runtime.ProcessInstance
 */
public class SharedProcessInstanceHolder {

    private final Callable<ProcessInstance> defaultProcessInstanceCallable =
            new Callable<ProcessInstance>() {
                @Override
                public ProcessInstance call() throws Exception {
                    return Context.getExecutionContext().getProcessInstance();
                }
            };

    private final ThreadLocal<Callable<ProcessInstance>> callableThreadLocal =
            new ThreadLocal<Callable<ProcessInstance>>();

    private final ProcessInstance processInstance;

    public SharedProcessInstanceHolder() {
        ProxyFactory proxyFactoryBean = new ProxyFactory(ProcessInstance.class, new MethodInterceptor() {
            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                String methodName = methodInvocation.getMethod().getName();
                if (methodName.equals("toString")) {
                    return "SharedProcessInstance";
                }

                ProcessInstance processInstance = obtainThreadLocalProcessInstance();
                Method method = methodInvocation.getMethod();
                Object[] args = methodInvocation.getArguments();
                return method.invoke(processInstance, args);
            }
        });

        this.processInstance = (ProcessInstance) proxyFactoryBean.getProxy(ClassUtils.getDefaultClassLoader());

        Assert.notNull(this.processInstance, "the processInstance proxy must not be null");
    }

    public ProcessInstance sharedProcessInstance() {
        return this.processInstance;
    }

    public void registerProcessInstanceCallable(final Callable<ProcessInstance> runnable) {

        callableThreadLocal.set(new Callable<ProcessInstance>() {
            private ProcessInstance processInstance;

            @Override
            public ProcessInstance call() throws Exception {
                if (null == processInstance) {
                    processInstance = runnable.call();
                }
                return processInstance;
            }
        });
    }

    protected ProcessInstance obtainThreadLocalProcessInstance() {
        try {
            if (callableThreadLocal.get() == null)
                registerProcessInstanceCallable(defaultProcessInstanceCallable);
            return callableThreadLocal.get().call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
