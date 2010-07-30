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
import java.util.Date;

import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.persistence.runtime.TimerImpl;


/**
 * @author Tom Baeyens
 */
public class TimerDeclarationImpl implements Serializable {

  private static final long serialVersionUID = 1L;

  private final BusinessCalendar calendar;
  private final String duedateDeclaration;
  private final String jobHandlerType;
  private String jobHandlerConfiguration = null;
  private String repeat;
  private boolean exclusive = TimerImpl.DEFAULT_EXCLUSIVE;
  private int retries = TimerImpl.DEFAULT_RETRIES;

  
  public TimerDeclarationImpl(BusinessCalendar calendar, String duedateDeclaration, String jobHandlerType) {
    this.calendar = calendar;
    this.duedateDeclaration = duedateDeclaration;
    this.jobHandlerType = jobHandlerType;
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
  
  public Date getDuedate() {
    return calendar.resolveDuedate(duedateDeclaration);
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
}
