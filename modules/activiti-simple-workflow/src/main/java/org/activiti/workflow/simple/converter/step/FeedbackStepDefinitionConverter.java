/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.workflow.simple.converter.step;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.FeedbackStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.util.JvmUtil;


/**
 * @author Joram Barrez
 */
public class FeedbackStepDefinitionConverter extends BaseStepDefinitionConverter<FeedbackStepDefinition, Map<String, BaseElement>> {
  
  private static final long serialVersionUID = 1L;
  
	private static final String SELECT_PEOPLE_USER_TASK = "initiatorSelectPeopleTask";
  private static final String FEEDBACK_FORK = "feedbackFork";
  private static final String FEEDBACK_JOIN = "feedbackJoin";
  private static final String FEEDBACK_USER_TASK = "gatherFeedback";
  
  public static final String VARIABLE_FEEDBACK_PROVIDERS = "feedbackProviders";
  public static final String VARIABLE_FEEDBACK_PROVIDER = "feedbackProvider";

  @Override
  public Class< ? extends StepDefinition> getHandledClass() {
    return FeedbackStepDefinition.class;
  }

  @Override
  protected Map<String, BaseElement> createProcessArtifact(FeedbackStepDefinition feedbackStepDefinition, WorkflowDefinitionConversion conversion) {

    // See feedback-step.png in the resource folder to get a graphical understanding of the conversion below
    
    Map<String, BaseElement> processElements = new HashMap<String, BaseElement>();
    
    // The first user task, responsible for configuring the feedback
    UserTask selectPeopleUserTask = createSelectPeopleUserTask(feedbackStepDefinition, conversion, processElements);
    
    // Parallel gateways (forking/joining)
    ParallelGateway fork = createForkParallelGateway(conversion, processElements);
    addSequenceFlow(conversion, selectPeopleUserTask, fork);
    
    // Gather feedback user task for the initiator of the feedback step
    UserTask gatherFeedbackUserTask = createGatherFeedbackUserTask(feedbackStepDefinition, conversion, processElements);
    addSequenceFlow(conversion, fork, gatherFeedbackUserTask);
    
    // Global signal event
    Signal signal = createSignalDeclaration(conversion);
    
    // Signal throw event after the gather feedback task
    ThrowEvent signalThrowEvent = createSignalThrow(conversion, signal);
    addSequenceFlow(conversion, gatherFeedbackUserTask, signalThrowEvent);
    
    // Povide feedback step
    UserTask feedbackTask = createFeedbackUserTask(feedbackStepDefinition, conversion, processElements);
    addSequenceFlow(conversion, fork, feedbackTask);
    
    // Boundary signal catch to shut down all tasks if the 'gather feedback' task is completed
    BoundaryEvent boundarySignalCatch = createBoundarySignalCatch(conversion, signal, feedbackTask);
    
    // Exclusive gateway after the feedback task, needed to correctly merge the sequence flow
    // such that the joining parallel gateway has exactly two incoming sequence flow
    ExclusiveGateway mergingExclusiveGateway = createMergingExclusiveGateway(conversion);
    addSequenceFlow(conversion, feedbackTask, mergingExclusiveGateway);
    addSequenceFlow(conversion, boundarySignalCatch, mergingExclusiveGateway);
    
    // Parallel gateway that will join  it all together
    ParallelGateway join = createJoinParallelGateway(conversion, processElements); 
    addSequenceFlow(conversion, signalThrowEvent, join);
    addSequenceFlow(conversion, mergingExclusiveGateway, join);
    
    // Set the last activity id, such that next steps can connect correctly
    conversion.setLastActivityId(join.getId());
    
    return processElements;
  }

