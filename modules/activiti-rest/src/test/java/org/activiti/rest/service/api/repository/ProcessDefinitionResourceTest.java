package org.activiti.rest.service.api.repository;

import java.net.URLDecoder;
import java.util.Calendar;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to single a Process Definition resource.
 * 
 * @author Frederik Heremans
 */
public class ProcessDefinitionResourceTest extends BaseRestTestCase {
  
  /**
  * Test getting a single process definition.
  * GET repository/process-definitions/{processDefinitionResource}
  */
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testGetProcessDefinition() throws Exception {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
    Representation response = client.get();
    
    // Check "OK" status
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertEquals(processDefinition.getId(), responseNode.get("id").textValue());
    assertEquals(processDefinition.getKey(), responseNode.get("key").textValue());
    assertEquals(processDefinition.getCategory(), responseNode.get("category").textValue());
    assertEquals(processDefinition.getVersion(), responseNode.get("version").intValue());
    assertEquals(processDefinition.getDescription(), responseNode.get("description").textValue());
    assertEquals(processDefinition.getName(), responseNode.get("name").textValue());
    assertFalse(responseNode.get("graphicalNotationDefined").booleanValue());
    
    // Check URL's
    assertEquals(client.getRequest().getResourceRef().toString(), URLDecoder.decode(responseNode.get("url").textValue(),"UTF-8"));
    assertEquals(processDefinition.getDeploymentId(), responseNode.get("deploymentId").textValue());
    assertTrue(responseNode.get("deploymentUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, processDefinition.getDeploymentId())));
    assertTrue(URLDecoder.decode(responseNode.get("resource").textValue(), "UTF-8").endsWith(RestUrls.createRelativeResourceUrl(
            RestUrls.URL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getResourceName())));
    assertTrue(responseNode.get("diagramResource").isNull());
  }
  
  /**
   * Test getting a single process definition with a graphical notation defined.
   * GET repository/process-definitions/{processDefinitionResource}
   */
   @Deployment
   public void testGetProcessDefinitionWithGraphicalNotation() throws Exception {
  
     ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
     
     ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
     Representation response = client.get();
     
     // Check "OK" status
     assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
     
     JsonNode responseNode = objectMapper.readTree(response.getStream());
     assertEquals(processDefinition.getId(), responseNode.get("id").textValue());
     assertEquals(processDefinition.getKey(), responseNode.get("key").textValue());
     assertEquals(processDefinition.getCategory(), responseNode.get("category").textValue());
     assertEquals(processDefinition.getVersion(), responseNode.get("version").intValue());
     assertEquals(processDefinition.getDescription(), responseNode.get("description").textValue());
     assertEquals(processDefinition.getName(), responseNode.get("name").textValue());
     assertTrue(responseNode.get("graphicalNotationDefined").booleanValue());
     
     // Check URL's
     assertEquals(client.getRequest().getResourceRef().toString(), URLDecoder.decode(responseNode.get("url").textValue(),"UTF-8"));
     assertEquals(processDefinition.getDeploymentId(), responseNode.get("deploymentId").textValue());
     assertTrue(responseNode.get("deploymentUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, processDefinition.getDeploymentId())));
     assertTrue(URLDecoder.decode(responseNode.get("resource").textValue(), "UTF-8").endsWith(RestUrls.createRelativeResourceUrl(
             RestUrls.URL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getResourceName())));
     assertTrue(URLDecoder.decode(responseNode.get("diagramResource").textValue(), "UTF-8").endsWith(RestUrls.createRelativeResourceUrl(
             RestUrls.URL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName())));
   }
  
  /**
   * Test getting an unexisting process-definition.
   * GET repository/process-definitions/{processDefinitionId}
   */
   public void testGetUnexistingProcessDefinition() throws Exception {
     ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, "unexisting"));
     try {
       client.get();
       fail("404 expected, but was: " + client.getResponse().getStatus());
     } catch(ResourceException expected) {
       assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
       assertEquals("Could not find a process definition with id 'unexisting'.", client.getResponse().getStatus().getDescription());
     }
   }
  
   /**
    * Test suspending a process definition.
    * POST repository/process-definitions/{processDefinitionId}
    */
    @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
    public void testSuspendProcessDefinition() throws Exception {
      ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
      assertFalse(processDefinition.isSuspended());
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("action", "suspend");
      
      Representation response = client.put(requestNode);
      
      // Check "OK" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertTrue(responseNode.get("suspended").booleanValue());
      
      // Check if process-definitoin is suspended
      processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
      assertTrue(processDefinition.isSuspended());
    }
    
  /**
   * Test suspending a process definition on a certain date.
   * POST repository/process-definitions/{processDefinitionId}
   */
   @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
   public void testSuspendProcessDefinitionDelayed() throws Exception {
     ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
     assertFalse(processDefinition.isSuspended());
     
     ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
     ObjectNode requestNode = objectMapper.createObjectNode();
     
     Calendar cal = Calendar.getInstance();
     cal.add(Calendar.HOUR, 2);
     
     // Format the date using ISO date format
     DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
     String dateString = formatter.print(cal.getTimeInMillis());
     
     requestNode.put("action", "suspend");
     requestNode.put("date", dateString);
     
     Representation response = client.put(requestNode);
     
     // Check "OK" status
     assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
     JsonNode responseNode = objectMapper.readTree(response.getStream());
     assertTrue(responseNode.get("suspended").booleanValue());
     
     // Check if process-definition is not yet suspended
     processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
     assertFalse(processDefinition.isSuspended());
     
     // Force suspension by altering time
     cal.add(Calendar.HOUR, 1);
     processEngineConfiguration.getClock().setCurrentTime(cal.getTime());
     waitForJobExecutorToProcessAllJobs(5000, 100);
     
     // Check if process-definition is suspended
     processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
     assertTrue(processDefinition.isSuspended());
   }
  
  /**
   * Test suspending already suspended process definition.
   * POST repository/process-definitions/{processDefinitionId}
   */
   @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
   public void testSuspendAlreadySuspendedProcessDefinition() throws Exception {
     ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
     repositoryService.suspendProcessDefinitionById(processDefinition.getId());
     
     processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
     assertTrue(processDefinition.isSuspended());
     
     ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
     ObjectNode requestNode = objectMapper.createObjectNode();
     requestNode.put("action", "suspend");
     
     try {
       client.put(requestNode);
       fail("Expected exception");
     } catch(ResourceException expected) {
       assertEquals(Status.CLIENT_ERROR_CONFLICT, expected.getStatus());
     }
   }
   
  
  /**
   * Test activating a suspended process definition.
   * POST repository/process-definitions/{processDefinitionId}
   */
   @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
   public void testActivateProcessDefinition() throws Exception {
     ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
     repositoryService.suspendProcessDefinitionById(processDefinition.getId());
     
     processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
     assertTrue(processDefinition.isSuspended());
     
     ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
     ObjectNode requestNode = objectMapper.createObjectNode();
     requestNode.put("action", "activate");
     
     Representation response = client.put(requestNode);
     
     // Check "OK" status
     assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
     JsonNode responseNode = objectMapper.readTree(response.getStream());
     assertFalse(responseNode.get("suspended").booleanValue());
     
     // Check if process-definitoin is suspended
     processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
     assertFalse(processDefinition.isSuspended());
   }
     
 /**
  * Test activating a suspended process definition delayed.
  * POST repository/process-definitions/{processDefinitionId}
  */
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testActivateProcessDefinitionDelayed() throws Exception {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertTrue(processDefinition.isSuspended());
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
    ObjectNode requestNode = objectMapper.createObjectNode();
    
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR, 2);
    
    // Format the date using ISO date format
    DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
    String dateString = formatter.print(cal.getTimeInMillis());
    
    requestNode.put("action", "activate");
    requestNode.put("date", dateString);
    
    Representation response = client.put(requestNode);
    
    // Check "OK" status
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertFalse(responseNode.get("suspended").booleanValue());
    
    // Check if process-definition is not yet active
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertTrue(processDefinition.isSuspended());
    
    // Force activation by altering time
    cal.add(Calendar.HOUR, 1);
    processEngineConfiguration.getClock().setCurrentTime(cal.getTime());
    waitForJobExecutorToProcessAllJobs(5000, 100);
    
    // Check if process-definition is activated
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());
  }
     
