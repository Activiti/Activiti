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

import org.activiti.engine.impl.pvm.PvmTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class OutgoingExecution {
  
  private static Logger log = LoggerFactory.getLogger(OutgoingExecution.class);
  
  protected InterpretableExecution outgoingExecution;
  protected PvmTransition outgoingTransition;
  protected boolean isNew;

  public OutgoingExecution(InterpretableExecution outgoingExecution, PvmTransition outgoingTransition, boolean isNew) {
    this.outgoingExecution = outgoingExecution;
    this.outgoingTransition = outgoingTransition;
    this.isNew = isNew;
  }
  
  public void take() {
  	take(true);
  }
  
  public void take(boolean fireActivityCompletedEvent) {
    if (outgoingExecution.getReplacedBy()!=null) {
      outgoingExecution = outgoingExecution.getReplacedBy();
    }    
    if(!outgoingExecution.isDeleteRoot()) {
      outgoingExecution.take(outgoingTransition, fireActivityCompletedEvent);
    } else {
      log.debug("Not taking transition '{}', outgoing execution has ended.", outgoingTransition);      
    }
  }
}