  protected UserTask createSelectPeopleUserTask(FeedbackStepDefinition feedbackStepDefinition, WorkflowDefinitionConversion conversion,
          Map<String, BaseElement> processElements) {
    UserTask selectPeopleUserTask = new UserTask();
    selectPeopleUserTask.setId(conversion.getUniqueNumberedId(ConversionConstants.USER_TASK_ID_PREFIX));
    selectPeopleUserTask.setName(getSelectPeopleTaskName());
    selectPeopleUserTask.setAssignee(feedbackStepDefinition.getFeedbackInitiator());
    addFlowElement(conversion, selectPeopleUserTask, true);
    processElements.put(SELECT_PEOPLE_USER_TASK, selectPeopleUserTask);
    
    // TODO: work out form such that it can be used in Activiti Explorer, ie. add correct form properties
    // The following is just a a bit of a dummy form property
    FormProperty feedbackProvidersProperty = new FormProperty();
    feedbackProvidersProperty.setId(VARIABLE_FEEDBACK_PROVIDERS);
    feedbackProvidersProperty.setName("Who needs to provide feedback?");
    feedbackProvidersProperty.setRequired(true);
    feedbackProvidersProperty.setType("string"); // TODO: we need some kind of 'people' property type here
    
    selectPeopleUserTask.setFormProperties(Arrays.asList(feedbackProvidersProperty));
    
    // When the list of feedback providers is fixed up front, we need to add a script listener
    // that injects these variables into the process (instead of having it provided by the end user in a form)
    if (feedbackStepDefinition.getFeedbackProviders() != null && !feedbackStepDefinition.getFeedbackProviders()
                                                                                        .isEmpty()) {
      if (selectPeopleUserTask.getTaskListeners() == null) 
      {
        selectPeopleUserTask.setTaskListeners(new ArrayList<ActivitiListener>());
      }
      
      ActivitiListener taskListener = new ActivitiListener();
      taskListener.setEvent("complete");
      taskListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
      taskListener.setImplementation("org.activiti.engine.impl.bpmn.listener.ScriptTaskListener");
      
      FieldExtension languageField = new FieldExtension();
      languageField.setFieldName("language");
      languageField.setStringValue("JavaScript");
      
      FieldExtension scriptField = new FieldExtension();
      scriptField.setFieldName("script");
      
      StringBuilder script = new StringBuilder();
      if (JvmUtil.isJDK8()) {
      	script.append("load(\"nashorn:mozilla_compat.js\");");
      }
      script.append("importPackage (java.util); var feedbackProviders = new ArrayList();" + System.getProperty("line.separator"));
      for (String feedbackProvider : feedbackStepDefinition.getFeedbackProviders()) {
        script.append("feedbackProviders.add('" + feedbackProvider + "');" + System.getProperty("line.separator"));
      }
      script.append("task.getExecution().setVariable('" + VARIABLE_FEEDBACK_PROVIDERS + "', feedbackProviders);" + System.getProperty("line.separator"));
      scriptField.setStringValue(script.toString());

      taskListener.setFieldExtensions(Arrays.asList(languageField, scriptField));
      
      selectPeopleUserTask.getTaskListeners().add(taskListener);
    }
    
    
    return selectPeopleUserTask;
  }

  protected ParallelGateway createForkParallelGateway(WorkflowDefinitionConversion conversion, Map<String, BaseElement> processElements) {
    ParallelGateway fork = new ParallelGateway();
    fork.setId(conversion.getUniqueNumberedId(ConversionConstants.GATEWAY_ID_PREFIX));
    addFlowElement(conversion, fork);
    processElements.put(FEEDBACK_FORK, fork);
    return fork;
  }

  protected UserTask createGatherFeedbackUserTask(FeedbackStepDefinition feedbackStepDefinition, WorkflowDefinitionConversion conversion,
          Map<String, BaseElement> processElements) {
    UserTask gatherFeedbackUserTask = new UserTask();
    gatherFeedbackUserTask.setId(conversion.getUniqueNumberedId(ConversionConstants.USER_TASK_ID_PREFIX));
    gatherFeedbackUserTask.setName(getGatherFeedbackTaskName());
    gatherFeedbackUserTask.setAssignee(feedbackStepDefinition.getFeedbackInitiator());
    addFlowElement(conversion, gatherFeedbackUserTask);
    processElements.put(SELECT_PEOPLE_USER_TASK, gatherFeedbackUserTask);
    return gatherFeedbackUserTask;
  }

