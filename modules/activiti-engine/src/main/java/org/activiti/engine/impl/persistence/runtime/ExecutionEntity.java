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

import java.util.List;

import org.activiti.engine.impl.persistence.PersistentObject;
import org.activiti.pvm.impl.runtime.ExecutionImpl;
import org.activiti.pvm.impl.runtime.TimerDeclarationImpl;
import org.activiti.pvm.impl.runtime.TimerImpl;
import org.activiti.pvm.process.PvmProcessDefinition;
import org.activiti.pvm.process.PvmTransition;
import org.activiti.pvm.runtime.PvmExecution;
import org.activiti.pvm.runtime.PvmProcessInstance;


/**
 * @author Tom Baeyens
 */
public class ExecutionEntity extends ExecutionImpl implements PersistentObject {

  protected Object cachedElContext = null;

  public Object getCachedElContext() {
    return cachedElContext;
  }
  public void setCachedElContext(Object cachedElContext) {
    this.cachedElContext = cachedElContext;
  }

  // timers ///////////////////////////////////////////////////////////////////

  public void createTimer(TimerDeclarationImpl timerDeclaration) {
    TimerImpl timer = new TimerImpl();
    timer.setExecution(this);
    timer.setDuedate( timerDeclaration.getDuedate() );
    timer.setJobHandlerType( timerDeclaration.getJobHandlerType() );
    timer.setJobHandlerConfiguration( timerDeclaration.getJobHandlerConfiguration() );
    timer.setExclusive(timerDeclaration.isExclusive());
    timer.setRepeat(timerDeclaration.getRepeat());
    timer.setRetries(timerDeclaration.getRetries());
    
    CommandContext
      .getCurrent()
      .getTimerSession()
      .schedule(timer);
  }

}
