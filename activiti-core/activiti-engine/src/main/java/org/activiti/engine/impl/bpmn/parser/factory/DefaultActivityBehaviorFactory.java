/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.bpmn.parser.factory;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.SendTask;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.TaskWithFieldExtensions;
import org.activiti.bpmn.model.TerminateEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.Transaction;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BusinessRuleTaskDelegate;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.AdhocSubProcessActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryCancelEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryCompensateEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryMessageEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundarySignalEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryTimerEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CancelEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ErrorEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventBasedGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessErrorStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessMessageStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.InclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchMessageEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchSignalEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchTimerEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowCompensationEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowMessageEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowNoneEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowSignalEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ManualTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.ServiceTaskDelegateExpressionActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ServiceTaskExpressionActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ShellActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TerminateEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ThrowMessageEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TransactionActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.WebServiceActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.helper.ClassDelegateFactory;
import org.activiti.engine.impl.bpmn.helper.DefaultClassDelegateFactory;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.MessagePayloadMappingProvider;
import org.activiti.engine.impl.delegate.ThrowMessageDelegate;
import org.activiti.engine.impl.delegate.ThrowMessageDelegateExpression;
import org.activiti.engine.impl.delegate.ThrowMessageJavaDelegate;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.util.ReflectUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Default implementation of the {@link ActivityBehaviorFactory}. Used when no custom {@link ActivityBehaviorFactory} is injected on the {@link ProcessEngineConfigurationImpl}.
 */
public class DefaultActivityBehaviorFactory extends AbstractBehaviorFactory implements ActivityBehaviorFactory {

    public static final String DEFAULT_SERVICE_TASK_BEAN_NAME = "defaultServiceTaskBehavior";

    private final ClassDelegateFactory classDelegateFactory;

    public DefaultActivityBehaviorFactory(ClassDelegateFactory classDelegateFactory) {
        this.classDelegateFactory = classDelegateFactory;
    }

    public DefaultActivityBehaviorFactory() {
        this(new DefaultClassDelegateFactory());
    }

    // Start event
    public final static String EXCEPTION_MAP_FIELD = "mapExceptions";

    public NoneStartEventActivityBehavior createNoneStartEventActivityBehavior(StartEvent startEvent) {
        return new NoneStartEventActivityBehavior();
    }

    // Task

    public TaskActivityBehavior createTaskActivityBehavior(Task task) {
        return new TaskActivityBehavior();
    }

    public ManualTaskActivityBehavior createManualTaskActivityBehavior(ManualTask manualTask) {
        return new ManualTaskActivityBehavior();
    }

    public ReceiveTaskActivityBehavior createReceiveTaskActivityBehavior(ReceiveTask receiveTask) {
        return new ReceiveTaskActivityBehavior();
    }

    @Override
    public UserTaskActivityBehavior createUserTaskActivityBehavior(UserTask userTask) {
        return new UserTaskActivityBehavior(userTask);
    }

    // Service task

    protected Expression getSkipExpressionFromServiceTask(ServiceTask serviceTask) {
        Expression result = null;
        if (StringUtils.isNotEmpty(serviceTask.getSkipExpression())) {
            result = expressionManager.createExpression(serviceTask.getSkipExpression());
        }
        return result;
    }

    public ClassDelegate createClassDelegateServiceTask(ServiceTask serviceTask) {
        return classDelegateFactory.create(serviceTask.getId(),
                                           serviceTask.getImplementation(),
                                           createFieldDeclarations(serviceTask.getFieldExtensions()),
                                           getSkipExpressionFromServiceTask(serviceTask),
                                           serviceTask.getMapExceptions());
    }

    public ServiceTaskDelegateExpressionActivityBehavior createServiceTaskDelegateExpressionActivityBehavior(ServiceTask serviceTask) {
        Expression delegateExpression = expressionManager.createExpression(serviceTask.getImplementation());
        return createServiceTaskBehavior(serviceTask,
                                         delegateExpression);
    }

