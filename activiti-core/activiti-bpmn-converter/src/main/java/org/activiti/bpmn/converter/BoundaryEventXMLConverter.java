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
package org.activiti.bpmn.converter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventDefinition;
import org.apache.commons.lang3.StringUtils;


public class BoundaryEventXMLConverter extends BaseBpmnXMLConverter {

  public Class<? extends BaseElement> getBpmnElementType() {
    return BoundaryEvent.class;
  }

  @Override
  protected String getXMLElementName() {
    return ELEMENT_EVENT_BOUNDARY;
  }

  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
    BoundaryEvent boundaryEvent = new BoundaryEvent();
    BpmnXMLUtil.addXMLLocation(boundaryEvent, xtr);
    if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_BOUNDARY_CANCELACTIVITY))) {
      String cancelActivity = xtr.getAttributeValue(null, ATTRIBUTE_BOUNDARY_CANCELACTIVITY);
      if (ATTRIBUTE_VALUE_FALSE.equalsIgnoreCase(cancelActivity)) {
        boundaryEvent.setCancelActivity(false);
      }
    }
    boundaryEvent.setAttachedToRefId(xtr.getAttributeValue(null, ATTRIBUTE_BOUNDARY_ATTACHEDTOREF));
    parseChildElements(getXMLElementName(), boundaryEvent, model, xtr);

    // Explicitly set cancel activity to false for error boundary events
    if (boundaryEvent.getEventDefinitions().size() == 1) {
      EventDefinition eventDef = boundaryEvent.getEventDefinitions().get(0);

      if (eventDef instanceof ErrorEventDefinition) {
        boundaryEvent.setCancelActivity(false);
      }
    }

    return boundaryEvent;
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
    BoundaryEvent boundaryEvent = (BoundaryEvent) element;
    if (boundaryEvent.getAttachedToRef() != null) {
      writeDefaultAttribute(ATTRIBUTE_BOUNDARY_ATTACHEDTOREF, boundaryEvent.getAttachedToRef().getId(), xtw);
    }

    if (boundaryEvent.getEventDefinitions().size() == 1) {
      EventDefinition eventDef = boundaryEvent.getEventDefinitions().get(0);

      if (eventDef instanceof ErrorEventDefinition == false) {
        writeDefaultAttribute(ATTRIBUTE_BOUNDARY_CANCELACTIVITY, String.valueOf(boundaryEvent.isCancelActivity()).toLowerCase(), xtw);
      }
    }
  }

  @Override
  protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
    BoundaryEvent boundaryEvent = (BoundaryEvent) element;
    writeEventDefinitions(boundaryEvent, boundaryEvent.getEventDefinitions(), model, xtw);
  }
}
