package org.activiti5.engine.test.bpmn.usertask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.repository.DeploymentProperties;
import org.activiti.engine.runtime.Execution;
import org.activiti5.engine.impl.test.ResourceActivitiTestCase;
import org.activiti5.engine.impl.util.io.InputStreamSource;
import org.activiti5.engine.impl.util.io.StreamSource;

/**
 * Created by p3700487 on 23/02/15.
 */
public class ImportExportTest extends ResourceActivitiTestCase {

    public ImportExportTest() {
        super("org/activiti5/standalone/parsing/encoding.activiti.cfg.xml");
    }

    public void testConvertXMLToModel() throws Exception {
        BpmnModel bpmnModel = readXMLFile();
        bpmnModel = exportAndReadXMLFile(bpmnModel);

        byte[] xml = new BpmnXMLConverter().convertToXML(bpmnModel);

        processEngine.getRepositoryService().createDeployment().name("test1").addString("test1.bpmn20.xml", new String(xml))
            .deploymentProperty(DeploymentProperties.DEPLOY_AS_ACTIVITI5_PROCESS_DEFINITION, Boolean.TRUE)
            .deploy();

        String processInstanceKey = runtimeService.startProcessInstanceByKey("process").getId();
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstanceKey).messageEventSubscriptionName("InterruptMessage").singleResult();

        assertNotNull(execution);
    }

    protected void tearDown() throws Exception {
        for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
        super.tearDown();
    }


    protected String getResource() {
        return "org/activiti5/engine/test/bpmn/usertask/ImportExportTest.testImportExport.bpmn20.xml";
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

}
