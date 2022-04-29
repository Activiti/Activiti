/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.bpmn.converter.parser;

import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.child.ActivitiEventListenerParser;
import org.activiti.bpmn.converter.child.ExecutionListenerParser;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SubProcess;


public class ExtensionElementsParser implements BpmnXMLConstants {

  public void parse(XMLStreamReader xtr, List<SubProcess> activeSubProcessList, Process activeProcess, BpmnModel model) throws Exception {
    BaseElement parentElement = null;
    if (!activeSubProcessList.isEmpty()) {
      parentElement = activeSubProcessList.get(activeSubProcessList.size() - 1);

    } else {
      parentElement = activeProcess;
    }

    boolean readyWithChildElements = false;
    while (readyWithChildElements == false && xtr.hasNext()) {
      xtr.next();
      if (xtr.isStartElement()) {
        if (ELEMENT_EXECUTION_LISTENER.equals(xtr.getLocalName())) {
          new ExecutionListenerParser().parseChildElement(xtr, parentElement, model);
        } else if (ELEMENT_EVENT_LISTENER.equals(xtr.getLocalName())) {
          new ActivitiEventListenerParser().parseChildElement(xtr, parentElement, model);
        } else if (ELEMENT_POTENTIAL_STARTER.equals(xtr.getLocalName())) {
          new PotentialStarterParser().parse(xtr, activeProcess);
        } else {
          ExtensionElement extensionElement = BpmnXMLUtil.parseExtensionElement(xtr);
          if (parentElement != null) {
              parentElement.addExtensionElement(extensionElement);
          }
        }

      } else if (xtr.isEndElement()) {
        if (ELEMENT_EXTENSIONS.equals(xtr.getLocalName())) {
          readyWithChildElements = true;
        }
      }
    }
  }
}
