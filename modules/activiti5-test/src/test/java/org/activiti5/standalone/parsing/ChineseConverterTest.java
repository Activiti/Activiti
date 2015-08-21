package org.activiti5.standalone.parsing;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentProperties;
import org.activiti5.engine.impl.test.ResourceActivitiTestCase;
import org.activiti5.engine.impl.util.io.InputStreamSource;
import org.activiti5.engine.impl.util.io.StreamSource;

public class ChineseConverterTest extends ResourceActivitiTestCase {
  
  public ChineseConverterTest() {
    super("org/activiti5/standalone/parsing/encoding.activiti.cfg.xml");
  }
  
  public void testConvertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    bpmnModel = exportAndReadXMLFile(bpmnModel);
    deployProcess(bpmnModel);
  }
  
  protected String getResource() {
    return "org/activiti5/standalone/parsing/chinese.bpmn";
  }
  
  protected BpmnModel readXMLFile() throws Exception {
    InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream(getResource());
    StreamSource xmlSource = new InputStreamSource(xmlStream);
    BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xmlSource, false, false, processEngineConfiguration.getXmlEncoding());
    return bpmnModel;
  }
  
  protected BpmnModel exportAndReadXMLFile(BpmnModel bpmnModel) throws Exception {
    byte[] xml = new BpmnXMLConverter().convertToXML(bpmnModel, processEngineConfiguration.getXmlEncoding());
    StreamSource xmlSource = new InputStreamSource(new ByteArrayInputStream(xml));
    BpmnModel parsedModel = new BpmnXMLConverter().convertToBpmnModel(xmlSource, false, false, processEngineConfiguration.getXmlEncoding());
    return parsedModel;
  }
  
  protected void deployProcess(BpmnModel bpmnModel)  {
    byte[] xml = new BpmnXMLConverter().convertToXML(bpmnModel);
    try {
      Deployment deployment = processEngine.getRepositoryService().createDeployment()
          .name("test")
          .deploymentProperty(DeploymentProperties.DEPLOY_AS_ACTIVITI5_PROCESS_DEFINITION, Boolean.TRUE)
          .addString("test.bpmn20.xml", new String(xml))
          .deploy();
      processEngine.getRepositoryService().deleteDeployment(deployment.getId());
    } finally {
      processEngine.close();
    }
  }
}
