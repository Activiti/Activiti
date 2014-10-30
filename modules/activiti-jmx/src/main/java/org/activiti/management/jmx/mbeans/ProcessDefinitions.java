package org.activiti.management.jmx.mbeans;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.management.jmx.annotations.ManagedAttribute;
import org.activiti.management.jmx.annotations.ManagedOperation;
import org.activiti.management.jmx.annotations.ManagedResource;

@ManagedResource(description = "Process definition MBean")
public class ProcessDefinitions {

  RepositoryService repositoryService;
  

  public ProcessDefinitions(ProcessEngineConfiguration processEngineConfig) {
    if (processEngineConfig != null)
      repositoryService = processEngineConfig.getRepositoryService();
  }

  @ManagedAttribute(description = "List of deployed Processes")
  public List<List<String>> getProcessDefinitions() {
    List<ProcessDefinition> deployments = repositoryService.createProcessDefinitionQuery().list();
    List<List<String>> result = new ArrayList<List<String>>(deployments.size());
    for (ProcessDefinition deployment : deployments) {
      List<String> item = new ArrayList<String>(3);
      item.add(deployment.getId());
      item.add(deployment.getName());
      item.add(Integer.toString(deployment.getVersion()));
      item.add(Boolean.toString(deployment.isSuspended()));
      item.add(deployment.getDescription());
      result.add(item);
    }
    return result;

  }

  @ManagedAttribute(description = "List of deployed Processes")
  public List<List<String>> getDepoymentd() {
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    List<List<String>> result = new ArrayList<List<String>>(deployments.size());
    for (Deployment deployment : deployments) {
      List<String> item = new ArrayList<String>(3);
      item.add(deployment.getId());
      item.add(deployment.getName());
      item.add(deployment.getTenantId());
      result.add(item);
    }
    return result;

  }

  @ManagedOperation(description = "undeploy given process ID")
  public void undeployProcessDefinitionById(String processId) {
    try {
      repositoryService.deleteDeployment(processId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @ManagedOperation(description = "Suspend given process ID")
  public void suspendProcessDefinitionById(String processId) {
    try {
      repositoryService.suspendProcessDefinitionById(processId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @ManagedOperation(description = "Activate given process ID")
  public void activatedProcessDefinitionById(String processId) {
    repositoryService.activateProcessDefinitionById(processId);
  }

  @ManagedOperation(description = "Suspend given process ID")
  public void suspendProcessDefinitionByKey(String processDefinitionKey) {
    repositoryService.suspendProcessDefinitionByKey(processDefinitionKey);
  }

  @ManagedOperation(description = "Activate given process ID")
  public void activatedProcessDefinitionByKey(String processDefinitionKey) {
    repositoryService.activateProcessDefinitionByKey(processDefinitionKey);
  }

  @ManagedOperation(description = "Deploy Process Definition")
  public void deployProcessDefinition(String resourceName, String processDefinitionFile) throws FileNotFoundException {
    Deployment deployment =  repositoryService.createDeployment().addInputStream(resourceName, new FileInputStream(processDefinitionFile)).deploy();
  }
  
}
