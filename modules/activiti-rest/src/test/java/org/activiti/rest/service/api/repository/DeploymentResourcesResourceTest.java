package org.activiti.rest.service.api.repository;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.activiti.engine.repository.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Test for all REST-operations related to listing the resources that are part of a deployment.
 * 
 * @author Frederik Heremans
 */
public class DeploymentResourcesResourceTest extends BaseRestTestCase {

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
      
      ClientResource client = getAuthenticatedClient(
              RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCES, deployment.getId()));
      Representation response = client.get();
      
      // Check "OK" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertTrue(responseNode.isArray());
      assertEquals(2, responseNode.size());
      
      // Since resources can be returned in any arbitrary order, find the right one to check
      JsonNode txtNode = null;
      for(int i=0; i< responseNode.size(); i++) {
        if("test.txt".equals(responseNode.get(i).get("id").getTextValue())) {
          txtNode = responseNode.get(i);
          break;
        }
      }
      
      // Check URL's for the resource
      assertNotNull(txtNode);
      assertTrue(txtNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_DEPLOYMENT_RESOURCE, deployment.getId(), "test.txt")));
      assertTrue(txtNode.get("contentUrl").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_DEPLOYMENT_RESOURCE_CONTENT, deployment.getId(), "test.txt")));
      assertTrue(txtNode.get("mediaType").isNull());
      assertEquals("resource", txtNode.get("type").getTextValue());
      
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

     ClientResource client = getAuthenticatedClient(
             RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCES, "unexisting"));
     
     try {
       client.get();
       fail("Expected 404 status, but was: " + client.getStatus());
     } catch(ResourceException expected) {
       assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
       assertEquals("Could not find a deployment with id 'unexisting'.", expected.getStatus().getDescription());
     }
   }
}
