package org.activiti.rest.service.api.repository;

import java.util.Calendar;
import java.util.List;

import org.activiti.engine.repository.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.fasterxml.jackson.databind.JsonNode;

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
      processEngineConfiguration.getClock().setCurrentTime(yesterday.getTime());
      
      Deployment firstDeployment = repositoryService.createDeployment().name("Deployment 1")
          .category("DEF")
          .addClasspathResource("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml")
          .deploy();

      processEngineConfiguration.getClock().setCurrentTime(Calendar.getInstance().getTime());
      Deployment secondDeployment = repositoryService.createDeployment().name("Deployment 2")
              .category("ABC")
              .addClasspathResource("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml")
              .tenantId("myTenant")
              .deploy();
      
      String baseUrl = RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION);
      assertResultsPresentInDataResponse(baseUrl, firstDeployment.getId(), secondDeployment.getId());
      
      // Check name filtering
      String url = baseUrl + "?name=Deployment 1";
      assertResultsPresentInDataResponse(url, firstDeployment.getId());
      
      // Check name-like filtering
      url = baseUrl + "?nameLike=" + encode("%ment 2");
      assertResultsPresentInDataResponse(url, secondDeployment.getId());
      
      // Check category filtering
      url = baseUrl +"?category=DEF";
      assertResultsPresentInDataResponse(url, firstDeployment.getId());
      
      // Check category-not-equals filtering
      url = baseUrl +"?categoryNotEquals=DEF";
      assertResultsPresentInDataResponse(url, secondDeployment.getId());
      
      // Check tenantId filtering
      url = baseUrl +"?tenantId=myTenant";
      assertResultsPresentInDataResponse(url, secondDeployment.getId());
      
      // Check tenantId filtering
      url = baseUrl +"?tenantId=unexistingTenant";
      assertResultsPresentInDataResponse(url);
      
      // Check tenantId like filtering
      url = baseUrl +"?tenantIdLike="+ encode("%enant");
      assertResultsPresentInDataResponse(url, secondDeployment.getId());
      
      // Check without tenantId filtering
      url = baseUrl +"?withoutTenantId=true";
      assertResultsPresentInDataResponse(url, firstDeployment.getId());
      
      // Check ordering by name
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION)
              + "?sort=name&order=asc");
      Representation response = client.get();
      
      JsonNode dataNode = objectMapper.readTree(response.getStream()).get("data");
      assertEquals(2L, dataNode.size());
      assertEquals(firstDeployment.getId(), dataNode.get(0).get("id").textValue());
      assertEquals(secondDeployment.getId(), dataNode.get(1).get("id").textValue());
      client.release();
      
      // Check ordering by deploy time
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION)
              + "?sort=deployTime&order=asc");
      response = client.get();
      
      dataNode = objectMapper.readTree(response.getStream()).get("data");
      assertEquals(2L, dataNode.size());
      assertEquals(firstDeployment.getId(), dataNode.get(0).get("id").textValue());
      assertEquals(secondDeployment.getId(), dataNode.get(1).get("id").textValue());
      client.release();
      
      // Check ordering by tenantId
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION)
              + "?sort=tenantId&order=desc");
      response = client.get();
      
      dataNode = objectMapper.readTree(response.getStream()).get("data");
      assertEquals(2L, dataNode.size());
      assertEquals(secondDeployment.getId(), dataNode.get(0).get("id").textValue());
      assertEquals(firstDeployment.getId(), dataNode.get(1).get("id").textValue());
      client.release();
      
      // Check paging
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION)
              + "?sort=deployTime&order=asc&start=1&size=1");
      response = client.get();
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      dataNode = responseNode.get("data");
      assertEquals(1L, dataNode.size());
      assertEquals(secondDeployment.getId(), dataNode.get(0).get("id").textValue());
      assertEquals(2L, responseNode.get("total").longValue());
      assertEquals(1L, responseNode.get("start").longValue());
      assertEquals(1L, responseNode.get("size").longValue());
      
    } finally {
      // Always cleanup any created deployments, even if the test failed
      List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
      for(Deployment deployment : deployments) {
        repositoryService.deleteDeployment(deployment.getId(), true);
      }
    }
  }
}
