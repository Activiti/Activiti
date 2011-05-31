package org.activiti.cycle.impl.connector.signavio.action;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
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
public class CreateTechnicalBpmnXmlAction extends AbstractTechnicalBpmnXmlAction {

  private static final long serialVersionUID = 1L;

  public static final String PARAM_TARGET_FOLDER = "targetFolderId";
  public static final String PARAM_TARGET_CONNECTOR = "targetConnectorId";
  public static final String PARAM_TARGET_NAME = "targetName";
  public static final String PARAM_COMMENT = "comment";
  public static final String CREATE_LINK_NAME = "createLink";

  public CreateTechnicalBpmnXmlAction() {
    // TODO: remove when real labels are introduced in the GUI
    this("Create technical BPMN model");
  }
  
  private Set<RepositoryArtifactType> types = new HashSet<RepositoryArtifactType>();
  
  public CreateTechnicalBpmnXmlAction(String name) {
    // TODO: remove when real labels are introduced in the GUI
    super(name);
    types.add(CycleApplicationContext.get(SignavioBpmn20ArtifactType.class));
  }

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {
    // TODO: Check with Nils that we get the object instead of the string in
    // here!
    String targetFolderId = getParameter(parameters, PARAM_TARGET_FOLDER, true, null, String.class);
    String targetName = getParameter(parameters, PARAM_TARGET_NAME, false, getProcessName(artifact), String.class);
    String comment = getParameter(parameters, PARAM_COMMENT, false, null, String.class);
    RepositoryConnector targetConnector = getParameter(parameters, PARAM_TARGET_CONNECTOR, true, null, RepositoryConnector.class);
    boolean createLink = getParameter(parameters, CREATE_LINK_NAME, true, Boolean.TRUE, Boolean.class);

    // no transaction required: atomic
    RepositoryArtifact targetArtifact = createArtifact(connector, artifact, targetFolderId, targetName, targetConnector);

    if (createLink) {
      RepositoryArtifactLink link = new RepositoryArtifactLinkEntity();
      link.setSourceArtifact(artifact);
      link.setTargetArtifact(targetArtifact);
      link.setComment(comment);
      link.setLinkType(getLinkType());
      CycleRepositoryService repositoryService = CycleServiceFactory.getRepositoryService();
      repositoryService.addArtifactLink(link);
    }
  }
  public String getLinkType() {
    return RepositoryArtifactLinkEntity.TYPE_IMPLEMENTS;
  }
  
  public Set<RepositoryArtifactType> getArtifactTypes() {
    return types;
  }

  public RepositoryArtifact createArtifact(RepositoryConnector sourceConnector, RepositoryArtifact sourceArtifact, String targetFolderId, String targetName,
          RepositoryConnector targetConnector) throws Exception {

    String targetArtifactId = targetName + ".bpmn20.xml";
    // String targetArtifactTypeId = FileSystemPluginDefinition.ARTIFACT_TYPE_BPMN_20_XML;
    // TODO : create FileSystemPluginDefinition.ARTIFACT_TYPE_BPMN_20_XML
    // equivalent Artifact type
    String targetArtifactTypeId = "";
    Content content = createContent(sourceConnector, sourceArtifact);

    return targetConnector.createArtifact(targetFolderId, targetArtifactId, targetArtifactTypeId, content);
  }

}
