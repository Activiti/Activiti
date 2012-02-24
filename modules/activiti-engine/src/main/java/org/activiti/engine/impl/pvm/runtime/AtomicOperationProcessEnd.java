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

package org.activiti.engine.impl.pvm.runtime;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationProcessEnd extends AbstractEventAtomicOperation {
  
  private static Logger log = Logger.getLogger(AtomicOperationProcessEnd.class.getName());

  @Override
  protected ScopeImpl getScope(InterpretableExecution execution) {
    return execution.getProcessDefinition();
  }

  @Override
  protected String getEventName() {
    return org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END;
  }

  @Override
  protected void eventNotificationsCompleted(InterpretableExecution execution) {
    InterpretableExecution superExecution = execution.getSuperExecution();
    SubProcessActivityBehavior subProcessActivityBehavior = null;

    // copy variables before destroying the ended sub process instance
    if (superExecution!=null) {
      ActivityImpl activity = (ActivityImpl) superExecution.getActivity();
      subProcessActivityBehavior = (SubProcessActivityBehavior) activity.getActivityBehavior();
      try {
        subProcessActivityBehavior.completing(superExecution, execution);
      } catch (ActivitiException e) {
        log.log(Level.SEVERE, "Error while completing sub process of execution " + execution, e);
        throw e;    	  
      } catch (Exception e) {
        log.log(Level.SEVERE, "Error while completing sub process of execution " + execution, e);
        throw new ActivitiException("Error while completing sub process of execution " + execution, e);
      }
    }
    
    execution.destroy();
    execution.remove();

    // and trigger execution afterwards
    if (superExecution!=null) {
      superExecution.setSubProcessInstance(null);
      try {
        subProcessActivityBehavior.completed(superExecution);
      } catch (ActivitiException e) {
        log.log(Level.SEVERE, "Error while completing sub process of execution " + execution, e);
        throw e;
      } catch (Exception e) {
        log.log(Level.SEVERE, "Error while completing sub process of execution " + execution, e);
        throw new ActivitiException("Error while completing sub process of execution " + execution, e);
      }
    }
  }
}
