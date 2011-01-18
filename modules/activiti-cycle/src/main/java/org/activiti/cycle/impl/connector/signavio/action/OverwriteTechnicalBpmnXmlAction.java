package org.activiti.cycle.impl.connector.signavio.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.action.ArtifactAwareParameterizedAction;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleServiceFactory;

/**
 * This action creates a technical BPMN 2.0 XML for the process engines. It
 * copies the XML from Signavio to a given {@link RepositoryFolder}.
 * 
 * By doing that, registered plugins / transformations are executed. The link
 * between the two {@link RepositoryArtifact}s is remembered (TODO).
 * 
 * @author bernd.ruecker@camunda.com
 */
@CycleComponent(context=CycleContextType.APPLICATION)
public class OverwriteTechnicalBpmnXmlAction extends AbstractTechnicalBpmnXmlAction implements ArtifactAwareParameterizedAction {

  private static final long serialVersionUID = 1L;

  public static final String PARAM_TARGET_FOLDER = "targetFolderId";
  public static final String PARAM_TARGET_CONNECTOR = "targetConnectorId";
  public static final String PARAM_TARGET_NAME = "targetName";
  public static final String PARAM_COMMENT = "comment";
  public static final String CREATE_LINK_NAME = "createLink";

  private Set<RepositoryArtifactType> types = new HashSet<RepositoryArtifactType>();

  public OverwriteTechnicalBpmnXmlAction() {
    // TODO: remove when real labels are introduced in the GUI
    this("Overwrite technical model(s)");
  }

  public OverwriteTechnicalBpmnXmlAction(String name) {
    // TODO: remove when real labels are introduced in the GUI
    super(name);
    types.add(CycleApplicationContext.get(SignavioBpmn20ArtifactType.class));
  }

  /**
   * TODO: dedicated query? What we do here might be too in-efficient if we have a huge amount
   * of links...
   */
  protected List<RepositoryArtifactLink> getImplementationLinks(RepositoryArtifact forArtifact) {

    CycleRepositoryService cycleRepositoryService = CycleServiceFactory.getRepositoryService();
    List<RepositoryArtifactLink> links = cycleRepositoryService.getArtifactLinks(forArtifact.getConnectorId(), forArtifact.getNodeId());
    List<RepositoryArtifactLink> implementationLinks = new ArrayList<RepositoryArtifactLink>();

    for (RepositoryArtifactLink repositoryArtifactLink : links) {
      if (!getLinkType().equals(repositoryArtifactLink.getLinkType())) {
        continue;
      }
      implementationLinks.add(repositoryArtifactLink);
    }

    return implementationLinks;

  }

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {

    List<RepositoryArtifactLink> implementations = getImplementationLinks(artifact);

    for (RepositoryArtifactLink repositoryArtifactLink : implementations) {
      RepositoryArtifact implementationArtifact = repositoryArtifactLink.getTargetArtifact();
      if (implementationArtifact == null) {
        throw new Exception("Cannot resolve artifact '" + repositoryArtifactLink.getTargetElementId() + "'. Corresponding connector not logged in?");
      }
      updateArtifact(connector, artifact, implementationArtifact.getNodeId(), implementationArtifact.getConnectorId());
    }

  }

  private void updateArtifact(RepositoryConnector sourceConnector, RepositoryArtifact sourceArtifact, String targetNodeId, String targetConnectorId) {
    CycleRepositoryService cycleRepositoryService = CycleServiceFactory.getRepositoryService();

    Content content = createContent(sourceConnector, sourceArtifact);

    cycleRepositoryService.updateContent(targetConnectorId, targetNodeId, content);
  }

  public boolean isApplicable(RepositoryArtifact toArtifact) {
    return getImplementationLinks(toArtifact).size() > 0;
  }

  public Set<RepositoryArtifactType> getArtifactTypes() {
    return types;
  }

}
