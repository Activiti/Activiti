package org.activiti.rest.service.api.repository;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.activiti.engine.repository.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test for all REST-operations related to listing the resources that are part of a deployment.
 * 
 * @author Frederik Heremans
 */
public class DeploymentResourcesResourceTest extends BaseSpringRestTestCase {

  /**
  * Test getting all resources for a single deployment.
  * GET repository/deployments/{deploymentId}/resources
  */
  public void testGetDeploymentResources() throws Exception {

    try {
      Deployment deployment = repositoryService.createDeployment().name("Deployment 1")
              .addClasspathResource("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml")
              .addInputStream("test.txt", new ByteArrayInputStream("Test content".getBytes()))
              .deploy();
      
      HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCES, deployment.getId()));
      CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertTrue(responseNode.isArray());
      assertEquals(2, responseNode.size());
      
      // Since resources can be returned in any arbitrary order, find the right one to check
      JsonNode txtNode = null;
      for(int i=0; i< responseNode.size(); i++) {
        if("test.txt".equals(responseNode.get(i).get("id").textValue())) {
          txtNode = responseNode.get(i);
          break;
        }
      }
      
      // Check URL's for the resource
      assertNotNull(txtNode);
      assertTrue(txtNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_DEPLOYMENT_RESOURCE, deployment.getId(), "test.txt")));
      assertTrue(txtNode.get("contentUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_DEPLOYMENT_RESOURCE_CONTENT, deployment.getId(), "test.txt")));
      assertTrue(txtNode.get("mediaType").isNull());
      assertEquals("resource", txtNode.get("type").textValue());
      
    } finally {
      // Always cleanup any created deployments, even if the test failed
      List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
      for(Deployment deployment : deployments) {
        repositoryService.deleteDeployment(deployment.getId(), true);
      }
    }
  }
  
  /**
   * Test getting all resources for a single unexisting deployment.
   * GET repository/deployments/{deploymentId}/resources
   */
   public void testGetDeploymentResourcesUnexistingDeployment() throws Exception {
     HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + 
         RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCES, "unexisting"));
     closeResponse(executeRequest(httpGet, HttpStatus.SC_NOT_FOUND));
   }
}
