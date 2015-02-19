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

import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tom Baeyens
 */
public class TimerEntity extends JobEntity {

  private static final long serialVersionUID = 1L;

  private static Logger log = LoggerFactory.getLogger(TimerEntity.class);

  protected String repeat;

  public TimerEntity() {
  }

  public TimerEntity(TimerDeclarationImpl timerDeclaration) {
    jobHandlerType = timerDeclaration.getJobHandlerType();
    jobHandlerConfiguration = timerDeclaration.getJobHandlerConfiguration();
    isExclusive = timerDeclaration.isExclusive();
    repeat = timerDeclaration.getRepeat();
    retries = timerDeclaration.getRetries();
  }

  private TimerEntity(TimerEntity te) {
    jobHandlerConfiguration = te.jobHandlerConfiguration;
    jobHandlerType = te.jobHandlerType;
    isExclusive = te.isExclusive;
    repeat = te.repeat;
    retries = te.retries;
    executionId = te.executionId;
    processInstanceId = te.processInstanceId;
    processDefinitionId = te.processDefinitionId;

    // Inherit tenant
    tenantId = te.tenantId;
  }

  @Override
  public void execute(CommandContext commandContext) {

    super.execute(commandContext);

    if (repeat == null) {

      if (log.isDebugEnabled()) {
        log.debug("Timer {} fired. Deleting timer.", getId());
      }
      delete();
    } else {
      delete();
      int repeatValue = calculateRepeatValue();
      if (repeatValue != 0) {
        if (repeatValue > 0) {
          setNewRepeat(repeatValue);
        }
        Date newTimer = calculateRepeat();
        if (newTimer != null) {
          TimerEntity te = new TimerEntity(this);
          te.setDuedate(newTimer);
          Context
              .getCommandContext()
              .getJobEntityManager()
              .schedule(te);
        }
      }
    }

  }
  
  private int calculateRepeatValue() {
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
  
  private void setNewRepeat(int newRepeatValue) {
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

  private Date calculateRepeat() {
    BusinessCalendar businessCalendar = Context
        .getProcessEngineConfiguration()
        .getBusinessCalendarManager()
        .getBusinessCalendar(CycleBusinessCalendar.NAME);
    return businessCalendar.resolveDuedate(repeat);
  }

  public String getRepeat() {
    return repeat;
  }

  public void setRepeat(String repeat) {
    this.repeat = repeat;
  }
}
