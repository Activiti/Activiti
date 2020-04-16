package org.activiti.engine.test.bpmn.usertask;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.impl.util.io.StreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.Execution;

public class ImportExportTest extends ResourceActivitiTestCase {

    public ImportExportTest() {
        super("org/activiti/standalone/parsing/encoding.activiti.cfg.xml");
    }

    public void testConvertXMLToModel() throws Exception {
        BpmnModel bpmnModel = readXMLFile();
        bpmnModel = exportAndReadXMLFile(bpmnModel);

        byte[] xml = new BpmnXMLConverter().convertToXML(bpmnModel);

        processEngine.getRepositoryService().createDeployment().name("test1").addString("test1.bpmn20.xml",
                                                                                                                                               new String(xml)).deploy();

        String processInstanceKey = runtimeService.startProcessInstanceByKey("process").getId();
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstanceKey).messageEventSubscriptionName("InterruptMessage").singleResult();

        assertThat(execution).isNotNull();
    }

    protected void tearDown() throws Exception {
        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(),
                                               true);
        }
        super.tearDown();
    }

    protected String getResource() {
        return "org/activiti/engine/test/bpmn/usertask/ImportExportTest.testImportExport.bpmn20.xml";
    }

    protected BpmnModel readXMLFile() throws Exception {
        InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream(getResource());
        StreamSource xmlSource = new InputStreamSource(xmlStream);
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xmlSource,
                                                                        false,
                                                                        false,
                                                                        processEngineConfiguration.getXmlEncoding());
        return bpmnModel;
    }

    protected BpmnModel exportAndReadXMLFile(BpmnModel bpmnModel) throws Exception {
        byte[] xml = new BpmnXMLConverter().convertToXML(bpmnModel,
                                                         processEngineConfiguration.getXmlEncoding());
        StreamSource xmlSource = new InputStreamSource(new ByteArrayInputStream(xml));
        BpmnModel parsedModel = new BpmnXMLConverter().convertToBpmnModel(xmlSource,
                                                                          false,
                                                                          false,
                                                                          processEngineConfiguration.getXmlEncoding());
        return parsedModel;
    }
}
