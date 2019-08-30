package org.activiti.spring.boot.process;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiErrorEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.assertj.core.groups.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessErrorEventsIT {

    private static final String ERROR_BOUNDARY_EVENT_SUBPROCESS = "errorBoundaryEventSubProcess";
    private static final String ERROR_START_EVENT_SUBPROCESS = "errorStartEventSubProcess";

    @Autowired
    private ProcessRuntime processRuntime;
    
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;
    
    private List<ActivitiEvent> receivedEvents = new LinkedList<>();

    @Before
    public void init() {
        receivedEvents.clear();
        
        //given
        runtimeService.addEventListener(new ActivitiEventListener() {  
            
            boolean isEventToAdd(ActivitiEvent event) {
                return true;        
            }
            
            @Override
            public void onEvent(ActivitiEvent event) {
                if (isEventToAdd(event)) {
                    receivedEvents.add(event);   
                }     
            }
    
            @Override
            public boolean isFailOnException() {
                return false;
            }            
        }); 
    }

    @After
    public void cleanUp(){
        processCleanUpUtil.cleanUpWithAdmin();
        receivedEvents.clear();
    }

    @Test
    public void testErrorBoundaryEventsSubProcess(){

        //given
        securityUtil.logInAs("user");

        //when
        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(ERROR_BOUNDARY_EVENT_SUBPROCESS)
                        .build());
        
        //then
        assertNotNull(processInstance);
        
        //Error-handling should end the process
        Page<ProcessInstance> processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances().withProcessDefinitionKey(ERROR_BOUNDARY_EVENT_SUBPROCESS)
                        .build());

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(0);
         
        //check events
        assertThat(receivedEvents).isNotEmpty();
        
        Collection<ActivitiEvent> events = receivedEvents
                                            .stream()
                                            .filter(event -> event instanceof ActivitiActivityEvent)
                                            .collect(Collectors.toList());  
        
        assertThat(events)
        .filteredOn(event -> event instanceof ActivitiActivityEvent)
        .extracting(event -> event.getType(),
                    event -> event.getProcessDefinitionId(),
                    event -> event.getProcessInstanceId(),
                    event -> ((ActivitiActivityEvent)event).getActivityType(),
                    event -> ((ActivitiActivityEvent)event).getActivityId()
                    )
        .contains(Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "startEvent",
                              "theStart"),
                  Tuple.tuple(ActivitiEventType.ACTIVITY_COMPLETED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "startEvent",
                              "theStart"),
                  Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "subProcess",
                              "subProcess"), 
                  Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "startEvent",
                              "subStart"),      
                  Tuple.tuple(ActivitiEventType.ACTIVITY_COMPLETED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "startEvent",
                              "subStart"),
                  Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "endEvent",
                              "subEnd"),  
                  Tuple.tuple(ActivitiEventType.ACTIVITY_ERROR_RECEIVED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              null,
                              "catchError"),  
                  Tuple.tuple(ActivitiEventType.ACTIVITY_COMPLETED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "boundaryEvent",
                              "catchError"),
                  Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "endEvent",
                              "errorEnd"),
                  Tuple.tuple(ActivitiEventType.ACTIVITY_COMPLETED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "endEvent",
                              "errorEnd")
                  );
        
        //check ACTIVITY_ERROR_RECEIVED event
        ActivitiErrorEvent e = (ActivitiErrorEvent)receivedEvents
                                                   .stream()
                                                   .filter(event -> event instanceof ActivitiErrorEvent)
                                                   .findAny()
                                                   .orElse(null);
        assertNotNull(e);
        assertThat(e.getErrorCode()).isEqualTo("123");
        assertThat(e.getErrorId()).isEqualTo("errorId");    
    }
    
    @Test
    public void testErrorStartEventsSubProcess(){

        //given
        securityUtil.logInAs("user");

        //when
        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(ERROR_START_EVENT_SUBPROCESS)
                        .build());
        
        //then
        assertNotNull(processInstance);
        
        //Error-handling should end the process
        Page<ProcessInstance> processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances().withProcessDefinitionKey(ERROR_START_EVENT_SUBPROCESS)
                        .build());

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(0);
         
        //check events
        assertThat(receivedEvents).isNotEmpty();
        
        Collection<ActivitiEvent> events = receivedEvents
                                            .stream()
                                            .filter(event -> event instanceof ActivitiActivityEvent)
                                            .collect(Collectors.toList());  
        
        assertThat(events)
        .filteredOn(event -> event instanceof ActivitiActivityEvent)
        .extracting(event -> event.getType(),
                    event -> event.getProcessDefinitionId(),
                    event -> event.getProcessInstanceId(),
                    event -> ((ActivitiActivityEvent)event).getActivityType(),
                    event -> ((ActivitiActivityEvent)event).getActivityId()
                    )
        .contains(Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "startEvent",
                              "theStart"),
                  Tuple.tuple(ActivitiEventType.ACTIVITY_COMPLETED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "startEvent",
                              "theStart"),
                  Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "subProcess",
                              "subProcess"), 
                  Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "startEvent",
                              "subStart"),      
                  Tuple.tuple(ActivitiEventType.ACTIVITY_COMPLETED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "startEvent",
                              "subStart"),
                  Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "endEvent",
                              "subEnd"),  
                  Tuple.tuple(ActivitiEventType.ACTIVITY_ERROR_RECEIVED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              null,
                              "subStart1"),  
                  Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "startEvent",
                              "subStart1"),      
                  Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "manualTask",
                              "task"), 
                  Tuple.tuple(ActivitiEventType.ACTIVITY_COMPLETED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "manualTask",
                              "task"),      
                  Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "endEvent",
                              "subEnd1"),  
                  Tuple.tuple(ActivitiEventType.ACTIVITY_COMPLETED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "endEvent",
                              "subEnd1"),  
                  Tuple.tuple(ActivitiEventType.ACTIVITY_COMPLETED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "eventSubProcess",
                              "errorStartSubProcess"),
                  Tuple.tuple(ActivitiEventType.ACTIVITY_COMPLETED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "subProcess",
                              "subProcess"),
                  Tuple.tuple(ActivitiEventType.ACTIVITY_STARTED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "endEvent",
                              "theEnd"),
                  Tuple.tuple(ActivitiEventType.ACTIVITY_COMPLETED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "endEvent",
                              "theEnd")
                  );
        
        //check ACTIVITY_ERROR_RECEIVED event
        ActivitiErrorEvent e = (ActivitiErrorEvent)receivedEvents
                                                   .stream()
                                                   .filter(event -> event instanceof ActivitiErrorEvent)
                                                   .findAny()
                                                   .orElse(null);
        assertNotNull(e);
        assertThat(e.getErrorCode()).isEqualTo("123");
        assertThat(e.getErrorId()).isEqualTo("errorId");    
    }

}
