package org.activiti.bpmn.converter;

import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BooleanDataObject;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DateDataObject;
import org.activiti.bpmn.model.DoubleDataObject;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.IntegerDataObject;
import org.activiti.bpmn.model.ItemDefinition;
import org.activiti.bpmn.model.LongDataObject;
import org.activiti.bpmn.model.StringDataObject;
import org.activiti.bpmn.model.ValuedDataObject;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Lori Small
 * @author Tijs Rademakers
 */
public class ValuedDataObjectXMLConverter extends BaseBpmnXMLConverter {
  
  protected boolean didWriteExtensionStartElement = false;
  
  public Class<? extends BaseElement> getBpmnElementType() {
    return ValuedDataObject.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_DATA_OBJECT;
  }
  
  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
    ValuedDataObject dataObject = null;
    ItemDefinition itemSubjectRef = new ItemDefinition();

    String structureRef = xtr.getAttributeValue(null, ATTRIBUTE_DATA_ITEM_REF);
    if (StringUtils.isNotEmpty(structureRef) && structureRef.contains(":")) {
      String dataType = structureRef.substring(structureRef.indexOf(':') + 1);
      
      if (dataType.equals("string")) {
        dataObject = new StringDataObject();
      } else if (dataType.equals("int")) {
        dataObject = new IntegerDataObject();
      } else if (dataType.equals("long")) {
        dataObject = new LongDataObject();
      } else if (dataType.equals("double")) {
        dataObject = new DoubleDataObject();
      } else if (dataType.equals("boolean")) {
        dataObject = new BooleanDataObject();
      } else if (dataType.equals("datetime")) {
        dataObject = new DateDataObject();
      } else {
        // TODO should throw exception here for unsupported data type
      }
    
    } else {
      // use String as default type
      dataObject = new StringDataObject();
      structureRef = "xsd:string";
    }
    
    if (dataObject != null) {
      dataObject.setId(xtr.getAttributeValue(null, ATTRIBUTE_DATA_ID)); 
      dataObject.setName(xtr.getAttributeValue(null, ATTRIBUTE_DATA_NAME)); 
      
      BpmnXMLUtil.addXMLLocation(dataObject, xtr);

      itemSubjectRef.setStructureRef(structureRef);
      dataObject.setItemSubjectRef(itemSubjectRef); 

      parseChildElements(getXMLElementName(), dataObject, model, xtr);
      
      List<ExtensionElement> valuesElement = dataObject.getExtensionElements().get("value");
      if (valuesElement != null && valuesElement.size() > 0) {
        ExtensionElement valueElement = valuesElement.get(0);
        if (StringUtils.isNotEmpty(valueElement.getElementText())) {
          if (dataObject instanceof DateDataObject) {
            dataObject.setValue(DateTime.parse(valueElement.getElementText(), ISODateTimeFormat.dateOptionalTimeParser()).toDate());
          } else {
            dataObject.setValue(valueElement.getElementText());
          }
        }
        
        // remove value element
        dataObject.getExtensionElements().remove("value");
      }
    }

    return dataObject;
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
    ValuedDataObject dataObject = (ValuedDataObject) element;
    if (dataObject.getItemSubjectRef() != null && StringUtils.isNotEmpty(dataObject.getItemSubjectRef().getStructureRef())) {
      writeDefaultAttribute(ATTRIBUTE_DATA_ITEM_REF, dataObject.getItemSubjectRef().getStructureRef(), xtw);
    }
  }

  @Override
  protected boolean writeExtensionChildElements(BaseElement element, boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
    ValuedDataObject dataObject = (ValuedDataObject) element;

    if (StringUtils.isNotEmpty(dataObject.getId()) && dataObject.getValue() != null) {

      if (didWriteExtensionStartElement == false) { 
        xtw.writeStartElement(ELEMENT_EXTENSIONS);
        didWriteExtensionStartElement = true;
      }

      xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_DATA_VALUE, ACTIVITI_EXTENSIONS_NAMESPACE);
      if (dataObject.getValue() != null) {
        String value = null;
        if (dataObject instanceof DateDataObject) {
          DateTime dateTime = new DateTime((Date) dataObject.getValue());
          value = ISODateTimeFormat.dateTimeNoMillis().print(dateTime);
        } else {
          value = dataObject.getValue().toString();
        }
        xtw.writeCharacters(value);
      }
      xtw.writeEndElement();
    }
    
    return didWriteExtensionStartElement;
  }

  @Override
  protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
  }
}
