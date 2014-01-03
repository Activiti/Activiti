package org.activiti.spring.components.support;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.annotations.BusinessKey;
import org.activiti.spring.annotations.ProcessVariable;
import org.activiti.spring.annotations.StartProcess;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * {@link org.aopalliance.intercept.MethodInterceptor} that starts a business process
 * as a result of a successful method invocation.
 *
 * @author Josh Long
 */
class ProcessStartingMethodInterceptor implements MethodInterceptor {

    private ParameterNameDiscoverer parameterNameDiscoverer;

    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * injected reference - can be obtained via a {@link org.activiti.spring.ProcessEngineFactoryBean}
     */
    protected ProcessEngine processEngine;

    /**
     * @param processEngine takes a reference to a {@link org.activiti.engine.ProcessEngine}
     */
    public ProcessStartingMethodInterceptor(ProcessEngine processEngine) {
        this(processEngine, new DefaultParameterNameDiscoverer());
    }

    public ProcessStartingMethodInterceptor(ProcessEngine processEngine, ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
        this.processEngine = processEngine;
    }

    private boolean shouldReturnProcessInstance(StartProcess startProcess, MethodInvocation methodInvocation, Object result) {
        return (result instanceof ProcessInstance || methodInvocation.getMethod().getReturnType().isAssignableFrom(ProcessInstance.class));
    }

    private boolean shouldReturnProcessInstanceId(StartProcess startProcess, MethodInvocation methodInvocation, Object result) {
        return startProcess.returnProcessInstanceId() && (result instanceof String || methodInvocation.getMethod().getReturnType().isAssignableFrom(String.class));
    }

    @SuppressWarnings("unused")
    private boolean shouldReturnAsyncResultWithProcessInstance(StartProcess startProcess, MethodInvocation methodInvocation, Object result) {
        return (result instanceof Future || methodInvocation.getMethod().getReturnType().isAssignableFrom(Future.class));
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {

        Method method = invocation.getMethod();

        StartProcess startProcess = AnnotationUtils.getAnnotation(method, StartProcess.class);

        String processKey = startProcess.processKey();

        Assert.hasText(processKey, "you must provide the name of process to start");

        Object result;
        try {
            result = invocation.proceed();
            Map<String, Object> vars = this.processVariablesFromAnnotations(invocation);

            String businessKey = this.processBusinessKey(invocation);

            log.info("variables for the started process: {}", vars.toString());

            RuntimeService runtimeService = this.processEngine.getRuntimeService();
            ProcessInstance pi;
            if (null != businessKey && StringUtils.hasText(businessKey)) {
                pi = runtimeService.startProcessInstanceByKey(processKey, businessKey, vars);
                log.info("the business key for the started process is '{}'", businessKey);
            } else {
                pi = runtimeService.startProcessInstanceByKey(processKey, vars);
            }

            String pId = pi.getId();

            if (invocation.getMethod().getReturnType().equals(void.class))
                return null;

            if (shouldReturnProcessInstance(startProcess, invocation, result))
                return pi;

            if (shouldReturnProcessInstanceId(startProcess, invocation, result))
                return pId;

            if (shouldReturnAsyncResultWithProcessInstance(startProcess, invocation, result)) {
                return new AsyncResult<ProcessInstance>(pi);
            }

        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
        return result;
    }

    private String processBusinessKey(MethodInvocation invocation) throws Throwable {
        List<Argument> arguments = mapOfAnnotationValues(BusinessKey.class, invocation);
        if (arguments.size() == 1) {
            for (Argument argument : arguments) {
                if (argument.getAnnotations().length > 0) {
                    Annotation[] annotations = argument.getAnnotations();
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof BusinessKey) {
                            Object o = argument.getValue();
                            Assert.isTrue(o instanceof String);
                            return (String) o;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static class Argument {
        private int index;
        private Annotation[] annotations;
        private Object value;
        private String argumentName;

        private String getArgumentName() {
            return argumentName;
        }

        public Argument(int indx, Annotation[] annotations, String argumentName, Object value) {
            this.index = indx;
            this.argumentName = argumentName;
            this.annotations = annotations == null ? new Annotation[0] : annotations;
            this.value = value;
        }

        public int getIndex() {
            return index;
        }

        public Annotation[] getAnnotations() {
            return annotations;
        }

        public Object getValue() {
            return value;
        }
    }


    private <K extends Annotation, V> List<Argument> mapOfAnnotationValues(Class<K> annotationType, MethodInvocation invocation) {
        Method method = invocation.getMethod();
        String[] argumentNames = this.parameterNameDiscoverer.getParameterNames(method);
        Annotation[][] annotations = method.getParameterAnnotations();
        List<Argument> argumentList = new ArrayList<Argument>();
        int paramIndex = 0;
        for (Annotation[] ann : annotations) {
            for (Annotation annotation : ann) {
                if (!annotationType.isAssignableFrom(annotation.getClass())) {
                    continue; // don't need this one
                }
                V value = (V) invocation.getArguments()[paramIndex];
                Argument argument = new Argument(paramIndex, ann, argumentNames[paramIndex], value);
                argumentList.add(argument);
            }
            paramIndex += 1;
        }
        return argumentList;

    }

    /**
     * if there any arguments with the {@link  org.activiti.spring.annotations.ProcessVariable} annotation,
     * then we feed those parameters into the business process
     *
     * @param invocation the invocation of the method as passed to the {@link org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)} method
     */
    private Map<String, Object> processVariablesFromAnnotations(MethodInvocation invocation) {
        List<Argument> args = mapOfAnnotationValues(ProcessVariable.class, invocation);
        Map<String, Object> vars = new HashMap<String, Object>();
        for (Argument argument : args) {
            Annotation[] annotations = argument.getAnnotations();
            for (Annotation a : annotations) {
                if (a instanceof ProcessVariable) {
                    ProcessVariable processVariable = (ProcessVariable) a;
                    String processVariableAnnotationValue = StringUtils.hasText(processVariable.value()) ? processVariable.value() : argument.getArgumentName();
                    vars.put(processVariableAnnotationValue, argument.getValue());
                }
            }
        }
        return vars;
    }

    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

}

