package org.activiti.cycle.impl.connector.signavio.action;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.impl.ParameterizedHtmlFormTemplateAction;
import org.activiti.cycle.impl.connector.signavio.provider.SignavioDiffProvider;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;

// un-comment to enable
//@CycleComponent(context=CycleContextType.APPLICATION)
public class SelectDiffTargetAction extends ParameterizedHtmlFormTemplateAction {

  private static final long serialVersionUID = 1L;

  public static final String PARAM_TARGET_ARTIFACT = "targetArtifactId";
  public static final String PARAM_TARGET_CONNECTOR = "targetConnectorId";

  private Set<RepositoryArtifactType> types = new HashSet<RepositoryArtifactType>();

  public SelectDiffTargetAction() {
    // TODO: remove when real labels are introduced in the GUI
    super("Select target for DIFF");
    types.add(CycleApplicationContext.get(SignavioBpmn20ArtifactType.class));
  }

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {
    // TODO: Check with Nils that we get the object instead of the string in
    // here!
    String targetNodeId = getParameter(parameters, PARAM_TARGET_ARTIFACT, true, null, String.class);
    RepositoryConnector targetConnector = getParameter(parameters, PARAM_TARGET_CONNECTOR, true, null, RepositoryConnector.class);
    RepositoryArtifact targetArtifact = targetConnector.getRepositoryArtifact(targetNodeId);

    // yeah, that is pretty hacky! Especially with multiple users. But as a
    // quick POC that was the easiest way!
    SignavioDiffProvider.targetArtifact = targetArtifact;
  }

  @Override
  public String getFormResourceName() {
    return getDefaultFormName();
  }

  public Set<RepositoryArtifactType> getArtifactTypes() {
    return types;
  }

}
