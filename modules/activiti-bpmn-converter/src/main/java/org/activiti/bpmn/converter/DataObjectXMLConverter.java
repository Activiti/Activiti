package org.activiti.bpmn.converter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataObject;
import org.activiti.bpmn.model.ItemDefinition;

/**
 * @author Lori Small
 */
public class DataObjectXMLConverter extends BaseBpmnXMLConverter {
  
  protected boolean didWriteExtensionStartElement = false;
  
  public static String getXMLType() {
    return ELEMENT_DATA_OBJECT;
  }
  
  public static Class<? extends BaseElement> getBpmnElementType() {
    return DataObject.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_DATA_OBJECT;
  }
  
  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception {
    DataObject dataObject = new DataObject();
    ItemDefinition itemSubjectRef = new ItemDefinition();
    
    BpmnXMLUtil.addXMLLocation(dataObject, xtr);

    dataObject.setId(xtr.getAttributeValue(null, ATTRIBUTE_DATA_ID)); 
    dataObject.setName(xtr.getAttributeValue(null, ATTRIBUTE_DATA_NAME)); 

    itemSubjectRef.setStructureRef(xtr.getAttributeValue(null, ATTRIBUTE_DATA_ITEM_REF));
    dataObject.setItemSubjectRef(itemSubjectRef); 

    parseChildElements(xtr, dataObject);

    return dataObject;
  }

  @Override
  public void convertToXML(XMLStreamWriter xtw, BaseElement baseElement, BpmnModel model) throws Exception {
    xtw.writeStartElement(getXMLElementName());
    didWriteExtensionStartElement = false;
    writeDefaultAttribute(ATTRIBUTE_DATA_ID, baseElement.getId(), xtw);
    writeDefaultAttribute(ATTRIBUTE_DATA_NAME, ((DataObject) baseElement).getName(), xtw);
    writeDefaultAttribute(ATTRIBUTE_DATA_ITEM_REF, ((DataObject) baseElement).getItemSubjectRef().getStructureRef(), xtw);
    
    writeExtensionChildElements(baseElement, xtw);
    
    xtw.writeEndElement();
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }

  @Override
  protected void writeExtensionChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }

  @Override
  protected void writeAdditionalChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }

  private void parseChildElements(XMLStreamReader xtr, BaseElement parentElement) throws Exception {
  }
}
