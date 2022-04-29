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
package org.activiti.bpmn.converter.export;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Error;

public class ErrorExport implements BpmnXMLConstants {

    public static void writeError(BpmnModel model,
                                  XMLStreamWriter xtw) throws Exception {
        for (Error error : model.getErrors().values()) {
            xtw.writeStartElement(ELEMENT_ERROR);
            xtw.writeAttribute(ATTRIBUTE_ID,
                               error.getId());
            xtw.writeAttribute(ATTRIBUTE_NAME,
                               error.getName());
            xtw.writeAttribute(ATTRIBUTE_ERROR_CODE,
                               error.getErrorCode());
            xtw.writeEndElement();
        }
    }
}
