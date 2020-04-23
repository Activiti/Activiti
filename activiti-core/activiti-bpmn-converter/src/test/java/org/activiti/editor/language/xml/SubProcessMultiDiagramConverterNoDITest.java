package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.SubprocessXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

public class SubProcessMultiDiagramConverterNoDITest extends AbstractConverterTest {

  @Override
  protected BpmnModel readXMLFile() throws Exception {
    InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream(getResource());
    XMLInputFactory xif = XMLInputFactory.newInstance();
    InputStreamReader in = new InputStreamReader(xmlStream, "UTF-8");
    XMLStreamReader xtr = xif.createXMLStreamReader(in);
    return new SubprocessXMLConverter().convertToBpmnModel(xtr);
  }

  @Override
  protected BpmnModel exportAndReadXMLFile(BpmnModel bpmnModel) throws Exception {
    byte[] xml = new SubprocessXMLConverter().convertToXML(bpmnModel);
    System.out.println("xml " + new String(xml, "UTF-8"));
    XMLInputFactory xif = XMLInputFactory.newInstance();
    InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(xml), "UTF-8");
    XMLStreamReader xtr = xif.createXMLStreamReader(in);
    return new SubprocessXMLConverter().convertToBpmnModel(xtr);
  }

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
    return "subprocessmultidiagrammodel-noDI.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("start1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(StartEvent.class);
    assertThat(flowElement.getId()).isEqualTo("start1");

    flowElement = model.getMainProcess().getFlowElement("userTask1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(UserTask.class);
    assertThat(flowElement.getId()).isEqualTo("userTask1");
    UserTask userTask = (UserTask) flowElement;
    assertThat(userTask.getCandidateUsers().size() == 1).isTrue();
    assertThat(userTask.getCandidateGroups().size() == 1).isTrue();

    flowElement = model.getMainProcess().getFlowElement("subprocess1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(SubProcess.class);
    assertThat(flowElement.getId()).isEqualTo("subprocess1");
    SubProcess subProcess = (SubProcess) flowElement;
    assertThat(subProcess.getFlowElements().size() == 11).isTrue();
  }
}
