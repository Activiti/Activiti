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
package org.activiti.pvm.impl.runtime;

import org.activiti.pvm.PvmException;
import org.activiti.pvm.activity.EventActivityBehavior;
import org.activiti.pvm.impl.process.ActivityImpl;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationSignal implements AtomicOperation {

  protected String signalName;
  protected Object signalData;

  public AtomicOperationSignal(String signalName, Object signalData) {
    this.signalName = signalName;
    this.signalData = signalData;
  }

  public boolean isAsync() {
    return false;
  }

  public void execute(ExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();
    EventActivityBehavior activityBehavior = (EventActivityBehavior) activity.getActivityBehavior();
    try {
      activityBehavior.signal(execution, signalName, signalData);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new PvmException("couldn't process signal '"+signalName+"' on activity '"+activity.getId()+"': "+e.getMessage(), e);
    }
  }
  
  public String toString() {
    return "Signal["+signalName+"]";
  }
}
