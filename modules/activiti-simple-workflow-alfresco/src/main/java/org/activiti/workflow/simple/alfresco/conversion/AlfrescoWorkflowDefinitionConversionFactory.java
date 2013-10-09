/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.workflow.simple.alfresco.conversion;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.workflow.simple.alfresco.configmodel.Module;
import org.activiti.workflow.simple.alfresco.conversion.exception.AlfrescoSimpleWorkflowException;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.apache.commons.io.IOUtils;

/**
 * {@link WorkflowDefinitionConversionFactory} which has additional listeners which
 * creates specific artifacts to use the workflowdefinition in Alfresco.
 * 
 * @author Frederik Heremans
 *
 */
public class AlfrescoWorkflowDefinitionConversionFactory extends WorkflowDefinitionConversionFactory {

  private static final long serialVersionUID = 1L;
  
  protected BpmnXMLConverter bpmnConverter;
  protected JAXBContext contentModelContext;
  protected JAXBContext moduleContext;

	public AlfrescoWorkflowDefinitionConversionFactory() {
		super();

		// Initialize writers for artifacts
		bpmnConverter = new BpmnXMLConverter();
		try {
	    contentModelContext = JAXBContext.newInstance(M2Model.class);
    } catch (JAXBException jaxbe) {
    	throw new AlfrescoSimpleWorkflowException(
    			"Error while building JAXB-context for exporting content-model", jaxbe);
    }
		
		try {
			moduleContext = JAXBContext.newInstance(Module.class);
    } catch (JAXBException jaxbe) {
    	throw new AlfrescoSimpleWorkflowException(
    			"Error while building JAXB-context for exporting share-module", jaxbe);
    }
		
		// Add additional listeners for Alfresco-specific listeners
		defaultWorkflowDefinitionConversionListeners.add(new InitializeAlfrescoModelsConversionListener());
		
		// Custom step converters
		defaultStepConverters.put(HumanStepDefinition.class, new AlfrescoHumanStepDefinitionConverter());
  }
	
	/**
	 * Write the BPMN-model in the given conversion to the given stream. 
	 */
	public void writeBpmnModel(OutputStream out, WorkflowDefinitionConversion conversion) throws IOException {
		BpmnModel model = conversion.getBpmnModel();
		byte[] xmlContent = bpmnConverter.convertToXML(model, "UTF-8");
		IOUtils.write(xmlContent, out);
	}
	
	/**
	 * Write the Share module XML in the given conversion to the given stream. 
	 */
	public void writeModule(OutputStream out, WorkflowDefinitionConversion conversion) throws IOException {
		Module module = AlfrescoConversionUtil.getModule(conversion);
		try {
	    Marshaller marshaller = moduleContext.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    marshaller.marshal(module, out);
    } catch (JAXBException jaxbe) {
    	throw new IOException(jaxbe);
    }
	}
	
	/**
	 * Write the content model XML in the given conversion to the given stream. 
	 */
	public void writeContentModel(OutputStream out, WorkflowDefinitionConversion conversion) throws IOException {
		M2Model model = AlfrescoConversionUtil.getContentModel(conversion);
		try {
	    Marshaller marshaller = contentModelContext.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    marshaller.marshal(model, out);
    } catch (JAXBException jaxbe) {
    	throw new IOException(jaxbe);
    }
	}
	
}
