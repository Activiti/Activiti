package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.bpmn.model.ValuedDataObject;
import org.junit.Test;

/**
 * @see <a href="https://activiti.atlassian.net/browse/ACT-1847">https://activiti.atlassian.net/browse/ACT-1847</a>
 */
public class DataObjectConverterTest extends AbstractConverterTest {

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
    return "dataobjectmodel.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("start1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof StartEvent);
    assertEquals("start1", flowElement.getId());

    // verify the main process data objects
    List<ValuedDataObject> dataObjects = model.getMainProcess().getDataObjects();
    assertEquals(7, dataObjects.size());
    
    Map<String, ValuedDataObject> objectMap = new HashMap<String, ValuedDataObject>();
    for (ValuedDataObject valueObj : dataObjects) {
      objectMap.put(valueObj.getId(), valueObj);
    }
    
    ValuedDataObject dataObj = objectMap.get("dObj1");
    assertEquals("dObj1", dataObj.getId());
    assertEquals("StringTest", dataObj.getName());
    assertEquals("xsd:string", dataObj.getItemSubjectRef().getStructureRef());
    
    dataObj = objectMap.get("dObj2");
    assertEquals("BooleanTest", dataObj.getName());
    assertEquals("xsd:boolean", dataObj.getItemSubjectRef().getStructureRef());
    
    dataObj = objectMap.get("dObj3");
    assertEquals("DateTest", dataObj.getName());
    assertEquals("xsd:datetime", dataObj.getItemSubjectRef().getStructureRef());
   
    dataObj = objectMap.get("dObj4");
    assertEquals("DoubleTest", dataObj.getName());
    assertEquals("xsd:double", dataObj.getItemSubjectRef().getStructureRef());
    
    dataObj = objectMap.get("dObj5");
    assertEquals("IntegerTest", dataObj.getName());
    assertEquals("xsd:int", dataObj.getItemSubjectRef().getStructureRef());
    
    dataObj = objectMap.get("dObj6");
    assertEquals("LongTest", dataObj.getName());
    assertEquals("xsd:long", dataObj.getItemSubjectRef().getStructureRef());
    
    dataObj = objectMap.get("dObjWithoutType");
    assertEquals("UnknownTypeTest", dataObj.getName());
    assertEquals("xsd:string", dataObj.getItemSubjectRef().getStructureRef());
    
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
    
    dataObj = objectMap.get("dObj8");
    assertEquals("dObj8", dataObj.getId());
    assertEquals("BooleanSubTest", dataObj.getName());
    assertEquals("xsd:boolean", dataObj.getItemSubjectRef().getStructureRef());
    
    dataObj = objectMap.get("dObj9");
    assertEquals("dObj9", dataObj.getId());
    assertEquals("DateSubTest", dataObj.getName());
    assertEquals("xsd:datetime", dataObj.getItemSubjectRef().getStructureRef());
    
    dataObj = objectMap.get("dObj10");
    assertEquals("dObj10", dataObj.getId());
    assertEquals("DoubleSubTest", dataObj.getName());
    assertEquals("xsd:double", dataObj.getItemSubjectRef().getStructureRef());
      
    dataObj = objectMap.get("dObj11");
    assertEquals("dObj11", dataObj.getId());
    assertEquals("IntegerSubTest", dataObj.getName());
    assertEquals("xsd:int", dataObj.getItemSubjectRef().getStructureRef());
    
    dataObj = objectMap.get("dObj12");
    assertEquals("dObj12", dataObj.getId());
    assertEquals("LongSubTest", dataObj.getName());
    assertEquals("xsd:long", dataObj.getItemSubjectRef().getStructureRef());
  }
}
