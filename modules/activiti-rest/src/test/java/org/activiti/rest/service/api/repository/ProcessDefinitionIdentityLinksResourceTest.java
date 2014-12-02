package org.activiti.rest.service.api.repository;

import java.util.List;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to single a Process Definition resource.
 * 
 * @author Frederik Heremans
 */
public class ProcessDefinitionIdentityLinksResourceTest extends BaseSpringRestTestCase {
  
  /**
  * Test getting identitylinks for a process definition.
  */
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testGetIdentityLinksForProcessDefinition() throws Exception {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.addCandidateStarterGroup(processDefinition.getId(), "admin");
    repositoryService.addCandidateStarterUser(processDefinition.getId(), "kermit");
    
    HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(
        RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINKS_COLLECTION, processDefinition.getId()));
    CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(2, responseNode.size());
    
    boolean groupCandidateFound = false;
    boolean userCandidateFound = false;
    
    for (int i=0; i < responseNode.size(); i++) {
      ObjectNode link = (ObjectNode) responseNode.get(i);
      assertNotNull(link);
      if (!link.get("user").isNull()) {
        assertEquals("kermit", link.get("user").textValue());
        assertEquals("candidate", link.get("type").textValue());
        assertTrue(link.get("group").isNull());
        assertTrue(link.get("url").asText().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, 
                processDefinition.getId(), "users", "kermit")));
        userCandidateFound = true;
          
      } else if (!link.get("group").isNull()) {
        assertEquals("admin", link.get("group").textValue());
        assertEquals("candidate", link.get("type").textValue());
        assertTrue(link.get("user").isNull());
        assertTrue(link.get("url").asText().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, 
                processDefinition.getId(), "groups", "admin")));
        groupCandidateFound = true;
      }
    }
    assertTrue(groupCandidateFound);
    assertTrue(userCandidateFound);
  }
  
  public void testGetIdentityLinksForUnexistingProcessDefinition() throws Exception {
    HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(
        RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINKS_COLLECTION, "unexisting"));
    CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_NOT_FOUND);
    closeResponse(response);
  }
  
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testAddCandidateStarterToProcessDefinition() throws Exception {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    
    // Create user candidate
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("user", "kermit");
    
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(
        RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINKS_COLLECTION, processDefinition.getId()));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("kermit", responseNode.get("user").textValue());
    assertEquals("candidate", responseNode.get("type").textValue());
    assertTrue(responseNode.get("group").isNull());
    assertTrue(responseNode.get("url").asText().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, 
            processDefinition.getId(), "users", "kermit")));
    
    List<IdentityLink> createdLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
    assertEquals(1, createdLinks.size());
    assertEquals("kermit", createdLinks.get(0).getUserId());
    assertEquals("candidate", createdLinks.get(0).getType());
    repositoryService.deleteCandidateStarterUser(processDefinition.getId(), "kermit");
    
    // Create group candidate
    requestNode = objectMapper.createObjectNode();
    requestNode.put("group", "admin");
    
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    response = executeRequest(httpPost, HttpStatus.SC_CREATED);
    responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("admin", responseNode.get("group").textValue());
    assertEquals("candidate", responseNode.get("type").textValue());
    assertTrue(responseNode.get("user").isNull());
    assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, 
            processDefinition.getId(), "groups", "admin")));
    
    createdLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
    assertEquals(1, createdLinks.size());
    assertEquals("admin", createdLinks.get(0).getGroupId());
    assertEquals("candidate", createdLinks.get(0).getType());
    repositoryService.deleteCandidateStarterUser(processDefinition.getId(), "admin");
  }
  
  public void testAddCandidateStarterToUnexistingProcessDefinition() throws Exception {
    // Create user candidate
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("user", "kermit");

    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(
        RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINKS_COLLECTION, "unexisting"));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_NOT_FOUND);
    closeResponse(response);
  }
  
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testGetCandidateStarterFromProcessDefinition() throws Exception {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.addCandidateStarterGroup(processDefinition.getId(), "admin");
    repositoryService.addCandidateStarterUser(processDefinition.getId(), "kermit");
    
    // Get user candidate
    HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(
        RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, processDefinition.getId(), "users", "kermit"));
    CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("kermit", responseNode.get("user").textValue());
    assertEquals("candidate", responseNode.get("type").textValue());
    assertTrue(responseNode.get("group").isNull());
    assertTrue(responseNode.get("url").asText().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, 
            processDefinition.getId(), "users", "kermit")));
    
    // Get group candidate
    httpGet = new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(
        RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, processDefinition.getId(), "groups", "admin"));
    response = executeRequest(httpGet, HttpStatus.SC_OK);
    responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("admin", responseNode.get("group").textValue());
    assertEquals("candidate", responseNode.get("type").textValue());
    assertTrue(responseNode.get("user").isNull());
    assertTrue(responseNode.get("url").asText().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, 
            processDefinition.getId(), "groups", "admin")));
  }
  
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testDeleteCandidateStarterFromProcessDefinition() throws Exception {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.addCandidateStarterGroup(processDefinition.getId(), "admin");
    repositoryService.addCandidateStarterUser(processDefinition.getId(), "kermit");
    
    // Delete user candidate
    HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(
        RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, processDefinition.getId(), "users", "kermit"));
    CloseableHttpResponse response = executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT);
    closeResponse(response);
    
    // Check if group-link remains
    List<IdentityLink> remainingLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
    assertEquals(1, remainingLinks.size());
    assertEquals("admin", remainingLinks.get(0).getGroupId());

    
    // Delete group candidate
    httpDelete = new HttpDelete(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(
        RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, processDefinition.getId(), "groups", "admin"));
    response = executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT);
    closeResponse(response);
    
    // Check if all links are removed
    remainingLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
    assertEquals(0, remainingLinks.size());
  }
  
  public void testDeleteCandidateStarterFromUnexistingProcessDefinition() throws Exception {
    HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(
        RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, "unexisting", "groups", "admin"));
    CloseableHttpResponse response = executeRequest(httpDelete, HttpStatus.SC_NOT_FOUND);
    closeResponse(response);
  }
  
  public void testGetCandidateStarterFromUnexistingProcessDefinition() throws Exception {
    HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(
        RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK, "unexisting", "groups", "admin"));
    CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_NOT_FOUND);
    closeResponse(response);
  }
}
