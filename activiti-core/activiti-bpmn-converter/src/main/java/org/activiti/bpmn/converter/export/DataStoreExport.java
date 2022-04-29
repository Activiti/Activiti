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
import org.activiti.bpmn.model.DataStore;
import org.apache.commons.lang3.StringUtils;

public class DataStoreExport implements BpmnXMLConstants {

  public static void writeDataStores(BpmnModel model, XMLStreamWriter xtw) throws Exception {

    for (DataStore dataStore : model.getDataStores().values()) {
      xtw.writeStartElement(ELEMENT_DATA_STORE);
      xtw.writeAttribute(ATTRIBUTE_ID, dataStore.getId());
      xtw.writeAttribute(ATTRIBUTE_NAME, dataStore.getName());
      if (StringUtils.isNotEmpty(dataStore.getItemSubjectRef())) {
        xtw.writeAttribute(ATTRIBUTE_ITEM_SUBJECT_REF, dataStore.getItemSubjectRef());
      }

      if (StringUtils.isNotEmpty(dataStore.getDataState())) {
        xtw.writeStartElement(ELEMENT_DATA_STATE);
        xtw.writeCharacters(dataStore.getDataState());
        xtw.writeEndElement();
      }

      xtw.writeEndElement();
    }
  }
}
