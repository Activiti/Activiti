package org.activiti.cycle.impl.processsolution.connector;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.impl.processsolution.artifacttype.ProcessSolutionHomeArtifactType;
import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;

public class ProcessSolutionArtifact extends ProcessSolutionRepositoryNode implements RepositoryArtifact {

  public ProcessSolutionArtifact(String connectorId, String nodeId, VirtualRepositoryFolder virtualFolder, ProcessSolution processSolution,
          RepositoryArtifact wrappedArtifact) {
    super(connectorId, nodeId, virtualFolder, processSolution, wrappedArtifact);
  }

  public RepositoryArtifactType getArtifactType() {
    if (wrappedNode != null) {
      return ((RepositoryArtifact) wrappedNode).getArtifactType();
    } else if (getNodeId().endsWith(ProcessSolutionConnector.PS_HOME_NAME)) {
      return CycleComponentFactory.getCycleComponentInstance(ProcessSolutionHomeArtifactType.class, ProcessSolutionHomeArtifactType.class);
    }
    return null;
  }

  public RepositoryArtifact getWrappedNode() {
    return (RepositoryArtifact) super.getWrappedNode();
  }
}
