package org.activiti.rest.api.repository;

import java.util.Calendar;
import java.util.List;

import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.activiti.rest.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Test for all REST-operations related to the Deployment collection.
 * 
 * @author Frederik Heremans
 */
public class DeploymentCollectionResourceTest extends BaseRestTestCase {
  
  /**
  * Test getting deployments.
  * GET repository/deployments
  */
  public void testGetDeployments() throws Exception {
    
    try {
      // Alter time to ensure different deployTimes
      Calendar yesterday = Calendar.getInstance();
      yesterday.add(Calendar.DAY_OF_MONTH, -1);
      ClockUtil.setCurrentTime(yesterday.getTime());
      
      Deployment firstDeployment = repositoryService.createDeployment().name("Deployment 1")
          .category("DEF")
          .addClasspathResource("org/activiti/rest/api/repository/oneTaskProcess.bpmn20.xml")
          .deploy();
      
      ClockUtil.setCurrentTime(Calendar.getInstance().getTime());
      Deployment secondDeployment = repositoryService.createDeployment().name("Deployment 2")
              .category("ABC")
              .addClasspathResource("org/activiti/rest/api/repository/oneTaskProcess.bpmn20.xml")
              .deploy();
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION));
      Representation response = client.get();
      
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      JsonNode dataNode = objectMapper.readTree(response.getStream()).get("data");
      assertEquals(2L, dataNode.size());
      
      // Check name filtering
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION) + "?deploymentName=Deployment 1");
      response = client.get();
      
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      dataNode = objectMapper.readTree(response.getStream()).get("data");
      assertEquals(1L, dataNode.size());
      assertEquals(firstDeployment.getId(), dataNode.get(0).get("id").getTextValue());
      
      // Check name-like filtering
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION) + "?deploymentNameLike=%25ment 2");
      response = client.get();
      
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      dataNode = objectMapper.readTree(response.getStream()).get("data");
      assertEquals(1L, dataNode.size());
      assertEquals(secondDeployment.getId(), dataNode.get(0).get("id").getTextValue());
      
      // Check category filtering
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION) + "?deploymentCategory=DEF");
      response = client.get();
      
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      dataNode = objectMapper.readTree(response.getStream()).get("data");
      assertEquals(1L, dataNode.size());
      assertEquals(firstDeployment.getId(), dataNode.get(0).get("id").getTextValue());
      
      // Check category-not-equals filtering
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION) + "?deploymentCategoryNotEquals=DEF");
      response = client.get();
      
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      dataNode = objectMapper.readTree(response.getStream()).get("data");
      assertEquals(1L, dataNode.size());
      assertEquals(secondDeployment.getId(), dataNode.get(0).get("id").getTextValue());
      
      // Check ordering by name
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION)
              + "?sort=name&order=asc");
      response = client.get();
      
      dataNode = objectMapper.readTree(response.getStream()).get("data");
      assertEquals(2L, dataNode.size());
      assertEquals(firstDeployment.getId(), dataNode.get(0).get("id").getTextValue());
      assertEquals(secondDeployment.getId(), dataNode.get(1).get("id").getTextValue());
      
      // Check ordering by deploy time
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION)
              + "?sort=deployTime&order=asc");
      response = client.get();
      
      dataNode = objectMapper.readTree(response.getStream()).get("data");
      assertEquals(2L, dataNode.size());
      assertEquals(firstDeployment.getId(), dataNode.get(0).get("id").getTextValue());
      assertEquals(secondDeployment.getId(), dataNode.get(1).get("id").getTextValue());
      
      // Check paging
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION)
              + "?sort=deployTime&order=asc&start=1&size=1");
      response = client.get();
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      dataNode = responseNode.get("data");
      assertEquals(1L, dataNode.size());
      assertEquals(secondDeployment.getId(), dataNode.get(0).get("id").getTextValue());
      assertEquals(2L, responseNode.get("total").getLongValue());
      assertEquals(1L, responseNode.get("start").getLongValue());
      assertEquals(1L, responseNode.get("size").getLongValue());
      
    } finally {
      // Always cleanup any created deployments, even if the test failed
      List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
      for(Deployment deployment : deployments) {
        repositoryService.deleteDeployment(deployment.getId(), true);
      }
    }
  }
}