    public ActivityBehavior createDefaultServiceTaskBehavior(ServiceTask serviceTask) {
        // this is covering the case where only the field `implementation` was defined in the process definition. I.e.
        // <serviceTask id="serviceTask" implementation="myServiceTaskImpl"/>
        // `myServiceTaskImpl` can be different things depending on the implementation of `defaultServiceTaskBehavior`
        // could be for instance a Spring bean or a target for a Spring Stream
        Expression delegateExpression = expressionManager.createExpression("${" + DEFAULT_SERVICE_TASK_BEAN_NAME + "}");
        return createServiceTaskBehavior(serviceTask,
                                         delegateExpression);
    }

    private ServiceTaskDelegateExpressionActivityBehavior createServiceTaskBehavior(ServiceTask serviceTask,
                                                                                    Expression delegateExpression) {
        return new ServiceTaskDelegateExpressionActivityBehavior(serviceTask.getId(),
                                                                 delegateExpression,
                                                                 getSkipExpressionFromServiceTask(serviceTask),
                                                                 createFieldDeclarations(serviceTask.getFieldExtensions()));
    }

    public ServiceTaskExpressionActivityBehavior createServiceTaskExpressionActivityBehavior(ServiceTask serviceTask) {
        Expression expression = expressionManager.createExpression(serviceTask.getImplementation());
        return new ServiceTaskExpressionActivityBehavior(serviceTask.getId(),
                                                         expression,
                                                         getSkipExpressionFromServiceTask(serviceTask),
                                                         serviceTask.getResultVariableName());
    }

    public WebServiceActivityBehavior createWebServiceActivityBehavior(ServiceTask serviceTask) {
        return new WebServiceActivityBehavior();
    }

    public WebServiceActivityBehavior createWebServiceActivityBehavior(SendTask sendTask) {
        return new WebServiceActivityBehavior();
    }

    public MailActivityBehavior createMailActivityBehavior(ServiceTask serviceTask) {
        return createMailActivityBehavior(serviceTask.getId(),
                                          serviceTask.getFieldExtensions());
    }

    public MailActivityBehavior createMailActivityBehavior(SendTask sendTask) {
        return createMailActivityBehavior(sendTask.getId(),
                                          sendTask.getFieldExtensions());
    }

    protected MailActivityBehavior createMailActivityBehavior(String taskId,
                                                              List<FieldExtension> fields) {
        List<FieldDeclaration> fieldDeclarations = createFieldDeclarations(fields);
        return (MailActivityBehavior) ClassDelegate.defaultInstantiateDelegate(
                MailActivityBehavior.class,
                fieldDeclarations);
    }

    // We do not want a hard dependency on Mule, hence we return
    // ActivityBehavior and instantiate the delegate instance using a string instead of the Class itself.
    public ActivityBehavior createMuleActivityBehavior(ServiceTask serviceTask) {
        return createMuleActivityBehavior(serviceTask,
                                          serviceTask.getFieldExtensions());
    }

    public ActivityBehavior createMuleActivityBehavior(SendTask sendTask) {
        return createMuleActivityBehavior(sendTask,
                                          sendTask.getFieldExtensions());
    }

    protected ActivityBehavior createMuleActivityBehavior(TaskWithFieldExtensions task,
                                                          List<FieldExtension> fieldExtensions) {
        try {

            Class<?> theClass = Class.forName("org.activiti.mule.MuleSendActivitiBehavior");
            List<FieldDeclaration> fieldDeclarations = createFieldDeclarations(fieldExtensions);
            return (ActivityBehavior) ClassDelegate.defaultInstantiateDelegate(
                    theClass,
                    fieldDeclarations);
        } catch (ClassNotFoundException e) {
            throw new ActivitiException("Could not find org.activiti.mule.MuleSendActivitiBehavior: ",
                                        e);
        }
    }

    // We do not want a hard dependency on Camel, hence we return
    // ActivityBehavior and instantiate the delegate instance using a string instead of the Class itself.
    public ActivityBehavior createCamelActivityBehavior(ServiceTask serviceTask) {
        return createCamelActivityBehavior(serviceTask,
                                           serviceTask.getFieldExtensions());
    }

    public ActivityBehavior createCamelActivityBehavior(SendTask sendTask) {
        return createCamelActivityBehavior(sendTask,
                                           sendTask.getFieldExtensions());
    }

