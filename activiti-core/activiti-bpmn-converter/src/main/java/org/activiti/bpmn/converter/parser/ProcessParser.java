/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
import org.activiti.bpmn.converter.export.ProcessExport;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.apache.commons.lang3.StringUtils;


public class ProcessParser implements BpmnXMLConstants {

  public Process parse(XMLStreamReader xtr, BpmnModel model) throws Exception {
    Process process = null;
    if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_ID))) {
      String processId = xtr.getAttributeValue(null, ATTRIBUTE_ID);
      process = new Process();
      process.setId(processId);
      BpmnXMLUtil.addXMLLocation(process, xtr);
      process.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));
      if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_PROCESS_EXECUTABLE))) {
        process.setExecutable(Boolean.parseBoolean(xtr.getAttributeValue(null, ATTRIBUTE_PROCESS_EXECUTABLE)));
      }
      String candidateUsersString = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_PROCESS_CANDIDATE_USERS);
      if (StringUtils.isNotEmpty(candidateUsersString)) {
        List<String> candidateUsers = BpmnXMLUtil.parseDelimitedList(candidateUsersString);
        process.setCandidateStarterUsers(candidateUsers);
      }
      String candidateGroupsString = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_PROCESS_CANDIDATE_GROUPS);
      if (StringUtils.isNotEmpty(candidateGroupsString)) {
        List<String> candidateGroups = BpmnXMLUtil.parseDelimitedList(candidateGroupsString);
        process.setCandidateStarterGroups(candidateGroups);
      }

      BpmnXMLUtil.addCustomAttributes(xtr, process, ProcessExport.defaultProcessAttributes);

      model.getProcesses().add(process);

    }
    return process;
  }
}
