package org.activiti.cycle.impl.connector.signavio.provider;

import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioDefaultArtifactType;

/**
 * PNG-Provider for {@link SignavioDefaultArtifactType}.
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class DefaultPngProvider extends AbstractPngProvider {

  public RepositoryArtifactType getRepositoryArtifactType() {
    return CycleApplicationContext.get(SignavioDefaultArtifactType.class);
  }

}
