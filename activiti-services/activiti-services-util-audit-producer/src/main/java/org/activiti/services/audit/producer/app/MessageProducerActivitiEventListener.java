package org.activiti.services.audit.producer.app;

import org.activiti.engine.delegate.event.ActivitiCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiProcessStartedEvent;
import org.activiti.engine.delegate.event.impl.ActivitiActivityEventImpl;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.delegate.event.impl.ActivitiSequenceFlowTakenEventImpl;
import org.activiti.engine.delegate.event.impl.ActivitiVariableEventImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.task.Task;
import org.activiti.services.audit.producer.app.events.ActivityCompletedEventImpl;
import org.activiti.services.audit.producer.app.events.ActivityStartedEventImpl;
import org.activiti.services.audit.producer.app.events.ProcessCancelledEventImpl;
import org.activiti.services.audit.producer.app.events.ProcessCompletedEventImpl;
import org.activiti.services.audit.producer.app.events.ProcessStartedEventImpl;
import org.activiti.services.audit.producer.app.events.SequenceFlowTakenEvent;
import org.activiti.services.audit.producer.app.events.SequenceFlowTakenEventImpl;
import org.activiti.services.audit.producer.app.events.TaskAssignedEvent;
import org.activiti.services.audit.producer.app.events.TaskAssignedEventImpl;
import org.activiti.services.audit.producer.app.events.TaskCompletedEventImpl;
import org.activiti.services.audit.producer.app.events.TaskCreatedEventImpl;
import org.activiti.services.audit.producer.app.events.VariableCreatedEventImpl;
import org.activiti.services.audit.producer.app.events.VariableDeletedEventImpl;
import org.activiti.services.audit.producer.app.events.VariableUpdatedEventImpl;
import org.activiti.services.model.converter.ProcessInstanceConverter;
import org.activiti.services.model.converter.TaskConverter;
import org.activiti.services.model.events.ProcessEngineEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class MessageProducerActivitiEventListener implements ActivitiEventListener {

    @Autowired
    private AuditProducerChannels producer;

    @Autowired
    private TaskConverter taskConverter;

    @Autowired
    private ProcessInstanceConverter processInstanceConverter;

    @Override
    public void onEvent(ActivitiEvent event) {
        ProcessEngineEvent newEvent = null;
        switch (event.getType()) {
            case PROCESS_STARTED:
                System.out.println("Process Started: " + event.getClass().getCanonicalName());
                newEvent = new ProcessStartedEventImpl(event.getExecutionId(),
                                                       event.getProcessDefinitionId(),
                                                       event.getProcessInstanceId(),
                                                       ((ActivitiProcessStartedEvent) event).getNestedProcessDefinitionId(),
                                                       ((ActivitiProcessStartedEvent) event).getNestedProcessInstanceId());
                break;
            case PROCESS_CANCELLED:
                newEvent = new ProcessCancelledEventImpl(event.getExecutionId(),
                                                         event.getProcessDefinitionId(),
                                                         event.getProcessInstanceId(),
                                                         ((ActivitiCancelledEvent) event).getCause().toString());
                break;
            case PROCESS_COMPLETED:
                System.out.println(event.getType() + "---> Completed??? " + event.getClass().getCanonicalName());
                System.out.println("Completed Entity: " + ((ActivitiEntityEventImpl)event).getEntity().getClass().getCanonicalName());
                newEvent = new ProcessCompletedEventImpl(event.getExecutionId(),
                                                         event.getProcessDefinitionId(),
                                                         event.getProcessInstanceId(),
                                                         processInstanceConverter.from(((ExecutionEntityImpl)((ActivitiEntityEventImpl)event).getEntity()).getProcessInstance()));

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
            case ACTIVITY_COMPLETED:
                System.out.println(event.getType() + "---> Activity Completed " + event.getClass().getCanonicalName());
                newEvent = new ActivityCompletedEventImpl(event.getExecutionId(),
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

                System.out.println("Task Assigned : " + ((ActivitiEntityEventImpl) event).getEntity().getClass().getCanonicalName());

                newEvent = new TaskAssignedEventImpl(event.getExecutionId(),
                                                     event.getProcessDefinitionId(),
                                                     event.getProcessInstanceId(),
                                                     taskConverter.from((Task) ((ActivitiEntityEventImpl) event).getEntity()));
                break;
            case TASK_COMPLETED:
                System.out.println(event.getType() + "---> Task Completed??? " + event.getClass().getCanonicalName());
                newEvent = new TaskCompletedEventImpl(event.getExecutionId(),
                                                      event.getProcessDefinitionId(),
                                                      event.getProcessInstanceId(),
                                                      taskConverter.from((Task) ((ActivitiEntityEventImpl) event).getEntity()));

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
                newEvent = new VariableDeletedEventImpl(event.getExecutionId(),
                                                        event.getProcessDefinitionId(),
                                                        event.getProcessInstanceId(),
                                                        ((ActivitiVariableEventImpl) event).getVariableName(),
                                                        ((ActivitiVariableEventImpl) event).getVariableType().getTypeName(),
                                                        ((ActivitiVariableEventImpl) event).getTaskId());
                break;
            case VARIABLE_UPDATED:
                System.out.println(event.getType() + "---> Variable Updated??? " + event.getClass().getCanonicalName());
                newEvent = new VariableUpdatedEventImpl(event.getExecutionId(),
                                                        event.getProcessDefinitionId(),
                                                        event.getProcessInstanceId(),
                                                        ((ActivitiVariableEventImpl) event).getVariableName(),
                                                        ((ActivitiVariableEventImpl) event).getVariableValue().toString(),
                                                        ((ActivitiVariableEventImpl) event).getVariableType().getTypeName(),
                                                        ((ActivitiVariableEventImpl) event).getTaskId());

                break;
            case SEQUENCEFLOW_TAKEN:
                System.out.println(event.getType() + "---> Sequence Flow Taken??? " + event.getClass().getCanonicalName());
                newEvent = new SequenceFlowTakenEventImpl(event.getExecutionId(),
                                                          event.getProcessDefinitionId(),
                                                          event.getProcessInstanceId(),
                                                          ((ActivitiSequenceFlowTakenEventImpl)event).getId(),
                                                          ((ActivitiSequenceFlowTakenEventImpl)event).getSourceActivityId(),
                                                          ((ActivitiSequenceFlowTakenEventImpl)event).getSourceActivityName(),
                                                          ((ActivitiSequenceFlowTakenEventImpl)event).getSourceActivityType(),
                                                          ((ActivitiSequenceFlowTakenEventImpl)event).getTargetActivityId(),
                                                          ((ActivitiSequenceFlowTakenEventImpl)event).getTargetActivityName(),
                                                          ((ActivitiSequenceFlowTakenEventImpl)event).getTargetActivityType());
                break;
            default:
                System.out.println(" -----> "+event.getType() + " we are ommiting this event type, fix this right now :) ");
                System.out.println(" ------> Class for the ommited event"+ event.getClass().getCanonicalName());
        }
        if (newEvent != null) {
            producer.auditProducer().send(MessageBuilder.withPayload(newEvent).build());
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