    protected ActivityBehavior createCamelActivityBehavior(TaskWithFieldExtensions task,
                                                           List<FieldExtension> fieldExtensions) {
        try {
            Class<?> theClass = null;
            FieldExtension behaviorExtension = null;
            for (FieldExtension fieldExtension : fieldExtensions) {
                if ("camelBehaviorClass".equals(fieldExtension.getFieldName()) && StringUtils.isNotEmpty(fieldExtension.getStringValue())) {
                    theClass = Class.forName(fieldExtension.getStringValue());
                    behaviorExtension = fieldExtension;
                    break;
                }
            }

            if (behaviorExtension != null) {
                fieldExtensions.remove(behaviorExtension);
            }

            if (theClass == null) {
                // Default Camel behavior class
                theClass = Class.forName("org.activiti.camel.impl.CamelBehaviorDefaultImpl");
            }

            List<FieldDeclaration> fieldDeclarations = createFieldDeclarations(fieldExtensions);
            addExceptionMapAsFieldDeclaration(fieldDeclarations,
                                              task.getMapExceptions());
            return (ActivityBehavior) ClassDelegate.defaultInstantiateDelegate(
                    theClass,
                    fieldDeclarations);
        } catch (ClassNotFoundException e) {
            throw new ActivitiException("Could not find org.activiti.camel.CamelBehavior: ",
                                        e);
        }
    }

    private void addExceptionMapAsFieldDeclaration(List<FieldDeclaration> fieldDeclarations,
                                                   List<MapExceptionEntry> mapExceptions) {
        FieldDeclaration exceptionMapsFieldDeclaration = new FieldDeclaration(EXCEPTION_MAP_FIELD,
                                                                              mapExceptions.getClass().toString(),
                                                                              mapExceptions);
        fieldDeclarations.add(exceptionMapsFieldDeclaration);
    }

    public ShellActivityBehavior createShellActivityBehavior(ServiceTask serviceTask) {
        List<FieldDeclaration> fieldDeclarations = createFieldDeclarations(serviceTask.getFieldExtensions());
        return (ShellActivityBehavior) ClassDelegate.defaultInstantiateDelegate(
                ShellActivityBehavior.class,
                fieldDeclarations);
    }

    public ActivityBehavior createBusinessRuleTaskActivityBehavior(BusinessRuleTask businessRuleTask) {
        BusinessRuleTaskDelegate ruleActivity = null;
        if (StringUtils.isNotEmpty(businessRuleTask.getClassName())) {
            try {
                Class<?> clazz = Class.forName(businessRuleTask.getClassName());
                ruleActivity = (BusinessRuleTaskDelegate) clazz.newInstance();
            } catch (Exception e) {
                throw new ActivitiException("Could not instantiate businessRuleTask (id:" + businessRuleTask.getId() + ") class: " +
                                                    businessRuleTask.getClassName(),
                                            e);
            }
        } else {
            // no default behavior
        }

        for (String ruleVariableInputObject : businessRuleTask.getInputVariables()) {
            ruleActivity.addRuleVariableInputIdExpression(expressionManager.createExpression(ruleVariableInputObject.trim()));
        }

        for (String rule : businessRuleTask.getRuleNames()) {
            ruleActivity.addRuleIdExpression(expressionManager.createExpression(rule.trim()));
        }

        ruleActivity.setExclude(businessRuleTask.isExclude());

        if (businessRuleTask.getResultVariableName() != null && businessRuleTask.getResultVariableName().length() > 0) {
            ruleActivity.setResultVariable(businessRuleTask.getResultVariableName());
        } else {
            ruleActivity.setResultVariable("org.activiti.engine.rules.OUTPUT");
        }

        return ruleActivity;
    }

    // Script task

    public ScriptTaskActivityBehavior createScriptTaskActivityBehavior(ScriptTask scriptTask) {
        String language = scriptTask.getScriptFormat();
        if (language == null) {
            language = ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE;
        }
        return new ScriptTaskActivityBehavior(scriptTask.getId(),
                                              scriptTask.getScript(),
                                              language,
                                              scriptTask.getResultVariable(),
                                              scriptTask.isAutoStoreVariables());
    }

    // Gateways

    public ExclusiveGatewayActivityBehavior createExclusiveGatewayActivityBehavior(ExclusiveGateway exclusiveGateway) {
        return new ExclusiveGatewayActivityBehavior();
    }

    public ParallelGatewayActivityBehavior createParallelGatewayActivityBehavior(ParallelGateway parallelGateway) {
        return new ParallelGatewayActivityBehavior();
    }

