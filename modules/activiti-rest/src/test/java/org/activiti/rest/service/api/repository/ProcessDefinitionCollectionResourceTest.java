package org.activiti.rest.service.api.repository;

import java.util.List;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test for all REST-operations related to the Deployment collection.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class ProcessDefinitionCollectionResourceTest extends BaseSpringRestTestCase {
  
  /**
  * Test getting process definitions.
  * GET repository/process-definitions
  */
  public void testGetProcessDefinitions() throws Exception {
    
    try {
      Deployment firstDeployment = repositoryService.createDeployment().name("Deployment 1")
          .addClasspathResource("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml")
          .deploy();
      
      Deployment secondDeployment = repositoryService.createDeployment().name("Deployment 2")
              .addClasspathResource("org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml")
              .addClasspathResource("org/activiti/rest/service/api/repository/twoTaskProcess.bpmn20.xml")
              .deploy();
      
      Deployment thirdDeployment = repositoryService.createDeployment().name("Deployment 3")
          .addClasspathResource("org/activiti/rest/service/api/repository/oneTaskProcessWithDi.bpmn20.xml")
          .deploy();
      
      ProcessDefinition oneTaskProcess = repositoryService.createProcessDefinitionQuery()
              .processDefinitionKey("oneTaskProcess").deploymentId(firstDeployment.getId()).singleResult();
      
      ProcessDefinition latestOneTaskProcess = repositoryService.createProcessDefinitionQuery()
              .processDefinitionKey("oneTaskProcess").deploymentId(secondDeployment.getId()).singleResult();
      
      ProcessDefinition twoTaskprocess = repositoryService.createProcessDefinitionQuery()
              .processDefinitionKey("twoTaskProcess").deploymentId(secondDeployment.getId()).singleResult();
      
      ProcessDefinition oneTaskWithDiProcess = repositoryService.createProcessDefinitionQuery()
          .processDefinitionKey("oneTaskProcessWithDi").deploymentId(thirdDeployment.getId()).singleResult();
      

      // Test parameterless call
      String baseUrl = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_COLLECTION);
      assertResultsPresentInDataResponse(baseUrl, oneTaskProcess.getId(), twoTaskprocess.getId(), latestOneTaskProcess.getId(), oneTaskWithDiProcess.getId());
      
      // Verify ACT-2141 Persistent isGraphicalNotation flag for process definitions
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + baseUrl), HttpStatus.SC_OK);
      JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
      closeResponse(response);
      for (int i=0; i<dataNode.size(); i++) {
      	JsonNode processDefinitionJson = dataNode.get(i);
      	
      	String key = processDefinitionJson.get("key").asText();
      	JsonNode graphicalNotationNode = processDefinitionJson.get("graphicalNotationDefined");
      	if (key.equals("oneTaskProcessWithDi")) {
      		assertTrue(graphicalNotationNode.asBoolean());
      	} else {
      		assertFalse(graphicalNotationNode.asBoolean());
      	}
      	
      }
      
      // Verify
      
      // Test name filtering
      String url = baseUrl + "?name=" + encode("The Two Task Process");
      assertResultsPresentInDataResponse(url, twoTaskprocess.getId());
      
      // Test nameLike filtering
      url = baseUrl + "?nameLike=" + encode("The Two%");
      assertResultsPresentInDataResponse(url, twoTaskprocess.getId());
      
      // Test key filtering
      url = baseUrl + "?key=twoTaskProcess";
      assertResultsPresentInDataResponse(url, twoTaskprocess.getId());
      
      // Test returning multiple versions for the same key
      url = baseUrl + "?key=oneTaskProcess";
      assertResultsPresentInDataResponse(url, oneTaskProcess.getId(), latestOneTaskProcess.getId());
      
      // Test keyLike filtering
      url = baseUrl + "?keyLike=" + encode("two%");
      assertResultsPresentInDataResponse(url, twoTaskprocess.getId());
      
      // Test category filtering
      url = baseUrl + "?category=TwoTaskCategory";
      assertResultsPresentInDataResponse(url, twoTaskprocess.getId());
      
      // Test categoryLike filtering
      url = baseUrl + "?categoryLike=" + encode("Two%");
      assertResultsPresentInDataResponse(url, twoTaskprocess.getId());
      
      // Test categoryNotEquals filtering
      url = baseUrl + "?categoryNotEquals=OneTaskCategory";
      assertResultsPresentInDataResponse(url, twoTaskprocess.getId(), oneTaskWithDiProcess.getId());
      
      // Test resourceName filtering
      url = baseUrl + "?resourceName=org/activiti/rest/service/api/repository/twoTaskProcess.bpmn20.xml";
      assertResultsPresentInDataResponse(url, twoTaskprocess.getId());
      
      // Test resourceNameLike filtering
      url = baseUrl + "?resourceNameLike=" + encode("%twoTaskProcess%");
      assertResultsPresentInDataResponse(url, twoTaskprocess.getId());
      
      // Test version filtering
      url = baseUrl + "?version=2";
      assertResultsPresentInDataResponse(url, latestOneTaskProcess.getId());
      
      // Test latest filtering
      url = baseUrl + "?latest=true";
      assertResultsPresentInDataResponse(url, latestOneTaskProcess.getId(), twoTaskprocess.getId(), oneTaskWithDiProcess.getId());
      url = baseUrl + "?latest=false";
      assertResultsPresentInDataResponse(baseUrl, oneTaskProcess.getId(), twoTaskprocess.getId(), latestOneTaskProcess.getId(), oneTaskWithDiProcess.getId());
      
      // Test deploymentId
      url = baseUrl + "?deploymentId=" + secondDeployment.getId();
      assertResultsPresentInDataResponse(url, twoTaskprocess.getId(), latestOneTaskProcess.getId());
      
      // Test startableByUser
      url = baseUrl + "?startableByUser=kermit";
      assertResultsPresentInDataResponse(url, twoTaskprocess.getId());
      
      // Test suspended
      repositoryService.suspendProcessDefinitionById(twoTaskprocess.getId());
      
      url = baseUrl + "?suspended=true";
      assertResultsPresentInDataResponse(url, twoTaskprocess.getId());
      
      url = baseUrl + "?suspended=false";
      assertResultsPresentInDataResponse(url, latestOneTaskProcess.getId(), oneTaskProcess.getId(), oneTaskWithDiProcess.getId());
      
    } finally {
      // Always cleanup any created deployments, even if the test failed
      List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
      for(Deployment deployment : deployments) {
        repositoryService.deleteDeployment(deployment.getId(), true);
      }
    }
  }
}
