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
 * Test for all REST-operations related to a resources that is part of a deployment.
 * 
 * @author Frederik Heremans
 */
public class DeploymentResourceResourceTest extends BaseRestTestCase {

  /**
  * Test getting a single resource, deployed in a deployment.
  * GET repository/deployments/{deploymentId}/resources/{resourceId}
  */
  public void testGetDeploymentResource() throws Exception {
    try {
      String rawResourceName = "org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml";
      Deployment deployment = repositoryService.createDeployment().name("Deployment 1")
              .addClasspathResource(rawResourceName)
              .addInputStream("test.txt", new ByteArrayInputStream("Test content".getBytes()))
              .deploy();
      
      // Build up the URL manually to make sure resource-id gets encoded correctly as one piece
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCES, deployment.getId()));
      client.getReference().addSegment(rawResourceName);
      Representation response = client.get();
      
      // Id contains slashes so it will be encoded by the client
      String encodedResourceId = client.getReference().getLastSegment(false);
      
      // Check "OK" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      
      // Check URL's for the resource
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_DEPLOYMENT_RESOURCE, deployment.getId(), encodedResourceId)));
      assertTrue(responseNode.get("contentUrl").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_DEPLOYMENT_RESOURCE_CONTENT, deployment.getId(), encodedResourceId)));
      assertEquals("text/xml", responseNode.get("mediaType").getTextValue());
      assertEquals("processDefinition", responseNode.get("type").getTextValue());
      
    } finally {
      // Always cleanup any created deployments, even if the test failed
      List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
      for(Deployment deployment : deployments) {
        repositoryService.deleteDeployment(deployment.getId(), true);
      }
    }
  }
  
  /**
   * Test getting a single resource for an unexisting deployment.
   * GET repository/deployments/{deploymentId}/resources/{resourceId}
   */
   public void testGetDeploymentResourceUnexistingDeployment() throws Exception {

     ClientResource client = getAuthenticatedClient(
             RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE, "unexisting", "resource.png"));
     try {
       client.get();
       fail("Expected 404 status, but was: " + client.getStatus());
     } catch(ResourceException expected) {
       assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
       assertEquals("Could not find a deployment with id 'unexisting'.", expected.getStatus().getDescription());
     }
   }
   
   /**
    * Test getting an unexisting resource for an existing deployment.
    * GET repository/deployments/{deploymentId}/resources/{resourceId}
    */
   public void testGetDeploymentResourceUnexistingResource() throws Exception {
     try {
       Deployment deployment = repositoryService.createDeployment().name("Deployment 1")
               .addInputStream("test.txt", new ByteArrayInputStream("Test content".getBytes()))
               .deploy();
       
       ClientResource client = getAuthenticatedClient(
               RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE, deployment.getId(), "unexisting-resource.png"));
       
       try {
         client.get();
         fail("Expected 404 status, but was: " + client.getStatus());
       } catch(ResourceException expected) {
         assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
         assertEquals("Could not find a resource with id 'unexisting-resource.png' in deployment '" + deployment.getId() + "'.", expected.getStatus().getDescription());
       }
       
     } finally {
       // Always cleanup any created deployments, even if the test failed
       List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
       for(Deployment deployment : deployments) {
         repositoryService.deleteDeployment(deployment.getId(), true);
       }
     }
   }
   
   /**
    * Test getting a deployment resource content.
    * GET repository/deployments/{deploymentId}/resources/{resourceId}
    */
   public void testGetDeploymentResourceContent() throws Exception {
     try {
       Deployment deployment = repositoryService.createDeployment().name("Deployment 1")
               .addInputStream("test.txt", new ByteArrayInputStream("Test content".getBytes()))
               .deploy();
       
       ClientResource client = getAuthenticatedClient(
               RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE_CONTENT, deployment.getId(), "test.txt"));
       client.get();
       
       String responseAsString = client.getResponse().getEntityAsText();
       assertNotNull(responseAsString);
       assertEquals("Test content", responseAsString);
       
     } finally {
       // Always cleanup any created deployments, even if the test failed
       List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
       for(Deployment deployment : deployments) {
         repositoryService.deleteDeployment(deployment.getId(), true);
       }
     }
   }
}
