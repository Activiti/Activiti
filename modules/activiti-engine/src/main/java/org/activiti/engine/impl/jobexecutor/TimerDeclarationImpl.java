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
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.util.ClockUtil;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;


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

  public TimerEntity prepareTimerEntity(ExecutionEntity executionEntity) {
    BusinessCalendar businessCalendar = Context
        .getProcessEngineConfiguration()
        .getBusinessCalendarManager()
        .getBusinessCalendar(type.caledarName);

    String dueDateString = executionEntity == null ? description.getExpressionText() : (String) description.getValue(executionEntity);
    Date duedate = businessCalendar.resolveDuedate(dueDateString);

    TimerEntity timer = new TimerEntity(this);
    timer.setDuedate(duedate);
    if (executionEntity != null) {
      timer.setExecution(executionEntity);
    }
    if (type == TimerDeclarationType.CYCLE) {
      String prepared = prepareRepeat(dueDateString);
      timer.setRepeat(prepared);

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
