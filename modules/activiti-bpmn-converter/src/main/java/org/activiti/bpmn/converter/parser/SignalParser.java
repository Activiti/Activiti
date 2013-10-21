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
package org.activiti.bpmn.converter.parser;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Signal;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class SignalParser implements BpmnXMLConstants {
  
  public void parse(XMLStreamReader xtr, BpmnModel model) throws Exception {
    String signalId = xtr.getAttributeValue(null, ATTRIBUTE_ID);
    String signalName = xtr.getAttributeValue(null, ATTRIBUTE_NAME);
    if (StringUtils.isEmpty(signalId)) {
      model.addProblem("signal must have an id", xtr);
    } else if (StringUtils.isEmpty(signalName)) {
      model.addProblem("signal with id '" + signalId + "' has no name", xtr);
    } else {
      
      for (Signal signal : model.getSignals()) {
        if (signal.getName().equals(signalName)) {
          model.addProblem("duplicate signal name", xtr);
        }
      }
      
      Signal signal = new Signal(signalId, signalName);
      
      String scope = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_SCOPE);
      if (scope != null) {
        if (!scope.equals(Signal.SCOPE_GLOBAL) 
                && !scope.equals(Signal.SCOPE_PROCESS_INSTANCE)) {
          model.addProblem("Invalid value for 'scope'. Only 'global' and 'processInstance' is supported, but value is '" + scope + "'", signal);
        }
        signal.setScope(scope);
      }
      
      BpmnXMLUtil.addXMLLocation(signal, xtr);
      BpmnXMLUtil.parseChildElements(ELEMENT_SIGNAL, signal, xtr, model);
      model.addSignal(signal);
    }
  }
}
