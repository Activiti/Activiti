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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.XMLStreamReaderUtil;
import org.activiti.bpmn.model.Process;
import org.apache.commons.lang3.StringUtils;


public class PotentialStarterParser implements BpmnXMLConstants {

  public void parse(XMLStreamReader xtr, Process activeProcess) throws Exception {
    String resourceElement = XMLStreamReaderUtil.moveDown(xtr);
    if (StringUtils.isNotEmpty(resourceElement) && "resourceAssignmentExpression".equals(resourceElement)) {
      String expression = XMLStreamReaderUtil.moveDown(xtr);
      if (StringUtils.isNotEmpty(expression) && "formalExpression".equals(expression)) {
        List<String> assignmentList = new ArrayList<String>();
        String assignmentText = xtr.getElementText();
        if (assignmentText.contains(",")) {
          String[] assignmentArray = assignmentText.split(",");
          assignmentList = asList(assignmentArray);
        } else {
          assignmentList.add(assignmentText);
        }
        for (String assignmentValue : assignmentList) {
          if (assignmentValue == null)
            continue;
          assignmentValue = assignmentValue.trim();
          if (assignmentValue.length() == 0)
            continue;

          String userPrefix = "user(";
          String groupPrefix = "group(";
          if (assignmentValue.startsWith(userPrefix)) {
            assignmentValue = assignmentValue.substring(userPrefix.length(), assignmentValue.length() - 1).trim();
            activeProcess.getCandidateStarterUsers().add(assignmentValue);
          } else if (assignmentValue.startsWith(groupPrefix)) {
            assignmentValue = assignmentValue.substring(groupPrefix.length(), assignmentValue.length() - 1).trim();
            activeProcess.getCandidateStarterGroups().add(assignmentValue);
          } else {
            activeProcess.getCandidateStarterGroups().add(assignmentValue);
          }
        }
      }
    }
  }
}
