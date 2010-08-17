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

import org.activiti.engine.impl.persistence.runtime.TimerEntity;


/**
 * @author Tom Baeyens
 */
public class TimerDeclarationImpl implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String duedateDescription;
  protected String jobHandlerType;
  protected String jobHandlerConfiguration = null;
  protected String repeat;
  protected boolean exclusive = TimerEntity.DEFAULT_EXCLUSIVE;
  protected int retries = TimerEntity.DEFAULT_RETRIES;

  public TimerDeclarationImpl(String duedateDescription, String jobHandlerType) {
    this.duedateDescription = duedateDescription;
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
  public String getDuedateDescription() {
    return duedateDescription;
  }
  public void setDuedateDescription(String duedateDescription) {
    this.duedateDescription = duedateDescription;
  }
  public void setJobHandlerType(String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
  }
}
