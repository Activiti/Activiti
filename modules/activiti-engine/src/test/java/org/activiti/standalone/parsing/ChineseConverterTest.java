package org.activiti.standalone.parsing;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;

public class ChineseConverterTest extends PluggableActivitiTestCase {
  
  public void testConvertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    deployProcess(bpmnModel);
  }
  
  protected String getResource() {
    return "chinese.bpmn";
  }
  
  protected BpmnModel readXMLFile() throws Exception {
    InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream(getResource());
    XMLInputFactory xif = XMLInputFactory.newInstance();
    InputStreamReader in = new InputStreamReader(xmlStream, "UTF-8");
    XMLStreamReader xtr = xif.createXMLStreamReader(in);
    BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
    return bpmnModel;
  }
  
  protected BpmnModel exportAndReadXMLFile(BpmnModel bpmnModel) throws Exception {
    byte[] xml = new BpmnXMLConverter().convertToXML(bpmnModel);
    System.out.println("xml " + new String(xml, "UTF-8"));
    XMLInputFactory xif = XMLInputFactory.newInstance();
    InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(xml), "UTF-8");
    XMLStreamReader xtr = xif.createXMLStreamReader(in);
    BpmnModel parsedModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
    return parsedModel;
  }
  
  protected void deployProcess(BpmnModel bpmnModel)  {
    byte[] xml = new BpmnXMLConverter().convertToXML(bpmnModel);
    try {
      Deployment deployment = processEngine.getRepositoryService().createDeployment().name("test").addString("test.bpmn20.xml", new String(xml)).deploy();
      processEngine.getRepositoryService().deleteDeployment(deployment.getId());
    } finally {
      processEngine.close();
    }
  }
}
