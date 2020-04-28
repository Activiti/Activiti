package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.bpmn.model.BooleanDataObject;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DateDataObject;
import org.activiti.bpmn.model.DoubleDataObject;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IntegerDataObject;
import org.activiti.bpmn.model.LongDataObject;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.StringDataObject;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.ValuedDataObject;
import org.junit.jupiter.api.Test;

public class ValuedDataObjectConverterTest extends AbstractConverterTest {

  @Test
  public void convertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  protected String getResource() {
    return "test.valueddataobjectmodel.json";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("start1", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(StartEvent.class);
    assertThat(flowElement.getId()).isEqualTo("start1");

    // verify main process data objects
    List<ValuedDataObject> dataObjects = model.getMainProcess().getDataObjects();
    assertThat(dataObjects).hasSize(6);
    for (ValuedDataObject dObj : dataObjects) {
      if ("dObj1".equals(dObj.getId())) {
        assertThat(dObj.getClass()).isEqualTo(StringDataObject.class);
        assertThat(dObj.getName()).isEqualTo("StringTest");
        assertThat(dObj.getValue()).isInstanceOf(String.class);
        assertThat(dObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:string");
        assertThat(dObj.getValue()).isEqualTo("Testing123");
      } else if ("dObj2".equals(dObj.getId())) {
        assertThat(dObj.getClass()).isEqualTo(BooleanDataObject.class);
        assertThat(dObj.getName()).isEqualTo("BooleanTest");
        assertThat(dObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:boolean");
        assertThat(dObj.getValue()).isInstanceOf(Boolean.class);
        assertThat(dObj.getValue()).isEqualTo(Boolean.TRUE);
      } else if ("dObj3".equals(dObj.getId())) {
        assertThat(dObj.getClass()).isEqualTo(DateDataObject.class);
        assertThat(dObj.getName()).isEqualTo("DateTest");
        assertThat(dObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:datetime");
        assertThat(dObj.getValue()).isInstanceOf(Date.class);
        GregorianCalendar dateCal = new GregorianCalendar();
        dateCal.setTime((Date) dObj.getValue());
        assertThat(dateCal.get(Calendar.YEAR)).isEqualTo(2013);
        assertThat(dateCal.get(Calendar.MONTH)).isEqualTo(8);
        assertThat(dateCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(16);
        assertThat(dateCal.get(Calendar.HOUR_OF_DAY)).isEqualTo(11);
        assertThat(dateCal.get(Calendar.MINUTE)).isEqualTo(23);
      } else if ("dObj4".equals(dObj.getId())) {
        assertThat(dObj.getClass()).isEqualTo(DoubleDataObject.class);
        assertThat(dObj.getName()).isEqualTo("DoubleTest");
        assertThat(dObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:double");
        assertThat(dObj.getValue()).isInstanceOf(Double.class);
        assertThat(dObj.getValue()).isEqualTo(new Double(123456789));
      } else if ("dObj5".equals(dObj.getId())) {
        assertThat(dObj.getClass()).isEqualTo(IntegerDataObject.class);
        assertThat(dObj.getName()).isEqualTo("IntegerTest");
        assertThat(dObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:int");
        assertThat(dObj.getValue()).isInstanceOf(Integer.class);
        assertThat(dObj.getValue()).isEqualTo(Integer.valueOf(123));
      } else if ("dObj6".equals(dObj.getId())) {
        assertThat(dObj.getClass()).isEqualTo(LongDataObject.class);
        assertThat(dObj.getName()).isEqualTo("LongTest");
        assertThat(dObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:long");
        assertThat(dObj.getValue()).isInstanceOf(Long.class);
        assertThat(dObj.getValue()).isEqualTo(new Long(-123456));
      }
    }

    flowElement = model.getMainProcess().getFlowElement("subprocess1", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(SubProcess.class);
    assertThat(flowElement.getId()).isEqualTo("subprocess1");
    SubProcess subProcess = (SubProcess) flowElement;
    assertThat(subProcess.getFlowElements()).hasSize(11);

    // verify subprocess data objects
    dataObjects = ((SubProcess) flowElement).getDataObjects();
    assertThat(dataObjects).hasSize(6);
    for (ValuedDataObject dObj : dataObjects) {
      if ("dObj1".equals(dObj.getId())) {
        assertThat(dObj.getClass()).isEqualTo(StringDataObject.class);
        assertThat(dObj.getName()).isEqualTo("SubStringTest");
        assertThat(dObj.getValue()).isInstanceOf(String.class);
        assertThat(dObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:string");
        assertThat(dObj.getValue()).isEqualTo("Testing456");
      } else if ("dObj2".equals(dObj.getId())) {
        assertThat(dObj.getClass()).isEqualTo(BooleanDataObject.class);
        assertThat(dObj.getName()).isEqualTo("SubBooleanTest");
        assertThat(dObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:boolean");
        assertThat(dObj.getValue()).isInstanceOf(Boolean.class);
        assertThat(dObj.getValue()).isEqualTo(Boolean.FALSE);
      } else if ("dObj3".equals(dObj.getId())) {
        assertThat(dObj.getClass()).isEqualTo(DateDataObject.class);
        assertThat(dObj.getName()).isEqualTo("SubDateTest");
        assertThat(dObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:datetime");
        assertThat(dObj.getValue()).isInstanceOf(Date.class);
        GregorianCalendar dateCal = new GregorianCalendar();
        dateCal.setTime((Date) dObj.getValue());
        assertThat(dateCal.get(Calendar.YEAR)).isEqualTo(2013);
        assertThat(dateCal.get(Calendar.MONTH)).isEqualTo(10);
        assertThat(dateCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(11);
      } else if ("dObj4".equals(dObj.getId())) {
        assertThat(dObj.getClass()).isEqualTo(DoubleDataObject.class);
        assertThat(dObj.getName()).isEqualTo("SubDoubleTest");
        assertThat(dObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:double");
        assertThat(dObj.getValue()).isInstanceOf(Double.class);
        assertThat(dObj.getValue()).isEqualTo(new Double(678912345));
      } else if ("dObj5".equals(dObj.getId())) {
        assertThat(dObj.getClass()).isEqualTo(IntegerDataObject.class);
        assertThat(dObj.getName()).isEqualTo("SubIntegerTest");
        assertThat(dObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:int");
        assertThat(dObj.getValue()).isInstanceOf(Integer.class);
        assertThat(dObj.getValue()).isEqualTo(Integer.valueOf(45));
      } else if ("dObj6".equals(dObj.getId())) {
        assertThat(dObj.getClass()).isEqualTo(LongDataObject.class);
        assertThat(dObj.getName()).isEqualTo("SubLongTest");
        assertThat(dObj.getItemSubjectRef().getStructureRef()).isEqualTo("xsd:long");
        assertThat(dObj.getValue()).isInstanceOf(Long.class);
        assertThat(dObj.getValue()).isEqualTo(new Long(456123));
        assertThat(dObj.getExtensionElements()).hasSize(1);
      }
    }
  }
}
