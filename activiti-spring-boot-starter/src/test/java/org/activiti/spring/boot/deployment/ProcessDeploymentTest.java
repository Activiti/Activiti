package org.activiti.spring.boot.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.boot.ActivitiProperties;
import org.activiti.spring.boot.ProcessDefinitionResourceFinderDescriptor;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.core.common.spring.connector.ConnectorResourceFinderDescriptor;
import org.activiti.spring.process.ProcessExtensionResourceFinderDescriptor;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.types.VariableType;
import org.activiti.spring.resources.ResourceFinder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessDeploymentTest {

    private static final String RESOURCE_CATEGORIZE_IMAGE_CONNECTORS_PROCESS = "categorize-image-connectors.bpmn20.xml";
    private static final String CATEGORIZE_IMAGE_CONNECTORS_PROCESS = "categorizeProcessConnectors";
    
    private static final String RESOURCE_INITIAL_VARS_PROCESS = "initial-vars.bpmn20.xml";
    private static final String INITIAL_VARS_PROCESS = "initialVarsProcess";
    
    
    private String processDefinitionLocation = "processes/";
    private String processDefinitionLocationPrefix = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "**/"+processDefinitionLocation;
    private String connectorLocation = "connectors/";
    private String connectorPrefix = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "**/"+connectorLocation;
    private String connectorLocationSuffix = "**.json";
    
    @Autowired
    private RepositoryService repositoryService;
    
    @Autowired
    ResourceFinder resourceFinder;
    
    @Autowired
    private ProcessDefinitionResourceFinderDescriptor processDefinitionResourceFinder;
    
    @Autowired
    ProcessExtensionResourceFinderDescriptor processExtensionResourceFinder;
    
    @Autowired
    ConnectorResourceFinderDescriptor processConnectorResourceFinder;
    
    @Autowired
    private ResourcePatternResolver resourceLoader;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    @SpyBean
    private ActivitiProperties activitiProperties;
    
    private Map<String, VariableType> variableTypeMap = new HashMap<>();
    
    //deploymentId,     processDefinitionKey, ProcessExtensionModel
    private Map<String, Map<String,ProcessExtensionModel>> processExtensionModelDeploymentMap = new HashMap<>();
    
    private Map<String, List<ConnectorDefinition>> processConnectorDefinitionsMap = new HashMap<>();
    

    @Autowired
    private ProcessRuntime processRuntime;

    @Test
    public void shouldAddConnectorResourcesToDeploymentResourceLoader() throws Exception{
              
        List<Resource> connectors = new ArrayList<>();
     
        String path = processDefinitionLocationPrefix + RESOURCE_CATEGORIZE_IMAGE_CONNECTORS_PROCESS;
        Resource xmlResource = resourceLoader.getResource(path);        
            
        path = connectorPrefix + connectorLocationSuffix;
        connectors.addAll(Arrays.asList(resourceLoader.getResources(path)));        
        assertThat(connectors.size()>1).isTrue();
        
        DeploymentBuilder deploymentBuilder = repositoryService
                                                .createDeployment()
                                                .name("deploymentName");
        deploymentBuilder.addClasspathResource(processDefinitionLocation+xmlResource.getFilename());
        
        //All all found connectors
        for (Resource r: connectors) {
            deploymentBuilder
            .addInputStream(connectorLocation+r.getFilename(), r.getInputStream());
        }
        
      
        Deployment deployment = deploymentBuilder.deploy();
        assertNotNull(deployment);
        
        
        String deploymentId = deployment.getId();
         
        for (Resource r: connectors) {
            InputStream resourceStream = repositoryService.getResourceAsStream(deploymentId, connectorLocation + r.getFilename());
            assertThat(resourceStream).isEqualTo(r.getInputStream());
        }
        

        repositoryService.deleteDeployment(deploymentId);
    }
    
    
    @Test
    public void shouldCreateDeploymentsWithProcessExtensions() throws Exception{
        variableTypeMap.clear();
        
        //Get all procssDefinitions
        Map<String, String> deployedProcess = new HashMap<>();
        List<Resource> processDefinitionResources = resourceFinder.discoverResources(processDefinitionResourceFinder);
        assertThat(processDefinitionResources.size()>1).isTrue();
        
        //Get all extensions
        Map<String, Resource> processExtensions = readProcessExtensions();
        assertThat(processExtensions.size()>1).isTrue();
        
        //Deploy processes
        for (Resource r: processDefinitionResources) {
            Entry<String, String> entry = deployProcessFromResource(r, processExtensions);
            deployedProcess.put(entry.getKey(), entry.getValue());
        }
           
        //Check process definitions
        for (Map.Entry<String, String> entry : deployedProcess.entrySet()) {
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(entry.getKey()).singleResult();
            assertThat(deployment.getKey()).isEqualTo(entry.getValue());
            Optional<String> processExtensionName = getProcessExtension(deployment.getId());
            if (processExtensionName.isPresent()) {
                InputStream deploymentResourceInputStream = repositoryService.getResourceAsStream(deployment.getId(), processExtensionName.get());
                assertNotNull(deploymentResourceInputStream);
                
                ProcessExtensionModel deploymentExtensionModel = readAndConvertVariables(deploymentResourceInputStream);
                assertNotNull(deploymentExtensionModel);
            }
         }
        
        
        //Clear all
        for (Map.Entry<String, String> entry : deployedProcess.entrySet()) {
            repositoryService.deleteDeployment(entry.getKey());
        }
        variableTypeMap.clear();
    }
    
    
    @Test
    public void shouldStartTheProcessWithExtension() throws Exception{
  
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(INITIAL_VARS_PROCESS).singleResult();
        assertNotNull(definition);
        
        ProcessExtensionModel processExtensionModel = getProcessExtension(definition);
        assertNotNull(processExtensionModel);
        
        //Check that now it is in cache
        processExtensionModel = getProcessExtension(definition);
        assertNotNull(processExtensionModel);
        
        
        securityUtil.logInAs("salaboy");
        
        // start a process with vars then check default and specified vars exist
        ProcessInstance initialVarsProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(INITIAL_VARS_PROCESS)
                .withVariable("extraVar",
                        true)
                .withVariable("age",
                        10)
                .withBusinessKey("my business key")
                .build());

        assertThat(initialVarsProcess).isNotNull();
        assertThat(initialVarsProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);

        List<VariableInstance> variableInstances = processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(initialVarsProcess).build());

        assertThat(variableInstances).isNotNull();
        assertThat(variableInstances).hasSize(4);

        assertThat(variableInstances).extracting("name")
                .contains("extraVar", "name", "age", "birth");
        
     // cleanup
        processRuntime.delete(ProcessPayloadBuilder.delete(initialVarsProcess));
    }
    
    @Test
    public void shouldLoadAndFilterProcessConnectors() throws Exception{
        
        //Get all connectors
        List<Resource> processConnectors = resourceFinder.discoverResources(processConnectorResourceFinder);
        assertTrue(processConnectors.size()>1);
        
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(INITIAL_VARS_PROCESS).singleResult();
        assertNotNull(definition);
        
        String deploymentId = definition.getDeploymentId();
        
        List<ConnectorDefinition> connectorDefinitionList = getProcessConnectorDefinitionsForDeploymentId(deploymentId);
        assertTrue(connectorDefinitionList.size()>1);
        
 
    }
    
    private Optional<String> getProcessExtension(String deploymentId) {
        
        List <String> resourceNames = repositoryService.getDeploymentResourceNames(deploymentId);
        if (resourceNames != null) {
            return resourceNames.stream()
                        .filter(s -> s.contains("-extensions.json"))
                        .findFirst();
        }
        return Optional.empty();
    }
    
    
   private ProcessExtensionModel getProcessExtension(ProcessDefinition definition) {
       String deploymentId = definition.getDeploymentId();
       String processDefinitionKey = definition.getKey();
       
       Map<String,ProcessExtensionModel> processExtensionModelMap = getProcessExtensionsForDeploymentId(deploymentId);
       return processExtensionModelMap.get(processDefinitionKey);
   }
   
   
   //Do this once for each deplymentId
   private Map<String,ProcessExtensionModel> getProcessExtensionsForDeploymentId(String deploymentId) {
       
       Map<String,ProcessExtensionModel> processExtensionModelMap = processExtensionModelDeploymentMap.get(deploymentId);
       
       if (processExtensionModelMap != null) {
           return processExtensionModelMap;
       }
          
       processExtensionModelMap = new HashMap<>();
       
       List <String> resourceNames = repositoryService.getDeploymentResourceNames(deploymentId);
       
       if (resourceNames != null && !resourceNames.isEmpty()) {
           
           List <String> processExtensionNames = resourceNames.stream()
                                                   .filter(s -> s.contains("-extensions.json"))
                                                   .collect(Collectors.toList());
           
           if (processExtensionNames != null && !processExtensionNames.isEmpty()) {
               for (String name:processExtensionNames) {
                   try {
                       ProcessExtensionModel processExtensionModel = read(repositoryService.getResourceAsStream(deploymentId, name));
                       if (processExtensionModel != null) {
                           processExtensionModelMap.put(processExtensionModel.getId(), processExtensionModel); 
                       }
                   } catch (Exception e)  {
                       
                   }
               }
           }
           
       }
       processExtensionModelDeploymentMap.put(deploymentId, processExtensionModelMap);
       return processExtensionModelMap;
   }
   
   private List<ConnectorDefinition> getProcessConnectorDefinitionsForDeploymentId(String deploymentId) {
  
       List<ConnectorDefinition> processConnectorDefinitions = processConnectorDefinitionsMap.get(deploymentId);
       
       if (processConnectorDefinitions != null) {
           return processConnectorDefinitions;
       }
          
       processConnectorDefinitions = new ArrayList<>();
       
       List <String> resourceNames = repositoryService.getDeploymentResourceNames(deploymentId);
       
       if (resourceNames != null && !resourceNames.isEmpty()) {
           
           List <String> processExtensionNames = resourceNames.stream()
                                                   .filter(s -> s.contains("\\connectors\\"))
                                                   .collect(Collectors.toList());
           
           if (processExtensionNames != null && !processExtensionNames.isEmpty()) {
               for (String name:processExtensionNames) {
                   try {
                       processConnectorDefinitions.add(objectMapper.readValue(repositoryService.getResourceAsStream(deploymentId, name),ConnectorDefinition.class));
                   } catch (Exception e)  {
                       
                   }
                  
               }
           }
       }
       processConnectorDefinitionsMap.put(deploymentId, processConnectorDefinitions);
       return processConnectorDefinitions;
   }
   
   
    
    private Entry<String, String> deployProcessFromResource(Resource xmlResource,
                                             Map<String, Resource> processExtensions) throws Exception {
        
        //Check / get BpmnModel  
        BpmnModel bpmnModel = getBpmnModelFromProcessDefinitionResource(xmlResource);
        assertNotNull(bpmnModel);
        
        //Get main process
        Process process = bpmnModel.getMainProcess();
         
        //Find Extensions for our process
        Resource processExtensionResource = processExtensions.get(process.getId());
        
        //Deploy process       
        DeploymentBuilder deploymentBuilder = repositoryService
                                                .createDeployment()
                                                .key(process.getId())
                                                .name(xmlResource.getFilename());
        deploymentBuilder.addBpmnModel(xmlResource.getFilename(), bpmnModel);
        
        //Add process extensions (as resource)
        if (processExtensionResource != null) {
            deploymentBuilder.addInputStream(processExtensionResource.getFilename(), processExtensionResource);
            
        }
        
        Deployment deployment = deploymentBuilder.deploy();
        assertNotNull(deployment);
        
        //Check Resource is stored OK
        if (processExtensionResource != null) {
            String deploymentId = deployment.getId();
            
            InputStream deploymentResourceInputStream = repositoryService.getResourceAsStream(deploymentId,processExtensionResource.getFilename());
            assertNotNull(deploymentResourceInputStream);
            
            assertThat(deploymentResourceInputStream.equals(processExtensionResource.getInputStream()));
        }
        return new AbstractMap.SimpleEntry<String, String>(deployment.getId(),process.getId());
    }
    
    
       
    private Map<String, Resource> readProcessExtensions() throws IOException {
        Map<String, Resource> processExtensionResources = new HashMap<>();
        List<Resource> processExtensions = resourceFinder.discoverResources(processExtensionResourceFinder);
        
        if (!processExtensions.isEmpty()) {
            for (Resource resource : processExtensions) {
                ProcessExtensionModel processExtensionModel = read(resource.getInputStream());
                
                if (processExtensionModel != null) {
                    processExtensionResources.put(processExtensionModel.getId(),resource);
                }
            }
        }
        return processExtensionResources;
    }
    
    private ProcessExtensionModel read(InputStream inputStream) throws IOException {
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        return  objectMapper.readValue(inputStream,
                ProcessExtensionModel.class);
    }
    
    private ProcessExtensionModel readAndConvertVariables(InputStream inputStream) throws IOException {
        ProcessExtensionModel extensionModel =  read(inputStream);
        return convertJsonVariables(extensionModel);
    }
       
    private ProcessExtensionModel convertJsonVariables(ProcessExtensionModel processExtensionModel){
        if( processExtensionModel!=null && processExtensionModel.getExtensions()!=null
                && processExtensionModel.getExtensions().getProperties()!=null ){

            for(VariableDefinition variableDefinition:processExtensionModel.getExtensions().getProperties().values()){
                if(!variableTypeMap.keySet().contains(variableDefinition.getType())||variableDefinition.getType().equals("json")){
                    variableDefinition.setValue(objectMapper.convertValue(variableDefinition.getValue(), JsonNode.class));
                }
            }
        }
        return processExtensionModel;
    }
    
    private BpmnModel getBpmnModelFromProcessDefinitionResource(Resource resource) throws Exception {
        assertNotNull(resource);
        return getBpmnModelFromInputStream(resource.getInputStream());
    }
    
    private BpmnModel getBpmnModelFromInputStream(InputStream stream) throws Exception {
        assertNotNull(stream);
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(stream, "UTF-8");
        XMLStreamReader xtr = xif.createXMLStreamReader(in);
        return  new BpmnXMLConverter().convertToBpmnModel(xtr);
     }
    
    
    
    
}
