package org.activiti.rest.api.repository;

import java.net.URLDecoder;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.activiti.rest.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

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
  @Deployment(resources={"org/activiti/rest/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testGetProcessDefinition() throws Exception {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
    Representation response = client.get();
    
    // Check "OK" status
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertEquals(processDefinition.getId(), responseNode.get("id").getTextValue());
    assertEquals(processDefinition.getKey(), responseNode.get("key").getTextValue());
    assertEquals(processDefinition.getCategory(), responseNode.get("category").getTextValue());
    assertEquals(processDefinition.getVersion(), responseNode.get("version").getIntValue());
    assertEquals(processDefinition.getDescription(), responseNode.get("description").getTextValue());
    assertEquals(processDefinition.getName(), responseNode.get("name").getTextValue());
    assertFalse(responseNode.get("graphicalNotationDefined").getBooleanValue());
    
    // Check URL's
    assertEquals(client.getRequest().getResourceRef().toString(), URLDecoder.decode(responseNode.get("url").getTextValue(),"UTF-8"));
    assertTrue(responseNode.get("deployment").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, processDefinition.getDeploymentId())));
    assertTrue(URLDecoder.decode(responseNode.get("resource").getTextValue(), "UTF-8").endsWith(RestUrls.createRelativeResourceUrl(
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
     assertEquals(processDefinition.getId(), responseNode.get("id").getTextValue());
     assertEquals(processDefinition.getKey(), responseNode.get("key").getTextValue());
     assertEquals(processDefinition.getCategory(), responseNode.get("category").getTextValue());
     assertEquals(processDefinition.getVersion(), responseNode.get("version").getIntValue());
     assertEquals(processDefinition.getDescription(), responseNode.get("description").getTextValue());
     assertEquals(processDefinition.getName(), responseNode.get("name").getTextValue());
     assertTrue(responseNode.get("graphicalNotationDefined").getBooleanValue());
     
     // Check URL's
     assertEquals(client.getRequest().getResourceRef().toString(), URLDecoder.decode(responseNode.get("url").getTextValue(),"UTF-8"));
     assertTrue(responseNode.get("deployment").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, processDefinition.getDeploymentId())));
     assertTrue(URLDecoder.decode(responseNode.get("resource").getTextValue(), "UTF-8").endsWith(RestUrls.createRelativeResourceUrl(
             RestUrls.URL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getResourceName())));
     assertTrue(URLDecoder.decode(responseNode.get("diagramResource").getTextValue(), "UTF-8").endsWith(RestUrls.createRelativeResourceUrl(
             RestUrls.URL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName())));
   }
  
  /**
   * Test getting an unexisting process-definition.
   * GET repository/process-definitions/{processDefinitionId}
   */
   public void testGetUnexistingDeployment() throws Exception {
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
    @Deployment(resources={"org/activiti/rest/api/repository/oneTaskProcess.bpmn20.xml"})
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
      assertTrue(responseNode.get("suspended").getBooleanValue());
      
      // Check if process-definitoin is suspended
      processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
      assertTrue(processDefinition.isSuspended());
    }
    
    /**
     * Test suspending already suspended process definition.
     * POST repository/process-definitions/{processDefinitionId}
     */
     @Deployment(resources={"org/activiti/rest/api/repository/oneTaskProcess.bpmn20.xml"})
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
     @Deployment(resources={"org/activiti/rest/api/repository/oneTaskProcess.bpmn20.xml"})
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
       assertFalse(responseNode.get("suspended").getBooleanValue());
       
       // Check if process-definitoin is suspended
       processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
       assertFalse(processDefinition.isSuspended());
     }
     
     /**
      * Test activating already active process definition.
      * POST repository/process-definitions/{processDefinitionId}
      */
      @Deployment(resources={"org/activiti/rest/api/repository/oneTaskProcess.bpmn20.xml"})
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
       @Deployment(resources={"org/activiti/rest/api/repository/oneTaskProcess.bpmn20.xml"})
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
}
