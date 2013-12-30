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
package org.activiti.workflow.simple.alfresco.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionUtil;
import org.activiti.workflow.simple.alfresco.conversion.exception.AlfrescoSimpleWorkflowException;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.beans.Bean;
import org.activiti.workflow.simple.alfresco.model.beans.BeanProperty;
import org.activiti.workflow.simple.alfresco.model.beans.BeanPropertyProp;
import org.activiti.workflow.simple.alfresco.model.beans.Beans;
import org.activiti.workflow.simple.alfresco.model.config.AlfrescoConfiguration;
import org.activiti.workflow.simple.alfresco.model.config.Extension;
import org.activiti.workflow.simple.alfresco.model.config.Module;
import org.activiti.workflow.simple.alfresco.model.config.ModuleDeployment;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;

/**
 * Class capable of exporting Alfresco artifacts that are part of a
 * {@link WorkflowDefinitionConversion}, together with the required
 * configuration files in order to make the artifacts available in an Alfresco
 * installation.
 * 
 * @author Frederik Heremans
 */
public class AlfrescoArtifactExporter {

	protected static final String PROCESS_FILE_SUFFIX = ".bpmn20.xml";
	protected static final String MODEL_FILE_SUFFIX = "-model.xml";
	protected static final String SPRING_CONTEXT_FILE_SUFFIX = "-context.xml";
	protected static final String SHARE_CONFIG_FILE_SUFFIX = "-config-custom.xml";
	protected static final String SHARE_CONFIG_MODULE_DEPLOYMENT_SUFFIX = "-module-deployment.xml";
	protected static final String SHARE_CONTEXT_FILE_SUFFIX = "-share-context.xml";
	
	protected static final String WORKFLOW_DEPLOYER_PARENT = "workflowDeployer";
	protected static final String WORKFLOW_DEPLOYER_NAME_SUFFIX = ".workflowBootstrap";
	protected static final String PROPERTY_MODELS = "models";
	protected static final String PROPERTY_WORKFLOW_DEFINITIONS = "workflowDefinitions";
	protected static final String PROP_KEY_ENGINE_ID = "engineId";
	protected static final String PROP_KEY_LOCATION = "location";
	protected static final String PROP_KEY_MIME_TYPE = "mimeType";
	protected static final String PROP_KEY_REDEPLOY = "redeploy";
	protected static final String PROP_ENGINE_ID = "activiti";
	protected static final String PROP_MIME_TYPE = "text/xml";
	protected static final String PROPERTY_CONFIGS = "configs";
	protected static final String PROPERTY_CONFIG_SERVICE = "configService";
	protected static final String PROPERTY_CONFIG_SERVICE_REF = "web.config";
	protected static final String EXTENSIONS_PATH_PREFIX = "alfresco/extension/";
	protected static final String WEB_EXTENSIONS_PATH_PREFIX = "classpath:alfresco/web-extension/";
	protected static final String SHARE_CONFIG_BOOTSTRAP_BEAN_CLASS = "org.springframework.extensions.config.ConfigBootstrap";
	protected static final String SHARE_CONFIG_BOOTSTRAP_BEAN_INIT_METHOD = "register";

	
	protected BpmnXMLConverter bpmnConverter;
	protected JAXBContext modelJaxbContext;
	protected JAXBContext beansJaxbContext;
	protected JAXBContext moduleJaxbContext;

	public AlfrescoArtifactExporter() {
		// Initialize writers for artifacts
		bpmnConverter = new BpmnXMLConverter();
		try {
			modelJaxbContext = JAXBContext.newInstance(M2Model.class);
			moduleJaxbContext = JAXBContext.newInstance(Extension.class, Module.class, AlfrescoConfiguration.class, ModuleDeployment.class);
			beansJaxbContext = JAXBContext.newInstance(Beans.class);
		} catch (JAXBException jaxbe) {
			throw new AlfrescoSimpleWorkflowException("Error while building JAXB-context for exporting content-model", jaxbe);
		}
	}

