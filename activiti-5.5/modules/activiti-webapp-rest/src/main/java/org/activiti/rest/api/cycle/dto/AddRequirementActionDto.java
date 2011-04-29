package org.activiti.rest.api.cycle.dto;

public class AddRequirementActionDto {

  private String requirementsFolderId, requirementsFolderConnectorId;

  public String getRequirementsFolderConnectorId() {
    return requirementsFolderConnectorId;
  }

  public String getRequirementsFolderId() {
    return requirementsFolderId;
  }

  public void setRequirementsFolderConnectorId(String requirementsFolderConnectorId) {
    this.requirementsFolderConnectorId = requirementsFolderConnectorId;
  }

  public void setRequirementsFolderId(String requirementsFolderId) {
    this.requirementsFolderId = requirementsFolderId;
  }

}