  protected Signal createSignalDeclaration(WorkflowDefinitionConversion conversion) {
    Signal signal = new Signal();
    String uniqueSignalId = "signal-" + UUID.randomUUID().toString();
    signal.setId(uniqueSignalId);
    signal.setName(uniqueSignalId);
    signal.setScope(Signal.SCOPE_PROCESS_INSTANCE);
    
    conversion.getBpmnModel().addSignal(signal);
    
    return signal;
  }

  protected ThrowEvent createSignalThrow(WorkflowDefinitionConversion conversion, Signal signal) {
    ThrowEvent signalThrowEvent = new ThrowEvent();
    signalThrowEvent.setId(conversion.getUniqueNumberedId(ConversionConstants.EVENT_ID_PREFIX));
    
    SignalEventDefinition signalThrowEventDefinition = new SignalEventDefinition();
    signalThrowEventDefinition.setSignalRef(signal.getId());
    signalThrowEvent.addEventDefinition(signalThrowEventDefinition);
    
    addFlowElement(conversion, signalThrowEvent);
    
    return signalThrowEvent;
  }

  protected ParallelGateway createJoinParallelGateway(WorkflowDefinitionConversion conversion, Map<String, BaseElement> processElements) {
    ParallelGateway join = new ParallelGateway();
    join.setId(conversion.getUniqueNumberedId(ConversionConstants.GATEWAY_ID_PREFIX));
    addFlowElement(conversion, join);
    processElements.put(FEEDBACK_JOIN, join);
    return join;
  }

  protected UserTask createFeedbackUserTask(FeedbackStepDefinition feedbackStepDefinition, WorkflowDefinitionConversion conversion,
          Map<String, BaseElement> processElements) {
    UserTask feedbackTask = new UserTask();
    feedbackTask.setId(conversion.getUniqueNumberedId(ConversionConstants.USER_TASK_ID_PREFIX));
    feedbackTask.setName(getProvideFeedbackTaskName());
    feedbackTask.setAssignee("${" + VARIABLE_FEEDBACK_PROVIDER + "}");
    
    MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();
    multiInstanceLoopCharacteristics.setSequential(false);
    multiInstanceLoopCharacteristics.setInputDataItem(VARIABLE_FEEDBACK_PROVIDERS);
    multiInstanceLoopCharacteristics.setElementVariable(VARIABLE_FEEDBACK_PROVIDER);
    feedbackTask.setLoopCharacteristics(multiInstanceLoopCharacteristics);
    
    addFlowElement(conversion, feedbackTask);
    processElements.put(FEEDBACK_USER_TASK, feedbackTask);
    return feedbackTask;
  }

  protected BoundaryEvent createBoundarySignalCatch(WorkflowDefinitionConversion conversion, Signal signal, UserTask feedbackTask) {
    BoundaryEvent boundarySignalCatch = new BoundaryEvent();
    boundarySignalCatch.setId(conversion.getUniqueNumberedId(ConversionConstants.BOUNDARY_ID_PREFIX));
    boundarySignalCatch.setAttachedToRef(feedbackTask);
    boundarySignalCatch.setAttachedToRefId(feedbackTask.getId());
    boundarySignalCatch.setCancelActivity(true);
    addFlowElement(conversion, boundarySignalCatch);
    
    SignalEventDefinition signalCatchEventDefinition = new SignalEventDefinition();
    signalCatchEventDefinition.setSignalRef(signal.getId());
    boundarySignalCatch.addEventDefinition(signalCatchEventDefinition);
    return boundarySignalCatch;
  }

  protected ExclusiveGateway createMergingExclusiveGateway(WorkflowDefinitionConversion conversion) {
    ExclusiveGateway mergingExclusiveGateway = new ExclusiveGateway();
    mergingExclusiveGateway.setId(conversion.getUniqueNumberedId(ConversionConstants.GATEWAY_ID_PREFIX));
    addFlowElement(conversion, mergingExclusiveGateway);
    return mergingExclusiveGateway;
  }
  
  
  // The following are default task names and can be overidden by subclasses
  
  protected String getSelectPeopleTaskName() {
    return "Choose people";
  }
  
  protected String getProvideFeedbackTaskName() {
    return "Provide feedback";
  }
  
  protected String getGatherFeedbackTaskName() {
    return "Gather feedback";
  }
  
}
