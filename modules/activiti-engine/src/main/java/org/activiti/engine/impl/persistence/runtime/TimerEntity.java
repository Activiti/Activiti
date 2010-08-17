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
package org.activiti.engine.impl.persistence.runtime;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;


/**
 * @author Tom Baeyens
 */
public class TimerEntity extends JobEntity {

  private static final long serialVersionUID = 1L;
  
  private static Logger log = Logger.getLogger(TimerEntity.class.getName());
  
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

  @Override
  public void execute(JobHandler jobHandler, CommandContext commandContext) {

    super.execute(jobHandler, commandContext);

    if (repeat==null){

      if (log.isLoggable(Level.FINE)) {
        log.fine("Timer " + getId() + " fired. Deleting timer.");
      }
      
      commandContext
        .getDbSqlSession()
        .delete(JobEntity.class, id);

    } else {

      // TODO calculate repeat
      throw new UnsupportedOperationException("repeat not yet supported");
    }
    
  }

  public String getRepeat() {
    return repeat;
  }
  public void setRepeat(String repeat) {
    this.repeat = repeat;
  }
}
