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

import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TimerEntity extends JobEntity {

  private static final long serialVersionUID = 1L;

  private static Logger log = LoggerFactory.getLogger(TimerEntity.class);

  protected int maxIterations;
  protected String repeat;
  protected Date endDate;

  public TimerEntity() {
  }

  public TimerEntity(TimerDeclarationImpl timerDeclaration) {
    jobHandlerType = timerDeclaration.getJobHandlerType();
    jobHandlerConfiguration = timerDeclaration.getJobHandlerConfiguration();
    isExclusive = timerDeclaration.isExclusive();
    repeat = timerDeclaration.getRepeat();
    retries = timerDeclaration.getRetries();
  }

  public TimerEntity(String jobHandlerType, String jobHandlerConfiguration, boolean isExclusive, int retries) {
    this.jobHandlerType = jobHandlerType;
    this.jobHandlerConfiguration = jobHandlerConfiguration;
    this.isExclusive = isExclusive;
    this.retries = retries;
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
  }

  @Override
  public void execute(CommandContext commandContext) {

    // set endDate if it was set to the definition
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
          System.out.println("-------AAP--->" + newTimer);
          te.setDuedate(newTimer);
          Context.getCommandContext().getJobEntityManager().schedule(te);
        }
      }
    }
  }

  private void restoreExtraData(CommandContext commandContext, String jobHandlerConfiguration) {
    String activityId = jobHandlerConfiguration;

    if (jobHandlerType.equalsIgnoreCase(TimerStartEventJobHandler.TYPE)) {

      activityId = TimerEventHandler.getActivityIdFromConfiguration(jobHandlerConfiguration);

      String endDateExpressionString = TimerEventHandler.getEndDateFromConfiguration(jobHandlerConfiguration);

      if (endDateExpressionString != null) {
        Expression endDateExpression = Context.getProcessEngineConfiguration().getExpressionManager().createExpression(endDateExpressionString);

        String endDateString = null;

        BusinessCalendar businessCalendar = Context.getProcessEngineConfiguration().getBusinessCalendarManager().getBusinessCalendar(CycleBusinessCalendar.NAME);

        VariableScope executionEntity = commandContext.getExecutionEntityManager().findExecutionById(this.getExecutionId());
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
      org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
      maxIterations = getMaxIterations(process, activityId);
      if (maxIterations <= 1) {
        maxIterations = getMaxIterations(process, activityId);
      }
    } else {
      maxIterations = 1;
    }
  }
  
  protected int getMaxIterations(org.activiti.bpmn.model.Process process, String activityId) {
    FlowElement flowElement = process.getFlowElement(activityId, true);
    if (flowElement != null) {
      if (flowElement instanceof Event) {
        
        Event event = (Event) flowElement;
        List<EventDefinition> eventDefinitions = event.getEventDefinitions();
        
        if (eventDefinitions != null) {
          
          for (EventDefinition eventDefinition : eventDefinitions) {
            if (eventDefinition instanceof TimerEventDefinition) {
              TimerEventDefinition timerEventDefinition = (TimerEventDefinition) eventDefinition;
              if (timerEventDefinition.getTimeCycle() != null) {
                return calculateMaxIterationsValue(timerEventDefinition.getTimeCycle());
              }
            }
          }
          
        }
        
      }
    }
    return -1;
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
    BusinessCalendar businessCalendar = Context.getProcessEngineConfiguration().getBusinessCalendarManager().getBusinessCalendar(CycleBusinessCalendar.NAME);
    return businessCalendar.validateDuedate(repeat, maxIterations, endDate, newTimer);
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
    BusinessCalendar businessCalendar = Context.getProcessEngineConfiguration().getBusinessCalendarManager().getBusinessCalendar(CycleBusinessCalendar.NAME);
    return businessCalendar.resolveDuedate(repeat, maxIterations);
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