    public InclusiveGatewayActivityBehavior createInclusiveGatewayActivityBehavior(InclusiveGateway inclusiveGateway) {
        return new InclusiveGatewayActivityBehavior();
    }

    public EventBasedGatewayActivityBehavior createEventBasedGatewayActivityBehavior(EventGateway eventGateway) {
        return new EventBasedGatewayActivityBehavior();
    }

    // Multi Instance

    public SequentialMultiInstanceBehavior createSequentialMultiInstanceBehavior(Activity activity,
                                                                                 AbstractBpmnActivityBehavior innerActivityBehavior) {
        return new SequentialMultiInstanceBehavior(activity,
                                                   innerActivityBehavior);
    }

    public ParallelMultiInstanceBehavior createParallelMultiInstanceBehavior(Activity activity,
                                                                             AbstractBpmnActivityBehavior innerActivityBehavior) {
        return new ParallelMultiInstanceBehavior(activity,
                                                 innerActivityBehavior);
    }

    // Subprocess

    public SubProcessActivityBehavior createSubprocessActivityBehavior(SubProcess subProcess) {
        return new SubProcessActivityBehavior();
    }

    public EventSubProcessErrorStartEventActivityBehavior createEventSubProcessErrorStartEventActivityBehavior(StartEvent startEvent) {
        return new EventSubProcessErrorStartEventActivityBehavior();
    }

    public EventSubProcessMessageStartEventActivityBehavior createEventSubProcessMessageStartEventActivityBehavior(StartEvent startEvent,
                                                                                                                   MessageEventDefinition messageEventDefinition) {
        MessageExecutionContext messageExecutionContext = createMessageExecutionContext(startEvent,
                                                                                        messageEventDefinition);

        return new EventSubProcessMessageStartEventActivityBehavior(messageEventDefinition,
                                                                    messageExecutionContext);
    }

    public AdhocSubProcessActivityBehavior createAdhocSubprocessActivityBehavior(SubProcess subProcess) {
        return new AdhocSubProcessActivityBehavior();
    }

    // Call activity

    public CallActivityBehavior createCallActivityBehavior(CallActivity callActivity) {
        String expressionRegex = "\\$+\\{+.+\\}";

        CallActivityBehavior callActivityBehaviour = null;
        if (StringUtils.isNotEmpty(callActivity.getCalledElement()) && callActivity.getCalledElement().matches(expressionRegex)) {
            callActivityBehaviour = createCallActivityBehavior(expressionManager.createExpression(callActivity.getCalledElement()), callActivity.getMapExceptions());
        } else {
            callActivityBehaviour = createCallActivityBehavior(callActivity.getCalledElement(), callActivity.getMapExceptions());
        }

        return callActivityBehaviour;
    }

    protected CallActivityBehavior createCallActivityBehavior(String calledElement, List<MapExceptionEntry> mapExceptions) {
        return new CallActivityBehavior(calledElement,
                mapExceptions);
    }

    protected CallActivityBehavior createCallActivityBehavior(Expression expression, List<MapExceptionEntry> mapExceptions) {
        return new CallActivityBehavior(expression,
                mapExceptions);
    }

    // Transaction

    public TransactionActivityBehavior createTransactionActivityBehavior(Transaction transaction) {
        return new TransactionActivityBehavior();
    }

    // Intermediate Events

