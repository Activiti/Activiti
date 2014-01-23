package org.activiti.rest.service.api.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.HttpMultipartRepresentation;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Test for all REST-operations related to a single Deployment resource.
 * 
 * @author Frederik Heremans
 */
public class DeploymentResourceTest extends BaseRestTestCase {

  /**
   * Test deploying singe bpmn-file.
   * POST repository/deployments
   */
  public void testPostNewDeploymentBPMNFile() throws Exception {
    try {
      // Upload a valid BPMN-file using multipart-data
      Representation uploadRepresentation = new HttpMultipartRepresentation("oneTaskProcess.bpmn20.xml",
              ReflectUtil.getResourceAsStream("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"));
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION));
      Representation response = client.post(uploadRepresentation);
      
      // Check "CREATED" status
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      // Check deployment
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      
      String deploymentId = responseNode.get("id").getTextValue();
      String name = responseNode.get("name").getTextValue();
      String category = responseNode.get("category").getTextValue();
      String deployTime = responseNode.get("deploymentTime").getTextValue();
      String url = responseNode.get("url").getTextValue();
      String tenantId = responseNode.get("tenantId").getTextValue();
      
      assertNull(tenantId);
      
      assertNotNull(deploymentId);
      assertEquals(1L, repositoryService.createDeploymentQuery().deploymentId(deploymentId).count());
      
      assertNotNull(name);
      assertEquals("oneTaskProcess.bpmn20.xml", name);
      
