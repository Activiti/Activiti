package org.activiti.spring.bpmn.parser;

import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowSignalEventActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.engine.impl.context.Context;
import org.activiti.spring.SpringProcessEngineConfiguration;


public class CloudActivityBehaviorFactory extends DefaultActivityBehaviorFactory {

    public static final String DEFAULT_THROW_SIGNAL_EVENT_BEAN_NAME = "defaultThrowSignalEventBehavior";

    @Override
    public IntermediateThrowSignalEventActivityBehavior createIntermediateThrowSignalEventActivityBehavior(ThrowEvent throwEvent,
                                                                                                           SignalEventDefinition signalEventDefinition,
                                                                                                           Signal signal) {
        SpringProcessEngineConfiguration springProcessEngineConfiguration = (SpringProcessEngineConfiguration) Context.getProcessEngineConfiguration();
        return (IntermediateThrowSignalEventActivityBehavior) springProcessEngineConfiguration.getApplicationContext().getBean(DEFAULT_THROW_SIGNAL_EVENT_BEAN_NAME, springProcessEngineConfiguration.getApplicationContext(), signalEventDefinition, signal);
    }
}
