package org.activiti.bpmn.converter;

import java.util.Date;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BooleanDataObject;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DateDataObject;
import org.activiti.bpmn.model.DoubleDataObject;
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
 */
public class ValuedDataObjectXMLConverter extends BaseBpmnXMLConverter {
  
  protected boolean didWriteExtensionStartElement = false;
  
  public static String getXMLType() {
    return ELEMENT_DATA_OBJECT;
  }
  
  public static Class<? extends BaseElement> getBpmnElementType() {
    return ValuedDataObject.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_DATA_OBJECT;
  }
  
  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception {
    ValuedDataObject dataObject = null;
    ItemDefinition itemSubjectRef = new ItemDefinition();

    String structureRef = xtr.getAttributeValue(null, ATTRIBUTE_DATA_ITEM_REF);
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
    
    if (dataObject != null) {
      dataObject.setId(xtr.getAttributeValue(null, ATTRIBUTE_DATA_ID)); 
      dataObject.setName(xtr.getAttributeValue(null, ATTRIBUTE_DATA_NAME)); 
      
      BpmnXMLUtil.addXMLLocation(dataObject, xtr);

      itemSubjectRef.setStructureRef(structureRef);
      dataObject.setItemSubjectRef(itemSubjectRef); 

      parseChildElements(xtr, dataObject);
    }

    return dataObject;
  }

  @Override
  public void convertToXML(XMLStreamWriter xtw, BaseElement baseElement, BpmnModel model) throws Exception {
    xtw.writeStartElement(getXMLElementName());
    didWriteExtensionStartElement = false;
    writeDefaultAttribute(ATTRIBUTE_DATA_ID, baseElement.getId(), xtw);
    writeDefaultAttribute(ATTRIBUTE_DATA_NAME, ((ValuedDataObject) baseElement).getName(), xtw);
    writeDefaultAttribute(ATTRIBUTE_DATA_ITEM_REF, ((ValuedDataObject) baseElement).getItemSubjectRef().getStructureRef(), xtw);
    
    writeExtensionChildElements(baseElement, xtw);
    
    xtw.writeEndElement();
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }

  @Override
  protected void writeExtensionChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
    ValuedDataObject dObj = (ValuedDataObject)element;

    if (StringUtils.isNotEmpty(dObj.getId())) {

      if (didWriteExtensionStartElement == false) { 
        xtw.writeStartElement(ELEMENT_EXTENSIONS);
        didWriteExtensionStartElement = true;
      }

      xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_DATA_VALUE, ACTIVITI_EXTENSIONS_NAMESPACE);
      if (dObj.getValue() != null) {
        String value = null;
        if (dObj instanceof DateDataObject) {
          DateTime dateTime = new DateTime((Date) dObj.getValue());
          value = ISODateTimeFormat.dateTimeNoMillis().print(dateTime);
        } else {
          value = dObj.getValue().toString();
        }
        xtw.writeCharacters(value);
      }
      xtw.writeEndElement();

      xtw.writeEndElement();
    }
  }

  @Override
  protected void writeAdditionalChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }

  private void parseChildElements(XMLStreamReader xtr, ValuedDataObject dataObject) throws Exception {
    
    boolean readyWithDataObject = false;
    try {
      while (readyWithDataObject == false && xtr.hasNext()) {
        xtr.next();
        if (xtr.isStartElement() && ELEMENT_DATA_VALUE.equalsIgnoreCase(xtr.getLocalName())) {
          String value = xtr.getElementText();
          if (StringUtils.isNotEmpty(value)) {
            if (dataObject instanceof DateDataObject) {
              dataObject.setValue(DateTime.parse(value, ISODateTimeFormat.dateOptionalTimeParser()).toDate());
            } else {
              dataObject.setValue(value);
            }
          }

        } else if (xtr.isEndElement() && ELEMENT_DATA_OBJECT.equalsIgnoreCase(xtr.getLocalName())) {
          readyWithDataObject = true;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Error parsing data object child elements", e);
    }
  }
}
