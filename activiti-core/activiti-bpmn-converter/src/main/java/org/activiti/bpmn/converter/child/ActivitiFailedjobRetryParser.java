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
package org.activiti.bpmn.converter.child;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;

public class ActivitiFailedjobRetryParser extends BaseChildElementParser {

  @Override
  public String getElementName() {
    return FAILED_JOB_RETRY_TIME_CYCLE;
  }

  @Override
  public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
    if (!(parentElement instanceof Activity))
      return;
    String cycle = xtr.getElementText();
    if (cycle == null || cycle.isEmpty()) {
      return;
    }
    ((Activity) parentElement).setFailedJobRetryTimeCycleValue(cycle);
  }

}
