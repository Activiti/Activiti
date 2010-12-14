package org.activiti.cycle.impl.connector.signavio.action;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.ParameterizedHtmlFormTemplateAction;
import org.activiti.cycle.impl.connector.signavio.provider.ActivitiCompliantBpmn20Provider;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;

/**
 * Abstract base action for creating technical bpmn models.
 */
public abstract class AbstractTechnicalBpmnXmlAction extends ParameterizedHtmlFormTemplateAction {

  private static final long serialVersionUID = 1L;

  public AbstractTechnicalBpmnXmlAction(String name) {
    super(name);
  }

  public String getLinkType() {
    return RepositoryArtifactLinkEntity.TYPE_IMPLEMENTS;
  }

  public Content createContent(RepositoryConnector connector, RepositoryArtifact artifact) {
    String bpmnXml = ActivitiCompliantBpmn20Provider.createBpmnXml(connector, artifact);
    Content content = new Content();
    content.setValue(bpmnXml);
    return content;
  }

  public String getProcessName(RepositoryArtifact artifact) {
    return artifact.getMetadata().getName();
  }

  @Override
  public String getFormResourceName() {
    return getDefaultFormName();
  }
  
  

}
