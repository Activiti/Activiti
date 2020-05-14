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
package org.activiti.bpmn.converter.child;

import static java.util.Collections.unmodifiableList;

import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public abstract class BaseChildElementParser implements BpmnXMLConstants {

  protected static final Logger LOGGER = LoggerFactory.getLogger(BaseChildElementParser.class);

  public abstract String getElementName();

  public abstract void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception;

  protected void parseChildElements(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model, BaseChildElementParser parser) throws Exception {
    boolean readyWithChildElements = false;
    while (!readyWithChildElements  && xtr.hasNext()) {
      xtr.next();
      if (xtr.isStartElement()) {
        if (parser.getElementName().equals(xtr.getLocalName())) {
          parser.parseChildElement(xtr, parentElement, model);
        }

      } else if (xtr.isEndElement() && getElementName().equalsIgnoreCase(xtr.getLocalName())) {
        readyWithChildElements = true;
      }
    }
  }

  public boolean accepts(BaseElement element) {
    return element != null;
  }

  protected List<ExtensionAttribute> parseExtensionAttributes(XMLStreamReader xtr,
                                                              BaseElement parentElement,
                                                              BpmnModel model) {
    List<ExtensionAttribute> attributes = new LinkedList<>();

    for(int i=0; i < xtr.getAttributeCount(); i++) {
      if(ACTIVITI_EXTENSIONS_NAMESPACE.equals(xtr.getAttributeNamespace(i))) {
        ExtensionAttribute attr = new ExtensionAttribute(ACTIVITI_EXTENSIONS_NAMESPACE,
                                                         xtr.getAttributeLocalName(i));
        attr.setValue(xtr.getAttributeValue(i));
        attributes.add(attr);
      }
    }

    return unmodifiableList(attributes);
  }
}
