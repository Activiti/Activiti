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
package org.activiti.engine.impl.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.calendar.DurationBusinessCalendar;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;


public class TimerUtil {

  /**
   * The event definition on which the timer is based.
   *
   * Takes in an optional execution, if missing the {@link NoExecutionVariableScope} will be used (eg Timer start event)
   */
  public static TimerJobEntity createTimerEntityForTimerEventDefinition(TimerEventDefinition timerEventDefinition, boolean isInterruptingTimer,
      ExecutionEntity executionEntity, String jobHandlerType, String jobHandlerConfig) {

    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    String businessCalendarRef = null;
    Expression expression = null;
    ExpressionManager expressionManager = processEngineConfiguration.getExpressionManager();

    // ACT-1415: timer-declaration on start-event may contain expressions NOT
    // evaluating variables but other context, evaluating should happen nevertheless
    VariableScope scopeForExpression = executionEntity;
    if (scopeForExpression == null) {
      scopeForExpression = NoExecutionVariableScope.getSharedInstance();
    }

    if (StringUtils.isNotEmpty(timerEventDefinition.getTimeDate())) {

      businessCalendarRef = DueDateBusinessCalendar.NAME;
      expression = expressionManager.createExpression(timerEventDefinition.getTimeDate());

    } else if (StringUtils.isNotEmpty(timerEventDefinition.getTimeCycle())) {

      businessCalendarRef = CycleBusinessCalendar.NAME;
      expression = expressionManager.createExpression(timerEventDefinition.getTimeCycle());

    } else if (StringUtils.isNotEmpty(timerEventDefinition.getTimeDuration())) {

      businessCalendarRef = DurationBusinessCalendar.NAME;
      expression = expressionManager.createExpression(timerEventDefinition.getTimeDuration());
    }

    if (StringUtils.isNotEmpty(timerEventDefinition.getCalendarName())) {
      businessCalendarRef = timerEventDefinition.getCalendarName();
      Expression businessCalendarExpression = expressionManager.createExpression(businessCalendarRef);
      businessCalendarRef = businessCalendarExpression.getValue(scopeForExpression).toString();
    }

    if (expression == null) {
      throw new ActivitiException("Timer needs configuration (either timeDate, timeCycle or timeDuration is needed) (" + timerEventDefinition.getId() + ")");
    }

    BusinessCalendar businessCalendar = processEngineConfiguration.getBusinessCalendarManager().getBusinessCalendar(businessCalendarRef);

    String dueDateString = null;
    Date duedate = null;

    Object dueDateValue = expression.getValue(scopeForExpression);
    if (dueDateValue instanceof String) {
      dueDateString = (String) dueDateValue;

    } else if (dueDateValue instanceof Date) {
      duedate = (Date) dueDateValue;

    } else if (dueDateValue instanceof DateTime) {
      //JodaTime support
      duedate = ((DateTime) dueDateValue).toDate();

    } else if (dueDateValue != null) {
      throw new ActivitiException("Timer '" + executionEntity.getActivityId()
          + "' was not configured with a valid duration/time, either hand in a java.util.Date or a String in format 'yyyy-MM-dd'T'hh:mm:ss'");
    }

    if (duedate == null && dueDateString != null) {
      duedate = businessCalendar.resolveDuedate(dueDateString);
    }

    TimerJobEntity timer = null;
    if (duedate != null) {
      timer = Context.getCommandContext().getTimerJobEntityManager().create();
      timer.setJobType(JobEntity.JOB_TYPE_TIMER);
      timer.setRevision(1);
      timer.setJobHandlerType(jobHandlerType);
      timer.setJobHandlerConfiguration(jobHandlerConfig);
      timer.setExclusive(true);
      timer.setRetries(processEngineConfiguration.getAsyncExecutorNumberOfRetries());
      timer.setDuedate(duedate);
      if (executionEntity != null) {
        timer.setExecution(executionEntity);
        timer.setProcessDefinitionId(executionEntity.getProcessDefinitionId());
        timer.setProcessInstanceId(executionEntity.getProcessInstanceId());

        // Inherit tenant identifier (if applicable)
        if (executionEntity.getTenantId() != null) {
          timer.setTenantId(executionEntity.getTenantId());
        }
      }
    }

    if (StringUtils.isNotEmpty(timerEventDefinition.getTimeCycle())) {
      // See ACT-1427: A boundary timer with a cancelActivity='true', doesn't need to repeat itself
      boolean repeat = !isInterruptingTimer;

      // ACT-1951: intermediate catching timer events shouldn't repeat according to spec
      if (executionEntity != null) {
        FlowElement currentElement = executionEntity.getCurrentFlowElement();
        if (currentElement instanceof IntermediateCatchEvent) {
          repeat = false;
        }
      }

      if (repeat) {
        String prepared = prepareRepeat(dueDateString);
        timer.setRepeat(prepared);
      }
    }

    if (timer != null && executionEntity != null) {
      timer.setExecution(executionEntity);
      timer.setProcessDefinitionId(executionEntity.getProcessDefinitionId());

      // Inherit tenant identifier (if applicable)
      if (executionEntity.getTenantId() != null) {
        timer.setTenantId(executionEntity.getTenantId());
      }
    }

    return timer;
  }

  public static String prepareRepeat(String dueDate) {
    if (dueDate.startsWith("R") && dueDate.split("/").length == 2) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      return dueDate.replace("/", "/" + sdf.format(Context.getProcessEngineConfiguration().getClock().getCurrentTime()) + "/");
    }
    return dueDate;
  }

}
