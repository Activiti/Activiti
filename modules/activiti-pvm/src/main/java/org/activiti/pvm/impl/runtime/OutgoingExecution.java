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

import org.activiti.pvm.activity.ActivityExecution;
import org.activiti.pvm.process.PvmTransition;

/**
 * 
 * @author Tom Baeyens
 */
public class OutgoingExecution {
  
  protected ActivityExecution outgoingExecution;
  protected PvmTransition outgoingTransition;
  protected boolean isNew;

  public OutgoingExecution(ActivityExecution outgoingExecution, PvmTransition outgoingTransition, boolean isNew) {
    this.outgoingExecution = outgoingExecution;
    this.outgoingTransition = outgoingTransition;
    this.isNew = isNew;
  }
  
  public void take() {
    outgoingExecution.setActive(true);
    outgoingExecution.setConcurrent(true);
    outgoingExecution.take(outgoingTransition);
  }
}