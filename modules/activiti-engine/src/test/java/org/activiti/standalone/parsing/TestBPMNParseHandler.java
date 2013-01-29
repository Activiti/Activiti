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

package org.activiti.standalone.parsing;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;


/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class TestBPMNParseHandler extends AbstractBpmnParseHandler<Process> {

  protected Class< ? extends BaseElement> getHandledType() {
    return Process.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, Process element) {
    // Change the key of all deployed process-definitions
    bpmnParse.getCurrentProcessDefinition().setKey(bpmnParse.getCurrentProcessDefinition().getKey() + "-modified");
  }

}
