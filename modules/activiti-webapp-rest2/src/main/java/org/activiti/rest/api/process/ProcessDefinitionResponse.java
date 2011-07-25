package org.activiti.rest.api.process;

import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

public class ProcessDefinitionResponse {
  
  private static final long serialVersionUID = 1L;
  
  String id;
  String key;
  String name;
  int version;
  String deploymentId;
  String resourceName;
  String diagramResourceName;
  String startFormResourceKey;
  boolean isGraphicNotationDefined;

  public ProcessDefinitionResponse(ProcessDefinitionEntity processDefinition) {
    this.setId(processDefinition.getId());
    this.setKey(processDefinition.getKey());
    this.setName(processDefinition.getName());
    this.setVersion(processDefinition.getVersion());
    this.setDeploymentId(processDefinition.getDeploymentId());
    this.setResourceName(processDefinition.getResourceName());
    this.setDiagramResourceName(processDefinition.getDiagramResourceName());
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public String getDiagramResourceName() {
    return diagramResourceName;
  }

  public void setDiagramResourceName(String diagramResourceName) {
    this.diagramResourceName = diagramResourceName;
  }

  public boolean isGraphicNotationDefined() {
    return isGraphicNotationDefined;
  }

  public void setGraphicNotationDefined(boolean graphicNotationDefined) {
    isGraphicNotationDefined = graphicNotationDefined;
  }

  public String getStartFormResourceKey() {
    return startFormResourceKey;
  }

  public void setStartFormResourceKey(String startFormResourceKey) {
    this.startFormResourceKey = startFormResourceKey;
  }
}