      assertNotNull(url);
      assertTrue(url.endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT,deploymentId)));
      
      // No deployment-category should have been set
      assertNull(category);
      assertNotNull(deployTime);
      
      // Check if process is actually deployed in the deployment
      List<String> resources = repositoryService.getDeploymentResourceNames(deploymentId);
      assertEquals(1L, resources.size());
      assertEquals("oneTaskProcess.bpmn20.xml", resources.get(0));
      assertEquals(1L, repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).count());
      
      
    } finally {
      // Always cleanup any created deployments, even if the test failed
      List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
      for(Deployment deployment : deployments) {
        repositoryService.deleteDeployment(deployment.getId(), true);
      }
    }
  }
  
  /**
   * Test deploying bar-file.
   * POST repository/deployments
   */
  public void testPostNewDeploymentBarFile() throws Exception {
    try {
      // Create zip with bpmn-file and resource
      ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();
      ZipOutputStream zipStream = new ZipOutputStream(zipOutput);
      
      // Add bpmn-xml
      zipStream.putNextEntry(new ZipEntry("oneTaskProcess.bpmn20.xml"));
      IOUtils.copy(ReflectUtil.getResourceAsStream("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"), zipStream);
      zipStream.closeEntry();
      
      // Add text-resource
      zipStream.putNextEntry(new ZipEntry("test.txt"));
      IOUtils.write("Testing REST-deployment with tenant", zipStream);
      zipStream.closeEntry();
      zipStream.close();
      
      // Upload a bar-file using multipart-data
      Representation uploadRepresentation = new HttpMultipartRepresentation("test-deployment.bar",
              new ByteArrayInputStream(zipOutput.toByteArray()));
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION));
      Representation response = client.post(uploadRepresentation);
        
      // Check "CREATED" status
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      // Check deployment
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      
      String deploymentId = responseNode.get("id").getTextValue();
      String name = responseNode.get("name").getTextValue();
      String category = responseNode.get("category").getTextValue();
      String deployTime = responseNode.get("deploymentTime").getTextValue();
      String url = responseNode.get("url").getTextValue();
      String tenantId = responseNode.get("tenantId").getTextValue();
      
      assertNull(tenantId);
      assertNotNull(deploymentId);
      assertEquals(1L, repositoryService.createDeploymentQuery().deploymentId(deploymentId).count());
      
      assertNotNull(name);
      assertEquals("test-deployment.bar", name);
      
      assertNotNull(url);
      assertTrue(url.endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT,deploymentId)));
      
      // No deployment-category should have been set
      assertNull(category);
      assertNotNull(deployTime);
      
      // Check if both resources are deployed and process is actually deployed in the deployment
      List<String> resources = repositoryService.getDeploymentResourceNames(deploymentId);
      assertEquals(2L, resources.size());
      assertEquals(1L, repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).count());
    } finally {
      // Always cleanup any created deployments, even if the test failed
      List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
      for(Deployment deployment : deployments) {
        repositoryService.deleteDeployment(deployment.getId(), true);
      }
    }
  }
  
 /** Test deploying bar-file.
  * POST repository/deployments
  */
 public void testPostNewDeploymentBarFileWithTenantId() throws Exception {
   try {
     // Create zip with bpmn-file and resource
     ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();
     ZipOutputStream zipStream = new ZipOutputStream(zipOutput);
     
     // Add bpmn-xml
     zipStream.putNextEntry(new ZipEntry("oneTaskProcess.bpmn20.xml"));
     IOUtils.copy(ReflectUtil.getResourceAsStream("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"), zipStream);
     zipStream.closeEntry();
     
     // Add text-resource
     zipStream.putNextEntry(new ZipEntry("test.txt"));
     IOUtils.write("Testing REST-deployment", zipStream);
     zipStream.closeEntry();
     zipStream.close();
     
     // Upload a bar-file using multipart-data
     
     Representation uploadRepresentation = new HttpMultipartRepresentation("test-deployment.bar",
             new ByteArrayInputStream(zipOutput.toByteArray()), Collections.singletonMap("tenantId", "myTenant"));
     
     ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION));
     Representation response = client.post(uploadRepresentation);
     
     // Check "CREATED" status
     assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
     
     // Check deployment
     JsonNode responseNode = objectMapper.readTree(response.getStream());
     
     String tenantId = responseNode.get("tenantId").getTextValue();
     assertEquals("myTenant", tenantId);
     String id = responseNode.get("id").getTextValue();
     
     Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(id).singleResult();
     assertNotNull(deployment);
     assertEquals("myTenant", deployment.getTenantId());
     
   } finally {
     // Always cleanup any created deployments, even if the test failed
     List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
     for(Deployment deployment : deployments) {
       repositoryService.deleteDeployment(deployment.getId(), true);
     }
   }
 }
  
  /**
   * Test deploying an invalid file.
   * POST repository/deployments
   */
  public void testPostNewDeploymentInvalidFile() throws Exception {
      // Upload a valid BPMN-file using multipart-data
      Representation uploadRepresentation = new HttpMultipartRepresentation("oneTaskProcess.invalidfile",
              ReflectUtil.getResourceAsStream("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"));
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT_COLLECTION));
      try {
        client.post(uploadRepresentation);
        fail("400 expected, but was: " + client.getResponse().getStatus());
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("File must be of type .bpmn20.xml, .bpmn, .bar or .zip", expected.getStatus().getDescription());
      }
  }
  
  /**
  * Test getting a single deployment.
  * GET repository/deployments/{deploymentId}
  */
  @org.activiti.engine.test.Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testGetDeployment() throws Exception {
    Deployment existingDeployment = repositoryService.createDeploymentQuery().singleResult();
     
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, existingDeployment.getId()));
    Representation response = client.get();
    
    // Check "OK" status
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
     
    String deploymentId = responseNode.get("id").getTextValue();
    String name = responseNode.get("name").getTextValue();
    String category = responseNode.get("category").getTextValue();
    String deployTime = responseNode.get("deploymentTime").getTextValue();
    String url = responseNode.get("url").getTextValue();
    String tenantId = responseNode.get("tenantId").getTextValue();
    
    assertNull(tenantId);
    assertNotNull(deploymentId);
    assertEquals(existingDeployment.getId(), deploymentId);
    
    assertNotNull(name);
    assertEquals(existingDeployment.getName(), name);
    
    assertEquals(existingDeployment.getCategory(), category);
    
    assertNotNull(deployTime);
    
    assertNotNull(url);
    assertTrue(url.endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT,deploymentId)));
  }
  
  /**
   * Test getting an unexisting deployment.
   * GET repository/deployments/{deploymentId}
   */
   public void testGetUnexistingDeployment() throws Exception {
     ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, "unexisting"));
     try {
       client.get();
       fail("404 expected, but was: " + client.getResponse().getStatus());
     } catch(ResourceException expected) {
       assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
       assertEquals("Could not find a deployment with id 'unexisting'.", client.getResponse().getStatus().getDescription());
     }
   }
  
  /**
   * Test deleting a single deployment.
   * DELETE repository/deployments/{deploymentId}
   */
   @org.activiti.engine.test.Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
   public void testDeleteDeployment() throws Exception {
     Deployment existingDeployment = repositoryService.createDeploymentQuery().singleResult();
     assertNotNull(existingDeployment);
     
     // Delete the deployment
     ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, existingDeployment.getId()));
     client.delete();
     
     // Check status
     assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
     
     existingDeployment = repositoryService.createDeploymentQuery().singleResult();
     assertNull(existingDeployment);
   }
   
   /**
    * Test deleting an unexisting deployment.
    * GET repository/deployments/{deploymentId}
    */
    public void testDeleteUnexistingDeployment() throws Exception {
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, "unexisting"));
      try {
        client.delete();
        fail("404 expected, but was: " + client.getResponse().getStatus());
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
        assertEquals("Could not find a deployment with id 'unexisting'.", client.getResponse().getStatus().getDescription());
      }
    }
}
