package org.activiti.engine.test.api.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;

public class ProcessDefinitionQueryByLatestTest extends PluggableActivitiTestCase {

	private static String XML_FILE_PATH = "org/activiti/engine/test/repository/latest/";
	
	  @Override
	  protected void setUp() throws Exception {
	    super.setUp();
	  }
	  
	  @Override
	  protected void tearDown() throws Exception {
	    super.tearDown();
	}
	
	protected List<String> deploy(List<String> xmlFileNameList) throws Exception {
		List<String> deploymentIdList = new ArrayList<String>();
		for(String xmlFileName : xmlFileNameList){
		    String deploymentId = repositoryService
		  	      .createDeployment()
		  	      .name(XML_FILE_PATH + xmlFileName)
		  	      .addClasspathResource(XML_FILE_PATH + xmlFileName)
		  	      .deploy()
		  	      .getId();
		    deploymentIdList.add(deploymentId);
		}
		return deploymentIdList;
	}

	private void unDeploy(List<String> deploymentIdList) throws Exception {
		for(String deploymentId : deploymentIdList){
			repositoryService.deleteDeployment(deploymentId, true);
		}
	}

	public void testQueryByLatestAndId() throws Exception {
		// Deploy
		List<String> xmlFileNameList = Arrays.asList("name_testProcess1_one.bpmn20.xml",
				"name_testProcess1_two.bpmn20.xml", "name_testProcess2_one.bpmn20.xml");
		List<String> deploymentIdList = deploy(xmlFileNameList);
		
		List<String> processDefinitionIdList = new ArrayList<String>();
		for(String deploymentId : deploymentIdList){
			String processDefinitionId = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).list().get(0).getId();
			processDefinitionIdList.add(processDefinitionId);
		}

		ProcessDefinitionQuery idQuery1 = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionIdList.get(0)).latestVersion();
		List<ProcessDefinition>  processDefinitions = idQuery1.list();
		assertEquals(0, processDefinitions.size());

		ProcessDefinitionQuery idQuery2 = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionIdList.get(1)).latestVersion();
		processDefinitions = idQuery2.list();
		assertEquals(1, processDefinitions.size());
		assertEquals("testProcess1", processDefinitions.get(0).getKey());

		ProcessDefinitionQuery idQuery3 = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionIdList.get(2)).latestVersion();
		processDefinitions = idQuery3.list();
		assertEquals(1, processDefinitions.size());
		assertEquals("testProcess2", processDefinitions.get(0).getKey());

		// Undeploy
		unDeploy(deploymentIdList);
	}

	public void testQueryByLatestAndName() throws Exception {
		// Deploy
		List<String> xmlFileNameList = Arrays.asList("name_testProcess1_one.bpmn20.xml",
				"name_testProcess1_two.bpmn20.xml", "name_testProcess2_one.bpmn20.xml");
		List<String> deploymentIdList = deploy(xmlFileNameList);

		// name
		ProcessDefinitionQuery nameQuery = repositoryService.createProcessDefinitionQuery().processDefinitionName("one").latestVersion();
		List<ProcessDefinition> processDefinitions = nameQuery.list();
		assertEquals(1, processDefinitions.size());
		assertEquals(1, processDefinitions.get(0).getVersion());
		assertEquals("testProcess2", processDefinitions.get(0).getKey());

		// nameLike
		ProcessDefinitionQuery nameLikeQuery = repositoryService.createProcessDefinitionQuery().processDefinitionName("one").latestVersion();
		processDefinitions = nameLikeQuery.list();
		assertEquals(1, processDefinitions.size());
		assertEquals(1, processDefinitions.get(0).getVersion());
		assertEquals("testProcess2", processDefinitions.get(0).getKey());
		
		// Undeploy
		unDeploy(deploymentIdList);
	}

	public void testQueryByLatestAndVersion() throws Exception {
		// Deploy
		List<String> xmlFileNameList = Arrays.asList("version_testProcess1_one.bpmn20.xml",
				"version_testProcess1_two.bpmn20.xml", "version_testProcess2_one.bpmn20.xml");
		List<String> deploymentIdList = deploy(xmlFileNameList);

		// version
		ProcessDefinitionQuery nameQuery = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(1).latestVersion();
		List<ProcessDefinition> processDefinitions = nameQuery.list();
		assertEquals(1, processDefinitions.size());
		assertEquals("testProcess2", processDefinitions.get(0).getKey());

		// Undeploy
		unDeploy(deploymentIdList);
	}

	public void testQueryByLatestAndDeploymentId() throws Exception {
		// Deploy
		List<String> xmlFileNameList = Arrays.asList("name_testProcess1_one.bpmn20.xml",
				"name_testProcess1_two.bpmn20.xml", "name_testProcess2_one.bpmn20.xml");
		List<String> deploymentIdList = deploy(xmlFileNameList);

		// deploymentId
		ProcessDefinitionQuery deploymentQuery1 = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentIdList.get(0)).latestVersion();
		List<ProcessDefinition> processDefinitions = deploymentQuery1.list();
		assertEquals(0, processDefinitions.size());

		ProcessDefinitionQuery deploymentQuery2 = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentIdList.get(1)).latestVersion();
		processDefinitions = deploymentQuery2.list();
		assertEquals(1, processDefinitions.size());
		assertEquals("testProcess1", processDefinitions.get(0).getKey());

		// Undeploy
		unDeploy(deploymentIdList);
	}
}