	/**
	 * Export all artifacts and configuration-files needed to run the process that is converted in the given
	 * {@link WorkflowDefinitionConversion}.
	 * 
	 * @param conversion the conversion object to be exported
	 * @param repositoryFolder the folder where all repository-artifacts and configurations are exported to
	 * @param shareFolder the folder where all share-artifacts and configurations are exported to
	 */
	public void exportArtifacts(WorkflowDefinitionConversion conversion, File repositoryFolder, File shareFolder, boolean asExtension) {
			validateArtifactTargets(repositoryFolder, shareFolder);
		
			String processId = conversion.getProcess().getId();
			try {
				
				// Export process BPMN
				File processFile = new File(repositoryFolder, getBpmnFileName(conversion));
	      processFile.createNewFile();
	      FileOutputStream processStream = new FileOutputStream(processFile);
	      writeBpmnModel(processStream, conversion);
	      processStream.close();
	      
	      // Export content model
	      File contentModelFile = new File(repositoryFolder, getContentModelFileName(conversion));
	      contentModelFile.createNewFile();
	      FileOutputStream modelStream = new FileOutputStream(contentModelFile);
	      writeContentModel(modelStream, conversion);
	      modelStream.close();

	      // Export workflow deployer context XML
	      File springContextFile = new File(repositoryFolder, processId + SPRING_CONTEXT_FILE_SUFFIX);
	      springContextFile.createNewFile();
	      FileOutputStream springContextStream = new FileOutputStream(springContextFile);
	      writeWorkflowDeployerBean(springContextStream, processId, processFile.getName(), contentModelFile.getName());
	      springContextStream.close();
	      
	      // Export share config
	      File shareConfigFile = new File(shareFolder, getShareConfigFileName(conversion));
	      shareConfigFile.createNewFile();
	      FileOutputStream shareConfigStream = new FileOutputStream(shareConfigFile);
	      writeShareConfig(shareConfigStream, conversion, asExtension);
	      shareConfigStream.close();
	      
	      if(asExtension) {
	      	File shareModuleDeploymentFile = new File(shareFolder, getShareModuleDeploymentFileName(conversion));
		      shareModuleDeploymentFile.createNewFile();
		      FileOutputStream shareModuleStream = new FileOutputStream(shareModuleDeploymentFile);
		      writeShareExtensionModule(shareModuleStream, conversion);
		      shareModuleStream.close();
	      }
	      
	      // Export share custom context
	      File shareContextFile = new File(shareFolder, processId + SHARE_CONTEXT_FILE_SUFFIX);
	      shareContextFile.createNewFile();
	      FileOutputStream shareContextStream = new FileOutputStream(shareContextFile);
	      writeShareCustomConfigBean(shareContextStream, processId, shareConfigFile.getName());
	      shareContextStream.close();
	      
      } catch (IOException ioe) {
      	throw new AlfrescoSimpleWorkflowException("Error while exporting artifacts", ioe);
      }
	}
	
	public String getBpmnFileName(WorkflowDefinitionConversion conversion) {
		String processId = conversion.getProcess().getId();
		return processId + PROCESS_FILE_SUFFIX;
	}
	
	public String getContentModelFileName(WorkflowDefinitionConversion conversion) {
		String processId = conversion.getProcess().getId();
		return processId + MODEL_FILE_SUFFIX;
	}
	
	public String getShareConfigFileName(WorkflowDefinitionConversion conversion) {
		String processId = conversion.getProcess().getId();
		return processId + SHARE_CONFIG_FILE_SUFFIX;
	}
	
	public String getShareModuleDeploymentFileName(WorkflowDefinitionConversion conversion) {
		String processId = conversion.getProcess().getId();
		return processId + SHARE_CONFIG_MODULE_DEPLOYMENT_SUFFIX;
	}
	
	/**
	 * Write the BPMN-model in the given conversion to the given stream. 
	 */
	public void writeBpmnModel(OutputStream out, WorkflowDefinitionConversion conversion) throws IOException {
		BpmnModel model = conversion.getBpmnModel();
		byte[] xmlContent = bpmnConverter.convertToXML(model, "UTF-8");
		out.write(xmlContent);
	}
	
	/**
	 * Write the Share module XML in the given conversion to the given stream. 
	 */
	public void writeShareConfig(OutputStream out, WorkflowDefinitionConversion conversion, boolean asExtension) throws IOException {
		Extension extension = AlfrescoConversionUtil.getExtension(conversion);
		try {
			Object toMarshall = extension;
			
			// In case the configuration should NOT be exported as a module, wrap the configurations
			// in a "alfresco-configuration" element instead
			if(!asExtension) {
				toMarshall = new AlfrescoConfiguration();
				((AlfrescoConfiguration) toMarshall).setConfigurations(extension.getModules().get(0).getConfigurations());
			}
	    Marshaller marshaller = moduleJaxbContext.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    marshaller.marshal(toMarshall, out);
    } catch (JAXBException jaxbe) {
    	throw new IOException(jaxbe);
    }
	}
	
