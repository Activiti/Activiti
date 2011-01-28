package org.activiti.cycle.impl.connector.signavio.provider;

import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.annotations.ListBeforeComponents;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;

/**
 * PNG-provider for {@link SignavioBpmn20ArtifactType}s.
 * 
 * @author meyerd
 */
@CycleComponent(context = CycleContextType.APPLICATION)
@ListBeforeComponents
public class BpmnProcessPngProvider extends AbstractPngProvider {

  public RepositoryArtifactType getRepositoryArtifactType() {
    return CycleApplicationContext.get(SignavioBpmn20ArtifactType.class);
  }

}
