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
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.AdhocSubProcess;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Transaction;
import org.apache.commons.lang3.StringUtils;


public class SubProcessParser implements BpmnXMLConstants {

  public void parse(XMLStreamReader xtr, List<SubProcess> activeSubProcessList, Process activeProcess) {
    SubProcess subProcess = null;
    if (ELEMENT_TRANSACTION.equalsIgnoreCase(xtr.getLocalName())) {
      subProcess = new Transaction();

    } else if (ELEMENT_ADHOC_SUBPROCESS.equalsIgnoreCase(xtr.getLocalName())) {
      AdhocSubProcess adhocSubProcess = new AdhocSubProcess();
      String orderingAttributeValue = xtr.getAttributeValue(null, ATTRIBUTE_ORDERING);
      if (StringUtils.isNotEmpty(orderingAttributeValue)) {
        adhocSubProcess.setOrdering(orderingAttributeValue);
      }

      if (ATTRIBUTE_VALUE_FALSE.equalsIgnoreCase(xtr.getAttributeValue(null, ATTRIBUTE_CANCEL_REMAINING_INSTANCES))) {
        adhocSubProcess.setCancelRemainingInstances(false);
      }

      subProcess = adhocSubProcess;

    } else if (ATTRIBUTE_VALUE_TRUE.equalsIgnoreCase(xtr.getAttributeValue(null, ATTRIBUTE_TRIGGERED_BY))) {
      subProcess = new EventSubProcess();

    } else {
      subProcess = new SubProcess();
    }

    BpmnXMLUtil.addXMLLocation(subProcess, xtr);
    activeSubProcessList.add(subProcess);

    subProcess.setId(xtr.getAttributeValue(null, ATTRIBUTE_ID));
    subProcess.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));

    boolean async = false;
    String asyncString = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_ACTIVITY_ASYNCHRONOUS);
    if (ATTRIBUTE_VALUE_TRUE.equalsIgnoreCase(asyncString)) {
      async = true;
    }

    boolean notExclusive = false;
    String exclusiveString = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_ACTIVITY_EXCLUSIVE);
    if (ATTRIBUTE_VALUE_FALSE.equalsIgnoreCase(exclusiveString)) {
      notExclusive = true;
    }

    boolean forCompensation = false;
    String compensationString = xtr.getAttributeValue(null, ATTRIBUTE_ACTIVITY_ISFORCOMPENSATION);
    if (ATTRIBUTE_VALUE_TRUE.equalsIgnoreCase(compensationString)) {
      forCompensation = true;
    }

    subProcess.setAsynchronous(async);
    subProcess.setNotExclusive(notExclusive);
    subProcess.setForCompensation(forCompensation);
    if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_DEFAULT))) {
      subProcess.setDefaultFlow(xtr.getAttributeValue(null, ATTRIBUTE_DEFAULT));
    }

    if (activeSubProcessList.size() > 1) {
      SubProcess parentSubProcess = activeSubProcessList.get(activeSubProcessList.size() - 2);
      parentSubProcess.addFlowElement(subProcess);

    } else {
      activeProcess.addFlowElement(subProcess);
    }
  }
}
