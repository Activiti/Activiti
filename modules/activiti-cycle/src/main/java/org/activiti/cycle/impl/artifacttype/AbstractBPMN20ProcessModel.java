package org.activiti.cycle.impl.artifacttype;

import org.activiti.cycle.RepositoryArtifactType;

/**
 * Abstract {@link RepositoryArtifactType} for BPMN Process models
 * 
 * @author daniel.meyer@camunda.com
 */
public abstract class AbstractBPMN20ProcessModel extends AbstractProcessModel {

  public String getName() {
    return "BPMN 2.0 Process Model";
  }

}
