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
package org.activiti.bpmn.converter.child;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;

/**
 * @author Tijs Rademakers
 */
public class ExecutionListenerParser extends ActivitiListenerParser {

  public String getElementName() {
  	return "executionListener";
  }
  
  public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement) throws Exception {
    
    super.parseChildElement(xtr, parentElement);
    
    if (parentElement instanceof Activity) {
      ((Activity) parentElement).getExecutionListeners().add(listener);
    } else if (parentElement instanceof SequenceFlow){
      ((SequenceFlow) parentElement).getExecutionListeners().add(listener);
    } else if (parentElement instanceof SubProcess){
      ((SubProcess) parentElement).getExecutionListeners().add(listener);
    } else if (parentElement instanceof Process){
      ((Process) parentElement).getExecutionListeners().add(listener);
    }
  }
}
