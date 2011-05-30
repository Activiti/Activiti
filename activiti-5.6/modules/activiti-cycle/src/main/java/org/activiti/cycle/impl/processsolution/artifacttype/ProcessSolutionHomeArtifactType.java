package org.activiti.cycle.impl.processsolution.artifacttype;

import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.artifacttype.AbstractRepositoryArtifactType;
import org.activiti.cycle.processsolution.ProcessSolution;

/**
 * {@link RepositoryArtifactType} for {@link ProcessSolution}-Home folders
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class ProcessSolutionHomeArtifactType extends AbstractRepositoryArtifactType implements RepositoryArtifactType {

  public String getName() {
    return "Process Solution Home";
  }

}