	/**
	 * Write the Share module XML in the given conversion to the given stream. 
	 */
	public void writeShareExtensionModule(OutputStream out, WorkflowDefinitionConversion conversion) throws IOException {
		Extension extension = AlfrescoConversionUtil.getExtension(conversion);
		try {
			ModuleDeployment toMarshall = new ModuleDeployment();
			toMarshall.setModule(extension.getModules().get(0).getId());
			// In case the configuration should NOT be exported as a module, wrap the configurations
			// in a "alfresco-configuration" element instead
	    Marshaller marshaller = moduleJaxbContext.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    marshaller.marshal(toMarshall, out);
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
	    Marshaller marshaller = modelJaxbContext.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    marshaller.marshal(model, out);
    } catch (JAXBException jaxbe) {
    	throw new IOException(jaxbe);
    }
	}
	
	protected void writeWorkflowDeployerBean(OutputStream out, String processId, String processFileName, String contentModelFileName) throws IOException {
		// Create root "beans" element
		Beans beans = new Beans();
    Bean workflowDeployer = new Bean(processId + WORKFLOW_DEPLOYER_NAME_SUFFIX, WORKFLOW_DEPLOYER_PARENT);
    beans.getBeans().add(workflowDeployer);
    
    // Add pointer to model
    BeanProperty modelsProperty = new BeanProperty(PROPERTY_MODELS);
    modelsProperty.addListItem(EXTENSIONS_PATH_PREFIX + contentModelFileName);
    workflowDeployer.getProperties().add(modelsProperty);
    
    // Add pointer to process definition
    BeanProperty workflowProperty = new BeanProperty(PROPERTY_WORKFLOW_DEFINITIONS);
    workflowProperty.addProp(new BeanPropertyProp(PROP_KEY_ENGINE_ID, PROP_ENGINE_ID));
    workflowProperty.addProp(new BeanPropertyProp(PROP_KEY_MIME_TYPE, PROP_MIME_TYPE));
    workflowProperty.addProp(new BeanPropertyProp(PROP_KEY_LOCATION, EXTENSIONS_PATH_PREFIX + processFileName));
    // TODO: set back to false or make configurable
    workflowProperty.addProp(new BeanPropertyProp(PROP_KEY_REDEPLOY, Boolean.TRUE.toString()));
    workflowDeployer.getProperties().add(workflowProperty);
    
    writeBeans(out, beans);
	}
	
	protected void writeShareCustomConfigBean(OutputStream out, String processId, String configFileName) throws IOException {
		Beans beans = new Beans();
		Bean shareCustomConfigBean = new Bean(processId + WORKFLOW_DEPLOYER_NAME_SUFFIX, null);
		shareCustomConfigBean.setBeanClass(SHARE_CONFIG_BOOTSTRAP_BEAN_CLASS);
		shareCustomConfigBean.setInitMethod(SHARE_CONFIG_BOOTSTRAP_BEAN_INIT_METHOD);
		BeanProperty configsProperty = new BeanProperty(PROPERTY_CONFIGS);
		configsProperty.addListItem(WEB_EXTENSIONS_PATH_PREFIX + configFileName);
		
		BeanProperty configServiceProperty = new BeanProperty(PROPERTY_CONFIG_SERVICE);
		configServiceProperty.setRef(PROPERTY_CONFIG_SERVICE_REF);
		shareCustomConfigBean.getProperties().add(configServiceProperty);
		
		shareCustomConfigBean.getProperties().add(configsProperty);
		beans.getBeans().add(shareCustomConfigBean);
		
		writeBeans(out, beans);
	}
	
	protected void writeBeans(OutputStream out, Beans beans) throws IOException {
		try {
    	Marshaller marshaller = beansJaxbContext.createMarshaller();
    	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    	marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd");
    	
    	marshaller.marshal(beans, out);
    } catch (JAXBException jaxbe) {
    	throw new IOException(jaxbe);
    }
	}
	
	protected void validateArtifactTargets(File repositoryFolder, File shareFolder) {
		if(repositoryFolder == null || shareFolder == null) {
			throw new AlfrescoSimpleWorkflowException("Both repositoryFolder and shareFolder are required.");
		}
		if(!repositoryFolder.exists()) {
			throw new AlfrescoSimpleWorkflowException("The repository target folder does not exist: " + repositoryFolder.getAbsolutePath());
		}
		if(!shareFolder.exists()) {
			throw new AlfrescoSimpleWorkflowException("The share target folder does not exist: " + shareFolder.getAbsolutePath());
		}
  }
	
}
