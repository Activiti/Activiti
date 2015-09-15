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

import java.util.Date;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TimerEntityImpl extends JobEntityImpl implements TimerEntity {

  private static final long serialVersionUID = 1L;

  protected int maxIterations;
  protected String repeat;
  protected Date endDate;

  public TimerEntityImpl() {
    super();
    this.jobType = "timer";
  }

  public TimerEntityImpl(TimerEntityImpl te) {
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

  public int getMaxIterations() {
    return maxIterations;
  }

  public void setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations;
  }

}
