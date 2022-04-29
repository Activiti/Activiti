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

import org.activiti.bpmn.converter.export.FieldExtensionExport;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CustomProperty;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;
import org.apache.commons.lang3.StringUtils;


public class ServiceTaskXMLConverter extends BaseBpmnXMLConverter {

  public Class<? extends BaseElement> getBpmnElementType() {
    return ServiceTask.class;
  }

  @Override
  protected String getXMLElementName() {
    return ELEMENT_TASK_SERVICE;
  }

  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
    ServiceTask serviceTask = new ServiceTask();
    BpmnXMLUtil.addXMLLocation(serviceTask, xtr);
    if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_CLASS))) {
      serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
      serviceTask.setImplementation(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_CLASS));

    } else if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_EXPRESSION))) {
      serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
      serviceTask.setImplementation(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_EXPRESSION));

    } else if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_DELEGATEEXPRESSION))) {
      serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
      serviceTask.setImplementation(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_DELEGATEEXPRESSION));

    } else if ("##WebService".equals(xtr.getAttributeValue(null, ATTRIBUTE_TASK_IMPLEMENTATION))) {
      serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_WEBSERVICE);
      serviceTask.setOperationRef(parseOperationRef(xtr.getAttributeValue(null, ATTRIBUTE_TASK_OPERATION_REF), model));
    } else {
      serviceTask.setImplementation(xtr.getAttributeValue(null, ATTRIBUTE_TASK_IMPLEMENTATION));
    }

    serviceTask.setResultVariableName(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_RESULTVARIABLE));
    if (StringUtils.isEmpty(serviceTask.getResultVariableName())) {
      serviceTask.setResultVariableName(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "resultVariable"));
    }

    serviceTask.setType(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TYPE));
    serviceTask.setExtensionId(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_EXTENSIONID));

    if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_SKIP_EXPRESSION))) {
      serviceTask.setSkipExpression(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_SKIP_EXPRESSION));
    }
    parseChildElements(getXMLElementName(), serviceTask, model, xtr);

    return serviceTask;
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {

    ServiceTask serviceTask = (ServiceTask) element;

    if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(serviceTask.getImplementationType())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_CLASS, serviceTask.getImplementation(), xtw);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(serviceTask.getImplementationType())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_EXPRESSION, serviceTask.getImplementation(), xtw);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(serviceTask.getImplementationType())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_DELEGATEEXPRESSION, serviceTask.getImplementation(), xtw);
    } else {
      writeDefaultAttribute(ATTRIBUTE_TASK_IMPLEMENTATION, serviceTask.getImplementation(), xtw);
    }

    if (StringUtils.isNotEmpty(serviceTask.getResultVariableName())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_RESULTVARIABLE, serviceTask.getResultVariableName(), xtw);
    }
    if (StringUtils.isNotEmpty(serviceTask.getType())) {
      writeQualifiedAttribute(ATTRIBUTE_TYPE, serviceTask.getType(), xtw);
    }
    if (StringUtils.isNotEmpty(serviceTask.getExtensionId())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_EXTENSIONID, serviceTask.getExtensionId(), xtw);
    }
    if (StringUtils.isNotEmpty(serviceTask.getSkipExpression())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_SKIP_EXPRESSION, serviceTask.getSkipExpression(), xtw);
    }
  }

  @Override
  protected boolean writeExtensionChildElements(BaseElement element, boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
    ServiceTask serviceTask = (ServiceTask) element;

    if (!serviceTask.getCustomProperties().isEmpty()) {
      for (CustomProperty customProperty : serviceTask.getCustomProperties()) {

        if (StringUtils.isEmpty(customProperty.getSimpleValue())) {
          continue;
        }

        if (!didWriteExtensionStartElement) {
          xtw.writeStartElement(ELEMENT_EXTENSIONS);
          didWriteExtensionStartElement = true;
        }
        xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_FIELD, ACTIVITI_EXTENSIONS_NAMESPACE);
        xtw.writeAttribute(ATTRIBUTE_FIELD_NAME, customProperty.getName());
        if ((customProperty.getSimpleValue().contains("${") || customProperty.getSimpleValue().contains("#{")) && customProperty.getSimpleValue().contains("}")) {

          xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ATTRIBUTE_FIELD_EXPRESSION, ACTIVITI_EXTENSIONS_NAMESPACE);
        } else {
          xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_FIELD_STRING, ACTIVITI_EXTENSIONS_NAMESPACE);
        }
        xtw.writeCharacters(customProperty.getSimpleValue());
        xtw.writeEndElement();
        xtw.writeEndElement();
      }
    } else {
      didWriteExtensionStartElement = FieldExtensionExport.writeFieldExtensions(serviceTask.getFieldExtensions(), didWriteExtensionStartElement, xtw);
    }

    return didWriteExtensionStartElement;
  }

  @Override
  protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
  }

  protected String parseOperationRef(String operationRef, BpmnModel model) {
    String result = null;
    if (StringUtils.isNotEmpty(operationRef)) {
      int indexOfP = operationRef.indexOf(':');
      if (indexOfP != -1) {
        String prefix = operationRef.substring(0, indexOfP);
        String resolvedNamespace = model.getNamespace(prefix);
        result = resolvedNamespace + ":" + operationRef.substring(indexOfP + 1);
      } else {
        result = model.getTargetNamespace() + ":" + operationRef;
      }
    }
    return result;
  }
}
