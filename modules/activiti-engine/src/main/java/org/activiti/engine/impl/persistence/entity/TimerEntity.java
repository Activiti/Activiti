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
package org.activiti.engine.impl.persistence.entity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tom Baeyens
 */
public class TimerEntity extends JobEntity {

  private static final long serialVersionUID = 1L;

  private static Logger log = LoggerFactory.getLogger(TimerEntity.class);

  protected int maxIterations;
  protected String repeat;
  protected Date endDate;

  public TimerEntity() {
    super();
    this.jobType = "timer";
  }

  public TimerEntity(TimerDeclarationImpl timerDeclaration) {
    jobHandlerType = timerDeclaration.getJobHandlerType();
    jobHandlerConfiguration = timerDeclaration.getJobHandlerConfiguration();
    isExclusive = timerDeclaration.isExclusive();
    repeat = timerDeclaration.getRepeat();
    retries = timerDeclaration.getRetries();
    this.jobType = "timer";
  }

  private TimerEntity(TimerEntity te) {
    jobHandlerConfiguration = te.jobHandlerConfiguration;
    jobHandlerType = te.jobHandlerType;
    isExclusive = te.isExclusive;
    repeat = te.repeat;
    retries = te.retries;
    endDate = te.endDate;
    executionId = te.executionId;
    processInstanceId = te.processInstanceId;
    processDefinitionId = te.processDefinitionId;

    // Inherit tenant
    tenantId = te.tenantId;
    this.jobType = "timer";
  }

  @Override
  public void execute(CommandContext commandContext) {

    //set endDate if it was set to the definition
    restoreExtraData(commandContext, jobHandlerConfiguration);

    if (this.getDuedate() != null && !isValidTime(this.getDuedate())) {
      if (log.isDebugEnabled()) {
        log.debug("Timer {} fired. but the dueDate is after the endDate.  Deleting timer.", getId());
      }
      delete();
      return;
    }

    super.execute(commandContext);

    if (log.isDebugEnabled()) {
      log.debug("Timer {} fired. Deleting timer.", getId());
    }
    delete();

    if (repeat != null) {
      int repeatValue = calculateRepeatValue();
      if (repeatValue != 0) {
        if (repeatValue > 0) {
          setNewRepeat(repeatValue);
        }

        Date newTimer = calculateNextTimer();
        if (newTimer != null && isValidTime(newTimer)) {
          TimerEntity te = new TimerEntity(this);
          te.setDuedate(newTimer);
          Context.getCommandContext().getJobEntityManager().schedule(te);
        }
      }
    }
  }

  protected void restoreExtraData(CommandContext commandContext, String jobHandlerConfiguration) {
    String embededActivityId = jobHandlerConfiguration;

    if (jobHandlerType.equalsIgnoreCase(TimerExecuteNestedActivityJobHandler.TYPE) ||
            jobHandlerType.equalsIgnoreCase(TimerCatchIntermediateEventJobHandler.TYPE) ||
            jobHandlerType.equalsIgnoreCase(TimerStartEventJobHandler.TYPE)) {

      embededActivityId = TimerEventHandler.getActivityIdFromConfiguration(jobHandlerConfiguration);

      String endDateExpressionString = TimerEventHandler.getEndDateFromConfiguration(jobHandlerConfiguration);

      if (endDateExpressionString!=null) {
         Expression endDateExpression = Context.getProcessEngineConfiguration().getExpressionManager().createExpression(endDateExpressionString);

        String endDateString = null;

        BusinessCalendar businessCalendar = Context.getProcessEngineConfiguration().getBusinessCalendarManager()
                .getBusinessCalendar(getBusinessCalendarName(TimerEventHandler.geCalendarNameFromConfiguration(jobHandlerConfiguration)));

        VariableScope executionEntity = null;
        if (executionId != null) {
          executionEntity = commandContext.getExecutionEntityManager().findExecutionById(executionId);
        }
        
        if (executionEntity == null) {
          executionEntity = NoExecutionVariableScope.getSharedInstance();
        }

        if (endDateExpression != null) {
          Object endDateValue = endDateExpression.getValue(executionEntity);
          if (endDateValue instanceof String) {
            endDateString = (String) endDateValue;
          } else if (endDateValue instanceof Date) {
            endDate = (Date) endDateValue;
          } else {
            throw new ActivitiException("Timer '" + ((ExecutionEntity) executionEntity).getActivityId()
                    + "' was not configured with a valid duration/time, either hand in a java.util.Date or a String in format 'yyyy-MM-dd'T'hh:mm:ss'");
          }

          if (endDate == null) {
            endDate = businessCalendar.resolveEndDate(endDateString);
          }
        }
      }
    }

     if (processDefinitionId != null) {
      ProcessDefinition definition = commandContext.getProcessEngineConfiguration()
          .getDeploymentManager().findDeployedProcessDefinitionById(processDefinitionId); 
      maxIterations = checkStartEventDefinitions(definition, embededActivityId);
      if (maxIterations <= 1) {
        maxIterations = checkBoundaryEventsDefinitions(definition, embededActivityId);
      }
    } else {
      maxIterations = 1;
    }
  }

