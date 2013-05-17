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
package org.activiti.engine.impl.jobexecutor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.StartProcessVariableScope;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.util.ClockUtil;

/**
 * @author Tom Baeyens
 */
public class TimerDeclarationImpl implements Serializable {

  private static final long serialVersionUID = 1L;

  protected Expression description;
  protected TimerDeclarationType type;

  protected String jobHandlerType;
  protected String jobHandlerConfiguration = null;
  protected String repeat;
  protected boolean exclusive = TimerEntity.DEFAULT_EXCLUSIVE;
  protected int retries = TimerEntity.DEFAULT_RETRIES;
  protected boolean isInterruptingTimer; // For boundary timers

  public TimerDeclarationImpl(Expression expression, TimerDeclarationType type, String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
    this.description = expression;
    this.type= type;
  }

  public String getJobHandlerType() {
    return jobHandlerType;
  }

  public String getJobHandlerConfiguration() {
    return jobHandlerConfiguration;
  }

  public void setJobHandlerConfiguration(String jobHandlerConfiguration) {
    this.jobHandlerConfiguration = jobHandlerConfiguration;
  }

  public String getRepeat() {
    return repeat;
  }

  public void setRepeat(String repeat) {
    this.repeat = repeat;
  }

  public boolean isExclusive() {
    return exclusive;
  }

  public void setExclusive(boolean exclusive) {
    this.exclusive = exclusive;
  }

  public int getRetries() {
    return retries;
  }

  public void setRetries(int retries) {
    this.retries = retries;
  }

  public void setJobHandlerType(String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
  }
  
  public boolean isInterruptingTimer() {
    return isInterruptingTimer;
  }
  
  public void setInterruptingTimer(boolean isInterruptingTimer) {
    this.isInterruptingTimer = isInterruptingTimer;
  }

  public TimerEntity prepareTimerEntity(ExecutionEntity executionEntity) {
    BusinessCalendar businessCalendar = Context
        .getProcessEngineConfiguration()
        .getBusinessCalendarManager()
        .getBusinessCalendar(type.calendarName);
    
    if (description==null) {
      // Prevent NPE from happening in the next line
      throw new ActivitiIllegalArgumentException("Timer '"+executionEntity.getActivityId()+"' was not configured with a valid duration/time");
    }
    
    String dueDateString = null;
    Date duedate = null;

    // ACT-1415: timer-declaration on start-event may contain expressions NOT
    // evaluating variables but other context, evaluating should happen nevertheless
    VariableScope scopeForExpression = executionEntity;
    if(scopeForExpression == null) {
      scopeForExpression = StartProcessVariableScope.getSharedInstance();
    }

    Object dueDateValue = description.getValue(scopeForExpression);
    if (dueDateValue instanceof String) {
      dueDateString = (String)dueDateValue;
    }
    else if (dueDateValue instanceof Date) {
      duedate = (Date)dueDateValue;
    }
    else {
      throw new ActivitiException("Timer '"+executionEntity.getActivityId()+"' was not configured with a valid duration/time, either hand in a java.util.Date or a String in format 'yyyy-MM-dd'T'hh:mm:ss'");
    }
    
    if (duedate==null) {      
      duedate = businessCalendar.resolveDuedate(dueDateString);
    }

    TimerEntity timer = new TimerEntity(this);
    timer.setDuedate(duedate);
    if (executionEntity != null) {
      timer.setExecution(executionEntity);
      timer.setProcessDefinitionId(executionEntity.getProcessDefinitionId());
    }
    
    if (type == TimerDeclarationType.CYCLE) {
      
      // See ACT-1427: A boundary timer with a cancelActivity='true', doesn't need to repeat itself
      if (!isInterruptingTimer) {
        String prepared = prepareRepeat(dueDateString);
        timer.setRepeat(prepared);
      }
    }
    
    return timer;
  }
  private String prepareRepeat(String dueDate) {
    if (dueDate.startsWith("R") && dueDate.split("/").length==2) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      return dueDate.replace("/","/"+sdf.format(ClockUtil.getCurrentTime())+"/");
    }
    return dueDate;
  }
}
