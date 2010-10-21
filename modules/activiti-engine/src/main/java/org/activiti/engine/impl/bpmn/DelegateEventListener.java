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

package org.activiti.engine.impl.bpmn;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.event.EventListenerExecution;

/**
 * EventListener that delegates the event notification to an instance of
 * {@link EventListener}. The delegate {@link EventListener} instance is only
 * created once and injected based on the fieldDeclarations.
 * 
 * @author Frederik Heremans
 */
public class DelegateEventListener extends FieldDeclarationDelegate implements EventListener {

  public DelegateEventListener(String className, List<FieldDeclaration> fieldDeclarations) {
    super(className, fieldDeclarations);
  }

  public void notify(EventListenerExecution execution) throws Exception {
    Object delegate = getDelegateInstance();
    if (delegate instanceof EventListener) {
      ((EventListener) delegate).notify(execution);
    } else {
      throw new ActivitiException("Class " + getDelegateClassName() + " is used as event-listener but does not implement the interface "
        + EventListener.class.getName());
    }
  }
}
