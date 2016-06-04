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
package org.activiti.engine.impl.bpmn.parser.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationType;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Joram Barrez
 */
public class TimerEventDefinitionParseHandler extends AbstractBpmnParseHandler<TimerEventDefinition> {

	private static final Logger logger = LoggerFactory.getLogger(TimerEventDefinitionParseHandler.class);

  public static final String PROPERTYNAME_START_TIMER = "timerStart";

  public Class< ? extends BaseElement> getHandledType() {
    return TimerEventDefinition.class;
  }

  protected void executeParse(BpmnParse bpmnParse, TimerEventDefinition timerEventDefinition) {

    ActivityImpl timerActivity = bpmnParse.getCurrentActivity();
    if (bpmnParse.getCurrentFlowElement() instanceof StartEvent) {

      ProcessDefinitionEntity processDefinition = bpmnParse.getCurrentProcessDefinition();
      timerActivity.setProperty("type", "startTimerEvent");
      TimerDeclarationImpl timerDeclaration = createTimer(bpmnParse, timerEventDefinition, timerActivity, TimerStartEventJobHandler.TYPE);

      String jobHandlerConfiguration = timerDeclaration.getJobHandlerConfiguration();
      Map<String, JobHandler> jobHandlers = Context.getProcessEngineConfiguration().getJobHandlers();
      JobHandler jobHandler = jobHandlers.get(TimerStartEventJobHandler.TYPE);
      jobHandlerConfiguration = ((TimerEventHandler) jobHandler).setProcessDefinitionKeyToConfiguration(jobHandlerConfiguration, processDefinition.getKey());
      jobHandlerConfiguration = ((TimerEventHandler) jobHandler).setActivityIdToConfiguration(jobHandlerConfiguration, timerActivity.getId());
      timerDeclaration.setJobHandlerConfiguration(jobHandlerConfiguration);


      List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) processDefinition.getProperty(PROPERTYNAME_START_TIMER);
      if (timerDeclarations == null) {
        timerDeclarations = new ArrayList<TimerDeclarationImpl>();
        processDefinition.setProperty(PROPERTYNAME_START_TIMER, timerDeclarations);
      }
      timerDeclarations.add(timerDeclaration);

    } else if (bpmnParse.getCurrentFlowElement() instanceof IntermediateCatchEvent) {

      timerActivity.setProperty("type", "intermediateTimer");
      TimerDeclarationImpl timerDeclaration = createTimer(bpmnParse, timerEventDefinition, timerActivity, TimerCatchIntermediateEventJobHandler.TYPE);
      if (getPrecedingEventBasedGateway(bpmnParse, (IntermediateCatchEvent) bpmnParse.getCurrentFlowElement()) != null) {
        addTimerDeclaration(timerActivity.getParent(), timerDeclaration);
      } else {
        addTimerDeclaration(timerActivity, timerDeclaration);
        timerActivity.setScope(true);
      }

    } else if (bpmnParse.getCurrentFlowElement() instanceof BoundaryEvent) {

      timerActivity.setProperty("type", "boundaryTimer");
      TimerDeclarationImpl timerDeclaration = createTimer(bpmnParse, timerEventDefinition, timerActivity, TimerExecuteNestedActivityJobHandler.TYPE);

      // ACT-1427
      BoundaryEvent boundaryEvent = (BoundaryEvent) bpmnParse.getCurrentFlowElement();
      boolean interrupting = boundaryEvent.isCancelActivity();
      if (interrupting) {
        timerDeclaration.setInterruptingTimer(true);
      }

      addTimerDeclaration(timerActivity.getParent(), timerDeclaration);

      if (timerActivity.getParent() instanceof ActivityImpl) {
        ((ActivityImpl) timerActivity.getParent()).setScope(true);
      }

      timerActivity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory()
              .createBoundaryEventActivityBehavior((BoundaryEvent) bpmnParse.getCurrentFlowElement(), interrupting, timerActivity));

    }
  }

  protected TimerDeclarationImpl createTimer(BpmnParse bpmnParse, TimerEventDefinition timerEventDefinition, ScopeImpl timerActivity, String jobHandlerType) {
    TimerDeclarationType type = null;
    Expression expression = null;
    Expression endDate = null;
    Expression calendarName = null;
    ExpressionManager expressionManager = bpmnParse.getExpressionManager();
    if (StringUtils.isNotEmpty(timerEventDefinition.getTimeDate())) {
      // TimeDate
      type = TimerDeclarationType.DATE;
      expression = expressionManager.createExpression(timerEventDefinition.getTimeDate());
    } else if (StringUtils.isNotEmpty(timerEventDefinition.getTimeCycle())) {
      // TimeCycle
      type = TimerDeclarationType.CYCLE;
      expression = expressionManager.createExpression(timerEventDefinition.getTimeCycle());
      //support for endDate
      if (StringUtils.isNotEmpty(timerEventDefinition.getEndDate())) {
        endDate = expressionManager.createExpression(timerEventDefinition.getEndDate());
      }
    } else if (StringUtils.isNotEmpty(timerEventDefinition.getTimeDuration())) {
      // TimeDuration
      type = TimerDeclarationType.DURATION;
      expression = expressionManager.createExpression(timerEventDefinition.getTimeDuration());
    }
    if (StringUtils.isNotEmpty(timerEventDefinition.getCalendarName())) {
      calendarName = expressionManager.createExpression(timerEventDefinition.getCalendarName());
    }

    // neither date, cycle or duration configured!
    if (expression == null) {
      logger.warn("Timer needs configuration (either timeDate, timeCycle or timeDuration is needed) (" + timerActivity.getId() + ")");
    }


    String jobHandlerConfiguration = timerActivity.getId();

    if (jobHandlerType.equalsIgnoreCase(TimerExecuteNestedActivityJobHandler.TYPE) ||
            jobHandlerType.equalsIgnoreCase(TimerCatchIntermediateEventJobHandler.TYPE) ||
            jobHandlerType.equalsIgnoreCase(TimerStartEventJobHandler.TYPE)) {
      jobHandlerConfiguration = TimerStartEventJobHandler.createConfiguration(timerActivity.getId(), endDate, calendarName);
    }

    // Parse the timer declaration
    // TODO move the timer declaration into the bpmn activity or next to the
    // TimerSession
    TimerDeclarationImpl timerDeclaration = new TimerDeclarationImpl(expression, type, jobHandlerType , endDate, calendarName);
    timerDeclaration.setJobHandlerConfiguration(jobHandlerConfiguration);

    timerDeclaration.setExclusive(true);
    return timerDeclaration;
  }

  @SuppressWarnings("unchecked")
  protected void addTimerDeclaration(ScopeImpl scope, TimerDeclarationImpl timerDeclaration) {
    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) scope.getProperty(PROPERTYNAME_TIMER_DECLARATION);
    if (timerDeclarations == null) {
      timerDeclarations = new ArrayList<TimerDeclarationImpl>();
      scope.setProperty(PROPERTYNAME_TIMER_DECLARATION, timerDeclarations);
    }
    timerDeclarations.add(timerDeclaration);
  }

}
