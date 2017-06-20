/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataStoreReference;
import org.apache.commons.lang3.StringUtils;

/**

 */
public class DataStoreReferenceXMLConverter extends BaseBpmnXMLConverter {

  public Class<? extends BaseElement> getBpmnElementType() {
    return DataStoreReference.class;
  }

  @Override
  protected String getXMLElementName() {
    return ELEMENT_DATA_STORE_REFERENCE;
  }

  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
    DataStoreReference dataStoreRef = new DataStoreReference();
    BpmnXMLUtil.addXMLLocation(dataStoreRef, xtr);
    parseChildElements(getXMLElementName(), dataStoreRef, model, xtr);
    return dataStoreRef;
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
    DataStoreReference dataStoreRef = (DataStoreReference) element;
    if (StringUtils.isNotEmpty(dataStoreRef.getDataStoreRef())) {
      xtw.writeAttribute(ATTRIBUTE_DATA_STORE_REF, dataStoreRef.getDataStoreRef());
    }

    if (StringUtils.isNotEmpty(dataStoreRef.getItemSubjectRef())) {
      xtw.writeAttribute(ATTRIBUTE_ITEM_SUBJECT_REF, dataStoreRef.getItemSubjectRef());
    }
  }

  @Override
  protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
    DataStoreReference dataStoreRef = (DataStoreReference) element;
    if (StringUtils.isNotEmpty(dataStoreRef.getDataState())) {
      xtw.writeStartElement(ELEMENT_DATA_STATE);
      xtw.writeCharacters(dataStoreRef.getDataState());
      xtw.writeEndElement();
    }
  }
}
