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

import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.MapExceptionEntry;
import org.apache.commons.lang3.StringUtils;



public class ActivitiMapExceptionParser extends BaseChildElementParser {

  @Override
  public String getElementName() {
    return MAP_EXCEPTION;
  }

  @Override
  public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
    if (!(parentElement instanceof Activity))
      return;

    String errorCode = xtr.getAttributeValue(null, MAP_EXCEPTION_ERRORCODE);
    String andChildren = xtr.getAttributeValue(null, MAP_EXCEPTION_ANDCHILDREN);
    String exceptionClass = xtr.getElementText();
    boolean hasChildrenBool = false;

    if (StringUtils.isEmpty(andChildren) || andChildren.toLowerCase().equals("false")) {
      hasChildrenBool = false;
    } else if (andChildren.toLowerCase().equals("true")) {
      hasChildrenBool = true;
    } else {
      throw new XMLException("'" + andChildren + "' is not valid boolean in mapException with errorCode=" + errorCode + " and class=" + exceptionClass);
    }

    if (StringUtils.isEmpty(errorCode) || StringUtils.isEmpty(errorCode.trim())) {
      throw new XMLException("No errorCode defined mapException with errorCode=" + errorCode + " and class=" + exceptionClass);
    }

    ((Activity) parentElement).getMapExceptions().add(new MapExceptionEntry(errorCode, exceptionClass, hasChildrenBool));
  }
}
