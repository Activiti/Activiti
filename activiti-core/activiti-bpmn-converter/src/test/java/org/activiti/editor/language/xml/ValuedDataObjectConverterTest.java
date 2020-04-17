package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.api.Test;

/**
 * @see https://activiti.atlassian.net/browse/ACT-1847
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
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(StartEvent.class);
    assertThat(flowElement.getId()).isEqualTo("start1");

    // verify the main process data objects
    List<ValuedDataObject> dataObjects = model.getProcess(null).getDataObjects();
    assertThat(dataObjects).hasSize(7);

    Map<String, ValuedDataObject> objectMap = new HashMap<String, ValuedDataObject>();
    for (ValuedDataObject valueObj : dataObjects) {
      objectMap.put(valueObj.getId(), valueObj);
    }

    ValuedDataObject dataObj = objectMap.get("dObj1");
    assertThat(dataObj.getId()).isEqualTo("dObj1");
    assertThat(dataObj.getName()).isEqualTo("StringTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:string");
    assertThat(dataObj.getValue()).isInstanceOf(String.class);
    assertThat(dataObj.getValue()).isEqualTo("Testing1&2&3");

    dataObj = objectMap.get("dObj2");
    assertThat(dataObj.getId()).isEqualTo("dObj2");
    assertThat(dataObj.getName()).isEqualTo("BooleanTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:boolean");
    assertThat(dataObj.getValue()).isInstanceOf(Boolean.class);
    assertThat(dataObj.getValue()).isEqualTo(Boolean.TRUE);

    dataObj = objectMap.get("dObj3");
    assertThat(dataObj.getId()).isEqualTo("dObj3");
    assertThat(dataObj.getName()).isEqualTo("DateTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:datetime");
    assertThat(dataObj.getValue()).isInstanceOf(Date.class);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    assertThat(sdf.format(dataObj.getValue())).isEqualTo("2013-09-16T11:23:00");

    dataObj = objectMap.get("dObj4");
    assertThat(dataObj.getId()).isEqualTo("dObj4");
    assertThat(dataObj.getName()).isEqualTo("DoubleTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:double");
    assertThat(dataObj.getValue()).isInstanceOf(Double.class);
    assertThat(dataObj.getValue()).isEqualTo(new Double(123456789));

    dataObj = objectMap.get("dObj5");
    assertThat(dataObj.getId()).isEqualTo("dObj5");
    assertThat(dataObj.getName()).isEqualTo("IntegerTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:int");
    assertThat(dataObj.getValue()).isInstanceOf(Integer.class);
    assertThat(dataObj.getValue()).isEqualTo(Integer.valueOf(123));

    dataObj = objectMap.get("dObj6");
    assertThat(dataObj.getId()).isEqualTo("dObj6");
    assertThat(dataObj.getName()).isEqualTo("LongTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:long");
    assertThat(dataObj.getValue()).isInstanceOf(Long.class);
    assertThat(dataObj.getValue()).isEqualTo(new Long(-123456));
    assertThat(dataObj.getExtensionElements()).hasSize(1);
    List<ExtensionElement> testValues = dataObj.getExtensionElements().get("testvalue");
    assertThat(testValues).isNotNull();
    assertThat(testValues).hasSize(1);
    assertThat(testValues.get(0).getName()).isEqualTo("testvalue");
    assertThat(testValues.get(0).getElementText()).isEqualTo("test");

    dataObj = objectMap.get("NoData");
    assertThat(dataObj.getId()).isEqualTo("NoData");
    assertThat(dataObj.getName()).isEqualTo("NoData");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:datetime");
    assertThat(dataObj.getValue()).isNull();

    flowElement = model.getMainProcess().getFlowElement("userTask1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(UserTask.class);
    assertThat(flowElement.getId()).isEqualTo("userTask1");
    UserTask userTask = (UserTask) flowElement;
    assertThat(userTask.getAssignee()).isEqualTo("kermit");

    flowElement = model.getMainProcess().getFlowElement("subprocess1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(SubProcess.class);
    assertThat(flowElement.getId()).isEqualTo("subprocess1");
    SubProcess subProcess = (SubProcess) flowElement;
    assertThat(subProcess.getFlowElements()).hasSize(11);

    // verify the sub process data objects
    dataObjects = subProcess.getDataObjects();
    assertThat(dataObjects).hasSize(6);

    objectMap = new HashMap<String, ValuedDataObject>();
    for (ValuedDataObject valueObj : dataObjects) {
      objectMap.put(valueObj.getId(), valueObj);
    }

    dataObj = objectMap.get("dObj7");
    assertThat(dataObj.getId()).isEqualTo("dObj7");
    assertThat(dataObj.getName()).isEqualTo("StringSubTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:string");
    assertThat(dataObj.getValue()).isInstanceOf(String.class);
    assertThat(dataObj.getValue()).isEqualTo("Testing456");

    dataObj = objectMap.get("dObj8");
    assertThat(dataObj.getId()).isEqualTo("dObj8");
    assertThat(dataObj.getName()).isEqualTo("BooleanSubTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:boolean");
    assertThat(dataObj.getValue()).isInstanceOf(Boolean.class);
    assertThat(dataObj.getValue()).isEqualTo(Boolean.FALSE);

    dataObj = objectMap.get("dObj9");
    assertThat(dataObj.getId()).isEqualTo("dObj9");
    assertThat(dataObj.getName()).isEqualTo("DateSubTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:datetime");
    assertThat(dataObj.getValue()).isInstanceOf(Date.class);
    assertThat(sdf.format(dataObj.getValue())).isEqualTo("2013-11-11T22:00:00");

    dataObj = objectMap.get("dObj10");
    assertThat(dataObj.getId()).isEqualTo("dObj10");
    assertThat(dataObj.getName()).isEqualTo("DoubleSubTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:double");
    assertThat(dataObj.getValue()).isInstanceOf(Double.class);
    assertThat(dataObj.getValue()).isEqualTo(new Double(678912345));

    dataObj = objectMap.get("dObj11");
    assertThat(dataObj.getId()).isEqualTo("dObj11");
    assertThat(dataObj.getName()).isEqualTo("IntegerSubTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:int");
    assertThat(dataObj.getValue()).isInstanceOf(Integer.class);
    assertThat(dataObj.getValue()).isEqualTo(Integer.valueOf(45));

    dataObj = objectMap.get("dObj12");
    assertThat(dataObj.getId()).isEqualTo("dObj12");
    assertThat(dataObj.getName()).isEqualTo("LongSubTest");
    assertThat(dataObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:long");
    assertThat(dataObj.getValue()).isInstanceOf(Long.class);
    assertThat(dataObj.getValue()).isEqualTo(new Long(456123));
  }
}
