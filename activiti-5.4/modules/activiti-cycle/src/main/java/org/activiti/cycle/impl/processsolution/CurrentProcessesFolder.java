package org.activiti.cycle.impl.processsolution;

import java.io.Serializable;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(name = "currentProcessesFolder", context = CycleContextType.SESSION) // TODO: make REQUEST-scoped
public class CurrentProcessesFolder implements Serializable {

  private String connectorId;
  private String folderId;

  public String getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(String connectorId) {
    this.connectorId = connectorId;
  }

  public String getFolderId() {
    return folderId;
  }

  public void setFolderId(String folderId) {
    this.folderId = folderId;
  }

}
