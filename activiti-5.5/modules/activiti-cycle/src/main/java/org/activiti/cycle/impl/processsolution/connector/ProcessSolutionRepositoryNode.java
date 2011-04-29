package org.activiti.cycle.impl.processsolution.connector;

import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeMetadata;
import org.activiti.cycle.impl.RepositoryNodeMetadataImpl;
import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;

/**
 * A {@link RepositoryFolder}-implementation for virtual folders
 * 
 * @author daniel.meyer@camunda.com
 */
public class ProcessSolutionRepositoryNode implements RepositoryNode {

  protected VirtualRepositoryFolder virtualRepositoryFolder;

  protected ProcessSolution processSolution;

  protected RepositoryNode wrappedNode;

  protected String connectorId;

  protected String nodeId;

  public ProcessSolutionRepositoryNode(String connectorId, String nodeId, VirtualRepositoryFolder virtualFolder, ProcessSolution processSolution,
          RepositoryNode wrappedNode) {
    this.virtualRepositoryFolder = virtualFolder;
    this.connectorId = connectorId;
    this.nodeId = nodeId;
    this.processSolution = processSolution;
    this.wrappedNode = wrappedNode;
  }

  public String getConnectorId() {
    if (wrappedNode != null) {
      return wrappedNode.getConnectorId();
    }
    return connectorId;
  }

  public String getNodeId() {
    if (wrappedNode != null) {
      return wrappedNode.getNodeId();
    }
    return nodeId;
  }

  public String getGlobalUniqueId() {
    if (wrappedNode != null) {
      return wrappedNode.getGlobalUniqueId();
    }
    return nodeId;
  }

  public RepositoryNodeMetadata getMetadata() {
    if (wrappedNode != null) {
      if (!wrappedNode.getMetadata().getParentFolderId().equals(virtualRepositoryFolder.getReferencedNodeId())) {
        return wrappedNode.getMetadata();
      }
    }

    return new RepositoryNodeMetadataImpl() {

      public String getName() {
        if (wrappedNode != null) {
          return wrappedNode.getMetadata().getName();
        }
        if (virtualRepositoryFolder != null) {
          return virtualRepositoryFolder.getLabel();
        }
        if (nodeId.endsWith(ProcessSolutionConnector.PS_HOME_NAME)) {
          return "Home";
        }
        return processSolution.getLabel();
      }
      public String getParentFolderId() {
        if (wrappedNode != null) {
          return processSolution.getId() + "/" + virtualRepositoryFolder.getId();
        }
        return "/";
      }
    };
  }

  public VirtualRepositoryFolder getVirtualRepositoryFolder() {
    return virtualRepositoryFolder;
  }

  public void setVirtualRepositoryFolder(VirtualRepositoryFolder virtualRepositoryFolder) {
    this.virtualRepositoryFolder = virtualRepositoryFolder;
  }

  public ProcessSolution getProcessSolution() {
    return processSolution;
  }

  public void setProcessSolution(ProcessSolution processSolution) {
    this.processSolution = processSolution;
  }

  public RepositoryNode getWrappedNode() {
    return wrappedNode;
  }

  public void setWrappedNode(RepositoryNode wrappedNode) {
    this.wrappedNode = wrappedNode;
  }

  public void setConnectorId(String connectorId) {
    this.connectorId = connectorId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

}
