package org.activiti.rest.api.jpa;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.api.jpa.model.Message;
import org.activiti.rest.api.jpa.repository.MessageRepository;
import org.activiti.rest.service.ProcessEnginesRest;
import org.activiti.rest.service.api.RestUrls;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:activiti-jpa-context.xml")
public class JpaRestTest extends SpringActivitiTestCase {
  
  @Autowired
  private MessageRepository messageRepository;
  
  protected ObjectMapper objectMapper = new ObjectMapper();
  protected Component component;
  
  
  @Override
  public void runBare() throws Throwable {
    initializeRestServer();
    super.runBare();
    stopRestServer();
  }
  
  @Override
  protected void initializeProcessEngine() {
    ContextConfiguration contextConfiguration = getClass().getAnnotation(ContextConfiguration.class);
    String[] value = contextConfiguration.value();
    if (value==null) {
      throw new ActivitiException("value is mandatory in ContextConfiguration");
    }
    if (value.length!=1) {
      throw new ActivitiException("SpringActivitiTestCase requires exactly one value in annotation ContextConfiguration");
    }
    String configurationFile = value[0];
    processEngine = cachedProcessEngines.get(configurationFile);
    if (processEngine==null) {
      processEngine = applicationContext.getBean(ProcessEngine.class);
      cachedProcessEngines.put(configurationFile, processEngine);
    }
    ProcessEnginesRest.init();
    ProcessEngines.registerProcessEngine(processEngine);
  }
  
