package org.activiti.rest.service.api.legacy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.HttpMultipartRepresentation;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Test for all <b>Legacy</b> REST-operations related to Deployments:
 *  
 * POST /deployment, 
 * DELETE /deployment/{deploymentId}, 
 * GET /deployments, 
 * POST /deployments/delete,
 * GET /deployment/{deploymentId}/resources,
 * GET /deployment/{deploymentId}/resources/{resourceName}
 * 
 * @author Frederik Heremans
 */
public class LegacyDeploymentResourseTest extends BaseRestTestCase {

  /**
   * Test deploying singe bpmn-file.
   * POST deployment
   */
  public void testPostNewDeploymentBPMNFile() throws Exception {
    try {
      // Upload a valid BPMN-file using multipart-data
      Representation uploadRepresentation = new HttpMultipartRepresentation("oneTaskProcess.bpmn20.xml",
              ReflectUtil.getResourceAsStream("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"));
      
      ClientResource client = getAuthenticatedClient("deployment");
      Representation response = client.post(uploadRepresentation);
      
      // Check "CREATED" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      // Check deployment
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      
      String deploymentId = responseNode.get("id").getTextValue();
      String name = responseNode.get("name").getTextValue();
      String category = responseNode.get("category").getTextValue();
      String deployTime = responseNode.get("deploymentTime").getTextValue();
      
      assertNotNull(deploymentId);
      assertEquals(1L, repositoryService.createDeploymentQuery().deploymentId(deploymentId).count());
      
      assertNotNull(name);
      assertEquals("oneTaskProcess.bpmn20.xml", name);
      
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
   * POST deployment
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
      IOUtils.write("Testing REST-deployment", zipStream);
      zipStream.closeEntry();
      zipStream.close();
      
      // Upload a bar-file using multipart-data
      Representation uploadRepresentation = new HttpMultipartRepresentation("test-deployment.bar",
              new ByteArrayInputStream(zipOutput.toByteArray()));
      ClientResource client = getAuthenticatedClient("deployment");
      Representation response = client.post(uploadRepresentation);
      
      // Check "CREATED" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      // Check deployment
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      
      String deploymentId = responseNode.get("id").getTextValue();
      String name = responseNode.get("name").getTextValue();
      String category = responseNode.get("category").getTextValue();
      String deployTime = responseNode.get("deploymentTime").getTextValue();
      
      assertNotNull(deploymentId);
      assertEquals(1L, repositoryService.createDeploymentQuery().deploymentId(deploymentId).count());
      
      assertNotNull(name);
      assertEquals("test-deployment.bar", name);
      
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
  
  /**
   * Test deploying an invalid file.
   * POST deployment
   */
  public void testPostNewDeploymentInvalidFile() throws Exception {
      // Upload a valid BPMN-file using multipart-data
      Representation uploadRepresentation = new HttpMultipartRepresentation("oneTaskProcess.invalidfile",
              ReflectUtil.getResourceAsStream("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"));
      
      ClientResource client = getAuthenticatedClient("deployment");
      try {
        client.post(uploadRepresentation);
        fail("400 expected, but was: " + client.getResponse().getStatus());
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("File must be of type .bpmn20.xml, .bpmn, .bar or .zip", expected.getStatus().getDescription());
      }
  }
  
  /**
   * Test deleting a single deployment.
   * DELETE deployment/{deploymentId}
   */
   @org.activiti.engine.test.Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
   public void testDeleteDeployment() throws Exception {
     Deployment existingDeployment = repositoryService.createDeploymentQuery().singleResult();
     assertNotNull(existingDeployment);
     
     // Delete the deployment
     ClientResource client = getAuthenticatedClient("deployment/" + existingDeployment.getId());
     Representation response = client.delete();
     
     // Check status
     assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
     
     // Check "success" response body
     JsonNode responseNode = objectMapper.readTree(response.getStream());
     assertEquals(Boolean.TRUE.booleanValue(), responseNode.get("success").asBoolean());
     
     existingDeployment = repositoryService.createDeploymentQuery().singleResult();
     assertNull(existingDeployment);
   }
   
   /**
    * Test getting deployments.
    * GET deployments
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
                .deploy();
        
        ClientResource client = getAuthenticatedClient("deployments?sort=name&order=asc");
        Representation response = client.get();
        
        assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
        JsonNode dataNode = objectMapper.readTree(response.getStream()).get("data");
        assertEquals(2L, dataNode.size());
               
        assertEquals(firstDeployment.getId(), dataNode.get(0).get("id").getTextValue());
        assertEquals(secondDeployment.getId(), dataNode.get(1).get("id").getTextValue());
        
      } finally {
        // Always cleanup any created deployments, even if the test failed
        List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for(Deployment deployment : deployments) {
          repositoryService.deleteDeployment(deployment.getId(), true);
        }
      }
    }
    
    /**
     * Test deleting multiple deployments.
     * POST deployments/delete
     */
     public void testDeleteMultipleDeployments() throws Exception {
       try {
         Deployment firstDeployment = repositoryService.createDeployment().name("Deployment 1")
             .category("DEF")
             .addClasspathResource("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml")
             .deploy();
         
         Deployment secondDeployment = repositoryService.createDeployment().name("Deployment 2")
                 .category("ABC")
                 .addClasspathResource("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml")
                 .deploy();
         
         ClientResource client = getAuthenticatedClient("deployments/delete");
         
         ObjectNode requestNode = objectMapper.createObjectNode();
         ArrayNode deploymentIds = objectMapper.createArrayNode();
         deploymentIds.add(firstDeployment.getId());
         deploymentIds.add(secondDeployment.getId());
         requestNode.put("deploymentIds", deploymentIds);
         
         Representation response = client.post(requestNode);
         assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
         
         // Check "success" response body
         JsonNode responseNode = objectMapper.readTree(response.getStream());
         assertEquals(Boolean.TRUE.booleanValue(), responseNode.get("success").asBoolean());
         
         // Check if both deployments are deleted
         assertEquals(0L, repositoryService.createDeploymentQuery().count());
         
       } finally {
         // Always cleanup any created deployments, even if the test failed
         List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
         for(Deployment deployment : deployments) {
           repositoryService.deleteDeployment(deployment.getId(), true);
         }
       }
     }
     
     /**
      * Test deleting multiple deployments.
      * POST deployments/delete
      */
    public void testDeleteMultipleDeploymentsUnexisting() throws Exception {
      try {
        ClientResource client = getAuthenticatedClient("deployments/delete");
        
        ObjectNode requestNode = objectMapper.createObjectNode();
        ArrayNode deploymentIds = objectMapper.createArrayNode();
        deploymentIds.add("unexisting");
        requestNode.put("deploymentIds", deploymentIds);
        
        try {
          client.post(requestNode);
          fail("Exception expected");
        } catch(ResourceException expected) {
          
          assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
          assertEquals("Could not find a deployment with id 'unexisting'.", expected.getStatus().getDescription());
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
     * Test get all resources in a deployment.
     * GET /deployment/{deploymentId}/resources
     */
     public void testGetDeploymentResources() throws Exception {
       try {
         Deployment deployment = repositoryService.createDeployment().name("Deployment 1")
             .addClasspathResource("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml")
             .addInputStream("test.png", new ByteArrayInputStream("test-content".getBytes()))
             .deploy();
         
         ClientResource client = getAuthenticatedClient("deployment/" + deployment.getId() + "/resources");
         
         Representation response = client.get();
         assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
         
         JsonNode resourcesNode = objectMapper.readTree(response.getStream()).get("resources");
         assertEquals(2L, resourcesNode.size());
         
         List<String> resources = new ArrayList<String>();
         for(int i=0; i < resourcesNode.size(); i++) {
           resources.add(resourcesNode.get(i).getTextValue());
         }
         assertTrue(resources.contains("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"));
         assertTrue(resources.contains("test.png"));
         
       } finally {
         // Always cleanup any created deployments, even if the test failed
         List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
         for(Deployment deployment : deployments) {
           repositoryService.deleteDeployment(deployment.getId(), true);
         }
       }
     }
     
     /**
      * Test get all resources in a deployment.
      * GET /deployment/{deploymentId}/resources/{resourceName}
      */
      public void testGetDeploymentResource() throws Exception {
        try {
          Deployment deployment = repositoryService.createDeployment().name("Deployment 1")
              .addClasspathResource("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml")
              .addInputStream("test.png", new ByteArrayInputStream("test-content".getBytes()))
              .deploy();
          
          ClientResource client = getAuthenticatedClient("deployment/" + deployment.getId() + "/resource/org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml");
          
          Representation response = client.get();
          assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
          
          // Read returned and original content
          ByteArrayOutputStream responseContent = new ByteArrayOutputStream();
          IOUtils.copy(response.getStream(), responseContent);
          ByteArrayOutputStream requestContent = new ByteArrayOutputStream();
          IOUtils.copy(ReflectUtil.getResourceAsStream("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"), requestContent);
          
          // Compare response with content that is deployed
          assertTrue("Returned content doesn't match deployed content",
                  Arrays.equals(requestContent.toByteArray(), responseContent.toByteArray()));
          
        } finally {
          // Always cleanup any created deployments, even if the test failed
          List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
          for(Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId(), true);
          }
        }
      }
}