  protected int checkStartEventDefinitions(ProcessDefinition def, String embededActivityId) {
    List<TimerDeclarationImpl> startTimerDeclarations = (List<TimerDeclarationImpl>) ((ProcessDefinitionEntity) def).getProperty("timerStart");

    if (startTimerDeclarations != null && startTimerDeclarations.size() > 0) {
      TimerDeclarationImpl timerDeclaration = null;
      
      for (TimerDeclarationImpl startTimerDeclaration : startTimerDeclarations) {
        String definitionActivityId = TimerEventHandler.getActivityIdFromConfiguration(startTimerDeclaration.getJobHandlerConfiguration());
        if (startTimerDeclaration.getJobHandlerType().equalsIgnoreCase(jobHandlerType) 
            && (definitionActivityId.equalsIgnoreCase(embededActivityId))) {
          timerDeclaration = startTimerDeclaration;
        }
      }
      
      if (timerDeclaration != null) {
        return calculateMaxIterationsValue(timerDeclaration.getDescription().getExpressionText());
      }
    }
    return 1;
  }
  
  protected int checkBoundaryEventsDefinitions(ProcessDefinition def, String embededActivityId) {
    return checkBoundaryEventsDefinitions(((ProcessDefinitionEntity) def).getActivities(), embededActivityId);
  }

  protected int checkBoundaryEventsDefinitions(List<ActivityImpl> activities, String embededActivityId) {
    // should check level by level, first check provided activities list 
    for (ActivityImpl activity : activities) {
      List<TimerDeclarationImpl> activityTimerDeclarations = (List<TimerDeclarationImpl>) activity.getProperty("timerDeclarations");
      if (activityTimerDeclarations != null) {
        for (TimerDeclarationImpl timerDeclaration : activityTimerDeclarations) {
          String definitionActivityId = TimerEventHandler.getActivityIdFromConfiguration(timerDeclaration.getJobHandlerConfiguration());
          if (timerDeclaration.getJobHandlerType().equalsIgnoreCase(jobHandlerType) && (definitionActivityId.equalsIgnoreCase(embededActivityId))) {
            return calculateMaxIterationsValue(timerDeclaration.getDescription().getExpressionText());
          }
        }
      }
    }
    
    // now check sub activities 
    for (ActivityImpl activity : activities) {
      return checkBoundaryEventsDefinitions(activity.getActivities(), embededActivityId);
    }
    
    return 1;
  }

  protected int calculateMaxIterationsValue(String originalExpression) {
    int times = Integer.MAX_VALUE;
    List<String> expression = Arrays.asList(originalExpression.split("/"));
    if (expression.size() > 1 && expression.get(0).startsWith("R")) {
      times = Integer.MAX_VALUE;
      if (expression.get(0).length() > 1) {
        times = Integer.parseInt(expression.get(0).substring(1));
      }
    }
    return times;
  }

  protected boolean isValidTime(Date newTimer) {
    BusinessCalendar businessCalendar = Context
        .getProcessEngineConfiguration()
        .getBusinessCalendarManager()
        .getBusinessCalendar(getBusinessCalendarName(TimerEventHandler.geCalendarNameFromConfiguration(jobHandlerConfiguration)));
    return businessCalendar.validateDuedate(repeat , maxIterations, endDate, newTimer);
  }

  protected int calculateRepeatValue() {
    int times = -1;
    List<String> expression = Arrays.asList(repeat.split("/"));
    if (expression.size() > 1 && expression.get(0).startsWith("R") && expression.get(0).length() > 1) {
      times = Integer.parseInt(expression.get(0).substring(1));
      if (times > 0) {
        times--;
      }
    }
    return times;
  }
  
  protected void setNewRepeat(int newRepeatValue) {
    List<String> expression = Arrays.asList(repeat.split("/"));
    expression = expression.subList(1, expression.size());
    StringBuilder repeatBuilder = new StringBuilder("R");
    repeatBuilder.append(newRepeatValue);
    for (String value : expression) {
      repeatBuilder.append("/");
      repeatBuilder.append(value);
    }
    repeat = repeatBuilder.toString();
  }

  protected Date calculateNextTimer() {
    BusinessCalendar businessCalendar = Context
        .getProcessEngineConfiguration()
        .getBusinessCalendarManager()
        .getBusinessCalendar(getBusinessCalendarName(TimerEventHandler.geCalendarNameFromConfiguration(jobHandlerConfiguration)));
    return businessCalendar.resolveDuedate(repeat,maxIterations);
  }

  protected String getBusinessCalendarName(String calendarName) {
    String businessCalendarName = CycleBusinessCalendar.NAME;
    if (StringUtils.isNotEmpty(calendarName)) {
      VariableScope execution = NoExecutionVariableScope.getSharedInstance();
      if (StringUtils.isNotEmpty(this.executionId)) {
        execution = Context.getCommandContext().getExecutionEntityManager().findExecutionById(this.executionId);
      }
      businessCalendarName = (String) Context.getProcessEngineConfiguration().getExpressionManager().createExpression(calendarName).getValue(execution);
    }
    return businessCalendarName;
  }

  public String getRepeat() {
    return repeat;
  }

  public void setRepeat(String repeat) {
    this.repeat = repeat;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }
}