  @Test
  @Deployment(resources = {"org/activiti/rest/api/jpa/jpa-process.bpmn20.xml"})
  public void testGetJpaVariableViaTaskVariablesCollections() throws Exception {

    // Get JPA managed entity through the repository
    Message message = messageRepository.findOne(1L);
    assertNotNull(message);
    assertEquals("Hello World", message.getText());
    
    // add the entity to the process variables and start the process
    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("message", message);
      
    ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("jpa-process", processVariables);
    assertNotNull(processInstance);
    // create the user required by the REST API authentication
    createUsers(processEngine);
    
    Task task = processEngine.getTaskService().createTaskQuery().singleResult();
    assertEquals("Activiti is awesome!", task.getName());
    
    // Request all variables (no scope provides) which include global and local
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_VARIABLES_COLLECTION, task.getId()));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream()).get(0);
    
    // check for message variable of type serializable
    assertNotNull(responseNode);
    assertEquals("message", responseNode.get("name").asText());
    assertEquals("global", responseNode.get("scope").asText());
    assertEquals("serializable", responseNode.get("type").asText());
    assertNotNull(responseNode.get("valueUrl"));
    
    // drop the users to clear the DB
    dropUsers(processEngine);
  }
  
  @Test
  @Deployment(resources = {"org/activiti/rest/api/jpa/jpa-process.bpmn20.xml"})
  public void testGetJpaVariableViaTaskCollection() throws Exception {

    // Get JPA managed entity through the repository
    Message message = messageRepository.findOne(1L);
    assertNotNull(message);
    assertEquals("Hello World", message.getText());
    
    // add the entity to the process variables and start the process
    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("message", message);
      
    ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("jpa-process", processVariables);
    assertNotNull(processInstance);
    // create the user required by the REST API authentication
    createUsers(processEngine);
    
    Task task = processEngine.getTaskService().createTaskQuery().singleResult();
    assertEquals("Activiti is awesome!", task.getName());
    
    // Request all variables (no scope provides) which include global and local
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_COLLECTION));
    // add a query parameter to ensure process variables are requested with the Task Collection
    client.getReference().addQueryParameter("includeProcessVariables", "true");
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode dataNode = objectMapper.readTree(response.getStream()).get("data").get(0);
    assertNotNull(dataNode);
    
    JsonNode variableNode = dataNode.get("variables").get(0);
    assertNotNull(variableNode);
    
    // check for message variable of type serializable
    assertEquals("message", variableNode.get("name").asText());
    assertEquals("global", variableNode.get("scope").asText());

    assertEquals("serializable", variableNode.get("type").asText());
    assertNotNull(variableNode.get("valueUrl"));
    
    // drop the users to clear the DB
    dropUsers(processEngine);
  }
  
  @Test
  @Deployment(resources = {"org/activiti/rest/api/jpa/jpa-process.bpmn20.xml"})
  public void testGetJpaVariableViaHistoricProcessCollection() throws Exception {

    // Get JPA managed entity through the repository
    Message message = messageRepository.findOne(1L);
    assertNotNull(message);
    assertEquals("Hello World", message.getText());
    
    // add the entity to the process variables and start the process
    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("message", message);
      
    ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("jpa-process", processVariables);
    assertNotNull(processInstance);
    // create the user required by the REST API authentication
    createUsers(processEngine);
    
    Task task = processEngine.getTaskService().createTaskQuery().singleResult();
    assertEquals("Activiti is awesome!", task.getName());
    
    // Request all variables (no scope provides) which include global and local
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCES));
    client.getReference().addQueryParameter("processInstanceId", processInstance.getId());
    client.getReference().addQueryParameter("includeProcessVariables", "true");
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    
    // check for message variable of type serializable
    assertNotNull(responseNode);
    JsonNode variablesArrayNode = responseNode.get("data").get(0).get("variables");
    assertEquals(1, variablesArrayNode.size());
    JsonNode variableNode = variablesArrayNode.get(0);
    assertEquals("message", variableNode.get("name").asText());
    assertEquals("serializable", variableNode.get("type").asText());
    assertNotNull(variableNode.get("valueUrl"));
    
    // drop the users to clear the DB
    dropUsers(processEngine);
  }
  
  @Test
  @Deployment(resources = {"org/activiti/rest/api/jpa/jpa-process.bpmn20.xml"})
  public void testGetJpaVariableViaHistoricVariablesCollections() throws Exception {

    // Get JPA managed entity through the repository
    Message message = messageRepository.findOne(1L);
    assertNotNull(message);
    assertEquals("Hello World", message.getText());
    
    // add the entity to the process variables and start the process
    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("message", message);
      
    ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("jpa-process", processVariables);
    assertNotNull(processInstance);
    // create the user required by the REST API authentication
    createUsers(processEngine);
    
    Task task = processEngine.getTaskService().createTaskQuery().singleResult();
    assertEquals("Activiti is awesome!", task.getName());
    
    // Request all variables (no scope provides) which include global and local
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_HISTORIC_VARIABLE_INSTANCES));
    client.getReference().addQueryParameter("processInstanceId", processInstance.getId());
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    
    // check for message variable of type serializable
    assertNotNull(responseNode);
    JsonNode variableNode = responseNode.get("data").get(0).get("variable");
    assertEquals("message", variableNode.get("name").asText());
    assertEquals("serializable", variableNode.get("type").asText());
    assertNotNull(variableNode.get("valueUrl"));
    
    // drop the users to clear the DB
    dropUsers(processEngine);
  }
  
  protected ClientResource getAuthenticatedClient(String uri) {
    ClientResource client = new ClientResource("http://localhost:8182/" + uri);
    client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "kermit", "kermit");
    return client;
  }
  
  protected void initializeRestServer() throws Exception {
    component = new Component();
    // Add a new HTTP server listening on port 8182.
    component.getServers().add(Protocol.HTTP, 8182);
    component.getDefaultHost().attach(new ActivitiRestServicesApplication());
    component.start();
  }
  
  protected void stopRestServer() throws Exception {
    component.stop();
  }

  protected void createUsers(ProcessEngine processEngine) {
    IdentityService identityService = processEngine.getIdentityService();
    User user = identityService.newUser("kermit");
    user.setFirstName("Kermit");
    user.setLastName("the Frog");
    user.setPassword("kermit");
    identityService.saveUser(user);

    Group group = identityService.newGroup("admin");
    group.setName("Administrators");
    identityService.saveGroup(group);

    identityService.createMembership(user.getId(), group.getId());
  }
  
  protected void dropUsers(ProcessEngine processEngine) {
    IdentityService identityService = processEngine.getIdentityService();

    identityService.deleteUser("kermit");
    identityService.deleteGroup("admin");
    identityService.deleteMembership("kermit", "admin");
  }

}