package org.activiti.rest.service.api.repository;

import java.util.List;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
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
public class ProcessDefinitionIdentityLinksResourceTest extends BaseRestTestCase {
  
  /**
  * Test getting identitylinks for a process definition.
  */
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testGetIdentityLinksForProcessDefinition() throws Exception {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.addCandidateStarterGroup(processDefinition.getId(), "admin");
    repositoryService.addCandidateStarterUser(processDefinition.getId(), "kermit");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
            RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINKS_COLLECTION, processDefinition.getId()));
    Representation response = client.get();
    
    // Check "OK" status
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(2, responseNode.size());
    
    boolean groupCandidateFound = false;
    boolean userCandidateFound = false;
    
    for(int i=0; i < responseNode.size(); i++) {
      ObjectNode link = (ObjectNode) responseNode.get(i);
      assertNotNull(link);
      if(!link.get("user").isNull()) {
          assertEquals("kermit", link.get("user").getTextValue());
          assertEquals("candidate", link.get("type").getTextValue());
          assertTrue(link.get("group").isNull());
          assertTrue(link.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, 
                  encode(processDefinition.getId()), "users", "kermit")));
          userCandidateFound = true;
      } else if(!link.get("group").isNull()) {
        assertEquals("admin", link.get("group").getTextValue());
        assertEquals("candidate", link.get("type").getTextValue());
        assertTrue(link.get("user").isNull());
        assertTrue(link.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, 
                encode(processDefinition.getId()), "groups", "admin")));
        groupCandidateFound = true;
      }
    }
    assertTrue(groupCandidateFound);
    assertTrue(userCandidateFound);
  }
  
  public void testGetIdentityLinksForUnexistingProcessDefinition() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINKS_COLLECTION, "unexisting"));
    try {
      client.get();
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a process definition with id 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
  
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testAddCandidateStarterToProcessDefinition() throws Exception {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINKS_COLLECTION, processDefinition.getId()));

    // Create user candidate
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("user", "kermit");
    
    Representation response = client.post(requestNode);
    
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());

    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("kermit", responseNode.get("user").getTextValue());
    assertEquals("candidate", responseNode.get("type").getTextValue());
    assertTrue(responseNode.get("group").isNull());
    assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, 
            encode(processDefinition.getId()), "users", "kermit")));
    
    List<IdentityLink> createdLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
    assertEquals(1, createdLinks.size());
    assertEquals("kermit", createdLinks.get(0).getUserId());
    assertEquals("candidate", createdLinks.get(0).getType());
    repositoryService.deleteCandidateStarterUser(processDefinition.getId(), "kermit");
    
    // Create group candidate
    requestNode = objectMapper.createObjectNode();
    requestNode.put("group", "admin");
    
    response = client.post(requestNode);
    
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());

    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("admin", responseNode.get("group").getTextValue());
    assertEquals("candidate", responseNode.get("type").getTextValue());
    assertTrue(responseNode.get("user").isNull());
    assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, 
            encode(processDefinition.getId()), "groups", "admin")));
    
    createdLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
    assertEquals(1, createdLinks.size());
    assertEquals("admin", createdLinks.get(0).getGroupId());
    assertEquals("candidate", createdLinks.get(0).getType());
    repositoryService.deleteCandidateStarterUser(processDefinition.getId(), "admin");
  }
  
  public void testAddCandidateStarterToUnexistingProcessDefinition() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINKS_COLLECTION, "unexisting"));

    // Create user candidate
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("user", "kermit");

    try {
      client.post(requestNode);
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a process definition with id 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
  
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testGetCandidateStarterFromProcessDefinition() throws Exception {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.addCandidateStarterGroup(processDefinition.getId(), "admin");
    repositoryService.addCandidateStarterUser(processDefinition.getId(), "kermit");
    
    // Get user candidate
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, processDefinition.getId(), "users", "kermit"));
    Representation response = client.get();
    
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("kermit", responseNode.get("user").getTextValue());
    assertEquals("candidate", responseNode.get("type").getTextValue());
    assertTrue(responseNode.get("group").isNull());
    assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, 
            encode(processDefinition.getId()), "users", "kermit")));
    
    // Get group candidate
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, processDefinition.getId(), "groups", "admin"));
    response = client.get();
    
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("admin", responseNode.get("group").getTextValue());
    assertEquals("candidate", responseNode.get("type").getTextValue());
    assertTrue(responseNode.get("user").isNull());
    assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, 
            encode(processDefinition.getId()), "groups", "admin")));
  }
  
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testDeleteCandidateStarterFromProcessDefinition() throws Exception {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.addCandidateStarterGroup(processDefinition.getId(), "admin");
    repositoryService.addCandidateStarterUser(processDefinition.getId(), "kermit");
    
    // Get user candidate
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, processDefinition.getId(), "users", "kermit"));
    Representation response = client.delete();
    
    assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
    assertEquals(0, response.getSize());

    // Check if group-link remains
    List<IdentityLink> remainingLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
    assertEquals(1, remainingLinks.size());
    assertEquals("admin", remainingLinks.get(0).getGroupId());

    
    // Delete group candidate
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, processDefinition.getId(), "groups", "admin"));
    response = client.delete();
    
    assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
    assertEquals(0, response.getSize());
    
    // Check if all links are removed
    remainingLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
    assertEquals(0, remainingLinks.size());
  }
  
  public void testDeleteCandidateStarterFromUnexistingProcessDefinition() throws Exception {
    ClientResource client = getAuthenticatedClient(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, "unexisting", "groups", "admin"));
    try {
      client.delete();
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a process definition with id 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
  
  public void testGetCandidateStarterFromUnexistingProcessDefinition() throws Exception {
    ClientResource client = getAuthenticatedClient(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, "unexisting", "groups", "admin"));
    try {
      client.get();
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a process definition with id 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
}
