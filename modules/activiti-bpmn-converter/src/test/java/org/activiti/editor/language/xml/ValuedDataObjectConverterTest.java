package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.bpmn.model.ValuedDataObject;
import org.junit.Test;

/**
 * @see <a href="https://activiti.atlassian.net/browse/ACT-1847">https://activiti.atlassian.net/browse/ACT-1847</a>
 */
public class ValuedDataObjectConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }
  
  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    validateModel(parsedModel);
    deployProcess(parsedModel);
  }
  
  protected String getResource() {
    return "valueddataobjectmodel.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("start1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof StartEvent);
    assertEquals("start1", flowElement.getId());

    // verify the main process data objects
    List<ValuedDataObject> dataObjects = model.getProcess(null).getDataObjects();
    assertEquals(7, dataObjects.size());
    
    Map<String, ValuedDataObject> objectMap = new HashMap<String, ValuedDataObject>();
    for (ValuedDataObject valueObj : dataObjects) {
      objectMap.put(valueObj.getId(), valueObj);
    }
    
    ValuedDataObject dataObj = objectMap.get("dObj1");
    assertEquals("dObj1", dataObj.getId());
    assertEquals("StringTest", dataObj.getName());
    assertEquals("xsd:string", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof String);
    assertEquals("Testing1&2&3", dataObj.getValue());
    
    dataObj = objectMap.get("dObj2");
    assertEquals("dObj2", dataObj.getId());
    assertEquals("BooleanTest", dataObj.getName());
    assertEquals("xsd:boolean", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof Boolean);
    assertEquals(new Boolean(true), dataObj.getValue());
    
    dataObj = objectMap.get("dObj3");
    assertEquals("dObj3", dataObj.getId());
    assertEquals("DateTest", dataObj.getName());
    assertEquals("xsd:datetime", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof Date);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    assertEquals("2013-09-16T11:23:00", sdf.format(dataObj.getValue()));
    
    dataObj = objectMap.get("dObj4");
    assertEquals("dObj4", dataObj.getId());
    assertEquals("DoubleTest", dataObj.getName());
    assertEquals("xsd:double", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof Double);
    assertEquals(new Double(123456789), dataObj.getValue());
    
    dataObj = objectMap.get("dObj5");
    assertEquals("dObj5", dataObj.getId());
    assertEquals("IntegerTest", dataObj.getName());
    assertEquals("xsd:int", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof Integer);
    assertEquals(new Integer(123), dataObj.getValue());
    
    dataObj = objectMap.get("dObj6");
    assertEquals("dObj6", dataObj.getId());
    assertEquals("LongTest", dataObj.getName());
    assertEquals("xsd:long", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof Long);
    assertEquals(new Long(-123456), dataObj.getValue());
    assertEquals(1, dataObj.getExtensionElements().size());
    List<ExtensionElement> testValues = dataObj.getExtensionElements().get("testvalue");
    assertNotNull(testValues);
    assertEquals(1, testValues.size());
    assertEquals("testvalue", testValues.get(0).getName());
    assertEquals("test", testValues.get(0).getElementText());
    
    dataObj = objectMap.get("NoData");
    assertEquals("NoData", dataObj.getId());
    assertEquals("NoData", dataObj.getName());
    assertEquals("xsd:datetime", dataObj.getItemSubjectRef().getStructureRef());
    assertNull(dataObj.getValue());
   
    flowElement = model.getMainProcess().getFlowElement("userTask1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof UserTask);
    assertEquals("userTask1", flowElement.getId());
    UserTask userTask = (UserTask) flowElement;
    assertEquals("kermit", userTask.getAssignee());
    
    flowElement = model.getMainProcess().getFlowElement("subprocess1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof SubProcess);
    assertEquals("subprocess1", flowElement.getId());
    SubProcess subProcess = (SubProcess) flowElement;
    assertEquals(11, subProcess.getFlowElements().size());

    // verify the sub process data objects
    dataObjects = subProcess.getDataObjects(); 
    assertEquals(6, dataObjects.size());
    
    objectMap = new HashMap<String, ValuedDataObject>();
    for (ValuedDataObject valueObj : dataObjects) {
      objectMap.put(valueObj.getId(), valueObj);
    }
    
    dataObj = objectMap.get("dObj7");
    assertEquals("dObj7", dataObj.getId());
    assertEquals("StringSubTest", dataObj.getName());
    assertEquals("xsd:string", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof String);
    assertEquals("Testing456", dataObj.getValue());
    
    dataObj = objectMap.get("dObj8");
    assertEquals("dObj8", dataObj.getId());
    assertEquals("BooleanSubTest", dataObj.getName());
    assertEquals("xsd:boolean", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof Boolean);
    assertEquals(new Boolean(false), dataObj.getValue());
    
    dataObj = objectMap.get("dObj9");
    assertEquals("dObj9", dataObj.getId());
    assertEquals("DateSubTest", dataObj.getName());
    assertEquals("xsd:datetime", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof Date);
    assertEquals("2013-11-11T22:00:00", sdf.format(dataObj.getValue()));
      
    dataObj = objectMap.get("dObj10");
    assertEquals("dObj10", dataObj.getId());
    assertEquals("DoubleSubTest", dataObj.getName());
    assertEquals("xsd:double", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof Double);
    assertEquals(new Double(678912345), dataObj.getValue());
    
    dataObj = objectMap.get("dObj11");
    assertEquals("dObj11", dataObj.getId());
    assertEquals("IntegerSubTest", dataObj.getName());
    assertEquals("xsd:int", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof Integer);
    assertEquals(new Integer(45), dataObj.getValue());
    
    dataObj = objectMap.get("dObj12");
    assertEquals("dObj12", dataObj.getId());
    assertEquals("LongSubTest", dataObj.getName());
    assertEquals("xsd:long", dataObj.getItemSubjectRef().getStructureRef());
    assertTrue(dataObj.getValue() instanceof Long);
    assertEquals(new Long(456123), dataObj.getValue());
  }
}