 /**
  * Test activating already active process definition.
  * POST repository/process-definitions/{processDefinitionId}
  */
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testActivateAlreadyActiveProcessDefinition() throws Exception {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "activate");
    
    try {
      client.put(requestNode);
      fail("Expected exception");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_CONFLICT, expected.getStatus());
    }
  }
  
  /**
   * Test executing an unexisting action.
   * 
   * POST repository/process-definitions/{processDefinitionId}
   */
   @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
   public void testIllegalAction() throws Exception {
     ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
     assertFalse(processDefinition.isSuspended());
     
     ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
     ObjectNode requestNode = objectMapper.createObjectNode();
     requestNode.put("action", "unexistingaction");
     
     try {
       client.put(requestNode);
       fail("Expected exception");
     } catch(ResourceException expected) {
       assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
     }
   }
   
   @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
   public void testGetProcessDefinitionResourceData() throws Exception {
     ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
     
     ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_RESOURCE_CONTENT, processDefinition.getId()));
     
     Representation response = client.get();
     
     // Check "OK" status
     assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
     String content = response.getText();
     assertNotNull(content);
     assertTrue(content.contains("The One Task Process"));
   }
   
   @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
   public void testGetProcessDefinitionModel() throws Exception {
     ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
     
     ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_MODEL, processDefinition.getId()));
     
     Representation response = client.get();
     
     // Check "OK" status
     assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
     JsonNode resultNode = objectMapper.readTree(response.getReader());
     assertNotNull(resultNode);
     JsonNode processes = resultNode.get("processes");
     assertNotNull(processes);
     assertTrue(processes.isArray());
     assertEquals(1, processes.size());
     assertEquals("oneTaskProcess", processes.get(0).get("id").textValue());
   }
   
   /**
    * Test getting model for an unexisting process-definition .
    */
    public void testGetModelForUnexistingProcessDefinition() throws Exception {
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_MODEL, "unexisting"));
      try {
        client.get();
        fail("404 expected, but was: " + client.getResponse().getStatus());
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
        assertEquals("Could not find a process definition with id 'unexisting'.", client.getResponse().getStatus().getDescription());
      }
    }
    
    /**
     * Test getting resource content for an unexisting process-definition .
     */
     public void testGetResourceContentForUnexistingProcessDefinition() throws Exception {
       ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_RESOURCE_CONTENT, "unexisting"));
       try {
         client.get();
         fail("404 expected, but was: " + client.getResponse().getStatus());
       } catch(ResourceException expected) {
         assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
         assertEquals("Could not find a process definition with id 'unexisting'.", client.getResponse().getStatus().getDescription());
       }
     }
     
     /**
      * Test activating a suspended process definition delayed.
      * POST repository/process-definitions/{processDefinitionId}
      */
      @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
      public void testUpdateProcessDefinitionCategory() throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionCategory("OneTaskCategory").count());
        
        ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("category", "updatedcategory");
        
        Representation response = client.put(requestNode);
        
        // Check "OK" status
        assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
        
        JsonNode responseNode = objectMapper.readTree(response.getStream());
        assertEquals("updatedcategory", responseNode.get("category").textValue());
        
        // Check actual entry in DB
        assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionCategory("updatedcategory").count());
        
      }
   
}
