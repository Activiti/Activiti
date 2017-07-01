package org.activiti.services.audit.producer.app;

import org.activiti.engine.delegate.event.ActivitiCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiProcessStartedEvent;
import org.activiti.engine.delegate.event.impl.ActivitiActivityEventImpl;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.delegate.event.impl.ActivitiVariableEventImpl;
import org.activiti.engine.task.Task;
import org.activiti.services.audit.producer.app.events.ActivityStartedEventImpl;
import org.activiti.services.audit.producer.app.events.ProcessCancelledEventImpl;
import org.activiti.services.model.events.ProcessEngineEvent;
import org.activiti.services.audit.producer.app.events.ProcessStartedEventImpl;
import org.activiti.services.audit.producer.app.events.TaskCreatedEventImpl;
import org.activiti.services.audit.producer.app.events.VariableCreatedEventImpl;
import org.activiti.services.model.converter.TaskConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class MessageProducerActivitiEventListener implements ActivitiEventListener {

    @Autowired
    private AuditProducerChannels producer;

    @Autowired
    private TaskConverter taskConverter;

    @Override
    public void onEvent(ActivitiEvent event) {
        ProcessEngineEvent newEvent = null;
        switch (event.getType()) {
            case PROCESS_STARTED:
                newEvent = new ProcessStartedEventImpl(event.getExecutionId(),
                                                       event.getProcessDefinitionId(),
                                                       event.getProcessInstanceId(),
                                                       ((ActivitiProcessStartedEvent) event).getNestedProcessDefinitionId(),
                                                       ((ActivitiProcessStartedEvent) event).getNestedProcessInstanceId());
                break;
            //((ActivitiProcessStartedEvent)event).getVariables()
            case PROCESS_CANCELLED:
                newEvent = new ProcessCancelledEventImpl(event.getExecutionId(),
                                                         event.getProcessDefinitionId(),
                                                         event.getProcessInstanceId(),
                                                         ((ActivitiCancelledEvent) event).getCause().toString());
                break;
            case PROCESS_COMPLETED:
                System.out.println(event.getType() + "---> Completed??? " + event.getClass().getCanonicalName());
                break;
            case PROCESS_COMPLETED_WITH_ERROR_END_EVENT:
                System.out.println(event.getType() + "---> Completed With Error??? " + event.getClass().getCanonicalName());
                break;
            case ACTIVITY_STARTED:
                newEvent = new ActivityStartedEventImpl(event.getExecutionId(),
                                                        event.getProcessDefinitionId(),
                                                        event.getProcessInstanceId(),
                                                        ((ActivitiActivityEventImpl) event).getActivityId(),
                                                        ((ActivitiActivityEventImpl) event).getActivityName(),
                                                        ((ActivitiActivityEventImpl) event).getActivityType());

                break;
            case ACTIVITY_CANCELLED:
                System.out.println(event.getType() + "---> Activity Cancelled??? " + event.getClass().getCanonicalName());
                break;
            case ACTIVITY_COMPENSATE:
                System.out.println(event.getType() + "---> Activity Compensate??? " + event.getClass().getCanonicalName());
                break;
            case TASK_CREATED:
                System.out.println("Task Created : " + ((ActivitiEntityEventImpl) event).getEntity().getClass().getCanonicalName());

                newEvent = new TaskCreatedEventImpl(event.getExecutionId(),
                                                    event.getProcessDefinitionId(),
                                                    event.getProcessInstanceId(),
                                                    taskConverter.from((Task) ((ActivitiEntityEventImpl) event).getEntity()));
                break;
            case TASK_ASSIGNED:
                System.out.println(event.getType() + "---> Task Assigned??? " + event.getClass().getCanonicalName());
                break;
            case TASK_COMPLETED:
                System.out.println(event.getType() + "---> Task Completed??? " + event.getClass().getCanonicalName());
                break;
            case VARIABLE_CREATED:
                newEvent = new VariableCreatedEventImpl(event.getExecutionId(),
                                                        event.getProcessDefinitionId(),
                                                        event.getProcessInstanceId(),
                                                        ((ActivitiVariableEventImpl) event).getVariableName(),
                                                        ((ActivitiVariableEventImpl) event).getVariableValue().toString(),
                                                        ((ActivitiVariableEventImpl) event).getVariableType().getTypeName(),
                                                        ((ActivitiVariableEventImpl) event).getTaskId());

                break;
            case VARIABLE_DELETED:
                System.out.println(event.getType() + "---> Variable Deleted??? " + event.getClass().getCanonicalName());
                break;
            case VARIABLE_UPDATED:
                System.out.println(event.getType() + "---> Variable Updated??? " + event.getClass().getCanonicalName());
                break;
        }
        if (newEvent != null) {
            producer.auditProducer().send(MessageBuilder.withPayload(newEvent).build());
        } else {
            System.out.println("No suitable event found for: " + event);
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
