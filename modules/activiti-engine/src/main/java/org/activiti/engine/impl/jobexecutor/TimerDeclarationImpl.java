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

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.calendar.DurationBusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.runtime.TimerEntity;

import java.io.Serializable;
import java.util.Date;


/**
 * @author Tom Baeyens
 */
public class TimerDeclarationImpl implements Serializable {

  private static final long serialVersionUID = 1L;

  protected Expression durationDescription;
  protected Expression dueDateDescription;
  protected Expression cycleDescription;

  protected String jobHandlerType;
  protected String jobHandlerConfiguration = null;
  protected String repeat;
  protected boolean exclusive = TimerEntity.DEFAULT_EXCLUSIVE;
  protected int retries = TimerEntity.DEFAULT_RETRIES;

  public TimerDeclarationImpl(Expression durationExpression, Expression dueDateExpression, Expression cycleExpression, String jobHandlerType) {
    this.durationDescription = durationExpression;
    this.dueDateDescription = dueDateExpression;
    this.jobHandlerType = jobHandlerType;
    this.cycleDescription = cycleExpression;
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

  public Expression getDuedateDescriptionExpression() {
    return durationDescription;
  }

  public String getDuedateDescriptionValue(VariableScope variableScope) {
    return (String) durationDescription.getValue(variableScope);
  }
//  public void setDuedateDescription(String durationDescription) {
//    this.durationDescription = durationDescription;
//  }

  public void setJobHandlerType(String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
  }

  public TimerEntity prepareTimerEntity(ExecutionEntity executionEntity) {
    String calendarName = dueDateDescription == null ? (durationDescription == null ? CycleBusinessCalendar.NAME : DurationBusinessCalendar.NAME) : DueDateBusinessCalendar.NAME ;
    Expression expression = dueDateDescription == null ? (durationDescription == null ? cycleDescription : durationDescription) : dueDateDescription;

    BusinessCalendar businessCalendar = Context
        .getProcessEngineConfiguration()
        .getBusinessCalendarManager()
        .getBusinessCalendar(calendarName);

    String dueDateString = executionEntity == null ? expression.getExpressionText() : (String) expression.getValue(executionEntity);
    Date duedate = businessCalendar.resolveDuedate(dueDateString);

    TimerEntity timer = new TimerEntity(this);
    timer.setDuedate(duedate);
    if (executionEntity != null) {
      timer.setExecution(executionEntity);
    }
    if (cycleDescription != null) {
      String cycleString = executionEntity == null ? cycleDescription.getExpressionText() : (String) cycleDescription.getValue(executionEntity);
      timer.setRepeat(cycleString);
    }
    return timer;
  }
}