    public IntermediateCatchEventActivityBehavior createIntermediateCatchEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent) {
        return new IntermediateCatchEventActivityBehavior();
    }

    public IntermediateCatchMessageEventActivityBehavior createIntermediateCatchMessageEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent,
                                                                                                             MessageEventDefinition messageEventDefinition) {
        MessageExecutionContext messageExecutionContext = createMessageExecutionContext(intermediateCatchEvent,
                                                                                        messageEventDefinition);
        return new IntermediateCatchMessageEventActivityBehavior(messageEventDefinition,
                                                                 messageExecutionContext);
    }

    public IntermediateCatchTimerEventActivityBehavior createIntermediateCatchTimerEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent,
                                                                                                         TimerEventDefinition timerEventDefinition) {
        return new IntermediateCatchTimerEventActivityBehavior(timerEventDefinition);
    }

    public IntermediateCatchSignalEventActivityBehavior createIntermediateCatchSignalEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent,
                                                                                                           SignalEventDefinition signalEventDefinition,
                                                                                                           Signal signal) {

        return new IntermediateCatchSignalEventActivityBehavior(signalEventDefinition,
                                                                signal);
    }

    public IntermediateThrowNoneEventActivityBehavior createIntermediateThrowNoneEventActivityBehavior(ThrowEvent throwEvent) {
        return new IntermediateThrowNoneEventActivityBehavior();
    }

    public IntermediateThrowSignalEventActivityBehavior createIntermediateThrowSignalEventActivityBehavior(ThrowEvent throwEvent,
                                                                                                           SignalEventDefinition signalEventDefinition,
                                                                                                           Signal signal) {

        return new IntermediateThrowSignalEventActivityBehavior(signalEventDefinition,
                                                                signal);
    }

    public IntermediateThrowCompensationEventActivityBehavior createIntermediateThrowCompensationEventActivityBehavior(ThrowEvent throwEvent,
                                                                                                                       CompensateEventDefinition compensateEventDefinition) {
        return new IntermediateThrowCompensationEventActivityBehavior(compensateEventDefinition);
    }

    // End events

    public NoneEndEventActivityBehavior createNoneEndEventActivityBehavior(EndEvent endEvent) {
        return new NoneEndEventActivityBehavior();
    }

    public ErrorEndEventActivityBehavior createErrorEndEventActivityBehavior(EndEvent endEvent,
                                                                             ErrorEventDefinition errorEventDefinition) {
        return new ErrorEndEventActivityBehavior(errorEventDefinition.getErrorRef());
    }

    public CancelEndEventActivityBehavior createCancelEndEventActivityBehavior(EndEvent endEvent) {
        return new CancelEndEventActivityBehavior();
    }

    public TerminateEndEventActivityBehavior createTerminateEndEventActivityBehavior(EndEvent endEvent) {
        boolean terminateAll = false;
        boolean terminateMultiInstance = false;

        if (endEvent.getEventDefinitions() != null
                && endEvent.getEventDefinitions().size() > 0
                && endEvent.getEventDefinitions().get(0) instanceof TerminateEventDefinition) {
            terminateAll = ((TerminateEventDefinition) endEvent.getEventDefinitions().get(0)).isTerminateAll();
            terminateMultiInstance = ((TerminateEventDefinition) endEvent.getEventDefinitions().get(0)).isTerminateMultiInstance();
        }

        TerminateEndEventActivityBehavior terminateEndEventActivityBehavior = new TerminateEndEventActivityBehavior();
        terminateEndEventActivityBehavior.setTerminateAll(terminateAll);
        terminateEndEventActivityBehavior.setTerminateMultiInstance(terminateMultiInstance);
        return terminateEndEventActivityBehavior;
    }

    // Boundary Events

    public BoundaryEventActivityBehavior createBoundaryEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                             boolean interrupting) {
        return new BoundaryEventActivityBehavior(interrupting);
    }

    public BoundaryCancelEventActivityBehavior createBoundaryCancelEventActivityBehavior(CancelEventDefinition cancelEventDefinition) {
        return new BoundaryCancelEventActivityBehavior();
    }

    public BoundaryCompensateEventActivityBehavior createBoundaryCompensateEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                                                 CompensateEventDefinition compensateEventDefinition,
                                                                                                 boolean interrupting) {

        return new BoundaryCompensateEventActivityBehavior(compensateEventDefinition,
                                                           interrupting);
    }

    public BoundaryTimerEventActivityBehavior createBoundaryTimerEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                                       TimerEventDefinition timerEventDefinition,
                                                                                       boolean interrupting) {
        return new BoundaryTimerEventActivityBehavior(timerEventDefinition,
                                                      interrupting);
    }

    public BoundarySignalEventActivityBehavior createBoundarySignalEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                                         SignalEventDefinition signalEventDefinition,
                                                                                         Signal signal,
                                                                                         boolean interrupting) {
        return new BoundarySignalEventActivityBehavior(signalEventDefinition,
                                                       signal,
                                                       interrupting);
    }

    public BoundaryMessageEventActivityBehavior createBoundaryMessageEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                                           MessageEventDefinition messageEventDefinition,
                                                                                           boolean interrupting) {
        MessageExecutionContext messageExecutionContext = createMessageExecutionContext(boundaryEvent,
                                                                                        messageEventDefinition);
        return new BoundaryMessageEventActivityBehavior(messageEventDefinition,
                                                        interrupting,
                                                        messageExecutionContext);
    }

    @Override
    public IntermediateThrowMessageEventActivityBehavior createThrowMessageEventActivityBehavior(ThrowEvent throwEvent,
                                                                                                 MessageEventDefinition messageEventDefinition,
                                                                                                 Message message) {
        ThrowMessageDelegate throwMessageDelegate = createThrowMessageDelegate(messageEventDefinition);
        MessageExecutionContext messageExecutionContext = createMessageExecutionContext(throwEvent,
                                                                                        messageEventDefinition);
        return new IntermediateThrowMessageEventActivityBehavior(throwEvent,
                                                                 messageEventDefinition,
                                                                 throwMessageDelegate,
                                                                 messageExecutionContext);
    }

    @Override
    public ThrowMessageEndEventActivityBehavior createThrowMessageEndEventActivityBehavior(EndEvent endEvent,
                                                                                           MessageEventDefinition messageEventDefinition,
                                                                                           Message message) {
        ThrowMessageDelegate throwMessageDelegate = createThrowMessageDelegate(messageEventDefinition);
        MessageExecutionContext messageExecutionContext = createMessageExecutionContext(endEvent,
                                                                                        messageEventDefinition);
        return new ThrowMessageEndEventActivityBehavior(endEvent,
                                                        messageEventDefinition,
                                                        throwMessageDelegate,
                                                        messageExecutionContext);
    }

    protected ThrowMessageDelegate createThrowMessageDelegate(MessageEventDefinition messageEventDefinition) {
        Map<String, List<ExtensionAttribute>> attributes = messageEventDefinition.getAttributes();

        return checkClassDelegate(attributes)
                    .map(this::createThrowMessageJavaDelegate).map(Optional::of)
                    .orElseGet(() -> checkDelegateExpression(attributes).map(this::createThrowMessageDelegateExpression))
                    .orElseGet(this::createDefaultThrowMessageDelegate);
    }

    public MessageExecutionContext createMessageExecutionContext(Event bpmnEvent,
                                                                 MessageEventDefinition messageEventDefinition) {
        MessagePayloadMappingProvider mappingProvider = createMessagePayloadMappingProvider(bpmnEvent,
                                                                                            messageEventDefinition);
        return getMessageExecutionContextFactory().create(messageEventDefinition,
                                                          mappingProvider,
                                                          expressionManager);
    }

    public ThrowMessageDelegate createThrowMessageJavaDelegate(String className) {
        Class<? extends ThrowMessageDelegate> clazz = ReflectUtil.loadClass(className)
                                                                 .asSubclass(ThrowMessageDelegate.class);

        return new ThrowMessageJavaDelegate(clazz, emptyList());
    }

    public ThrowMessageDelegate createThrowMessageDelegateExpression(String delegateExpression) {
        Expression expression = expressionManager.createExpression(delegateExpression);

        return new ThrowMessageDelegateExpression(expression, emptyList());
    }

    public ThrowMessageDelegate createDefaultThrowMessageDelegate() {
        return getThrowMessageDelegateFactory().create();
    }

    public MessagePayloadMappingProvider createMessagePayloadMappingProvider(Event bpmnEvent,
                                                                             MessageEventDefinition messageEventDefinition) {
        return getMessagePayloadMappingProviderFactory().create(bpmnEvent,
                                                                messageEventDefinition,
                                                                getExpressionManager());
    }

    protected Optional<String> checkClassDelegate(Map<String, List<ExtensionAttribute>> attributes ) {
        return getAttributeValue(attributes, "class");
    }

    protected Optional<String> checkDelegateExpression(Map<String, List<ExtensionAttribute>> attributes ) {
        return getAttributeValue(attributes, "delegateExpression");
    }

    protected Optional<String> getAttributeValue(Map<String, List<ExtensionAttribute>> attributes,
                                                 String name) {
        return Optional.ofNullable(attributes)
                       .filter(it -> it.containsKey("activiti"))
                       .map(it -> it.get("activiti"))
                       .flatMap(it -> it.stream()
                                        .filter(el -> name.equals(el.getName()))
                                        .findAny())
                       .map(ExtensionAttribute::getValue);
    }
}
