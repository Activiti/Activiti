package org.activiti.cycle.impl.processsolution;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.action.ArtifactAwareParameterizedAction;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.ParameterizedHtmlFormTemplateAction;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.cycle.impl.processsolution.connector.ProcessSolutionArtifact;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.cycle.service.CycleProcessSolutionService;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleServiceFactory;

/**
 * Action for linking Requirements with ProcessDiagrams
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class LinkRequirementWithProcessDiagramAction extends ParameterizedHtmlFormTemplateAction implements ArtifactAwareParameterizedAction,
        ProcessSolutionAction {

  public LinkRequirementWithProcessDiagramAction() {
    super("Link Requirement with Process Model");
  }

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {
    CycleRepositoryService repositoryService = CycleServiceFactory.getRepositoryService();

    String comment = (String) parameters.get("comment");
    String targetArtifactId = (String) parameters.get("targetArtifactId");
    RepositoryConnector targetConnector = (RepositoryConnector) parameters.get("targetConnectorId");
    RepositoryArtifact requirementsArtifact = artifact;
    RepositoryArtifact processModelArtifact = repositoryService.getRepositoryArtifact(targetConnector.getId(), targetArtifactId);

    RepositoryArtifactLink link = new RepositoryArtifactLinkEntity();
    link.setComment(comment);
    link.setSourceArtifact(requirementsArtifact);
    link.setTargetArtifact(processModelArtifact);
    link.setLinkType(RepositoryArtifactLinkEntity.TYPE_REQUIREMENT);
    repositoryService.addArtifactLink(link);

  }
  public Set<RepositoryArtifactType> getArtifactTypes() {
    // "null" means for all.
    return null;
  }

  public boolean isApplicable(RepositoryArtifact toArtifact) {
    if (toArtifact instanceof ProcessSolutionArtifact) {
      ProcessSolutionArtifact processSolutionArtifact = (ProcessSolutionArtifact) toArtifact;
      if (processSolutionArtifact.getVirtualRepositoryFolder() == null) {
        return false;
      }
      if ("Requirements".equals(processSolutionArtifact.getVirtualRepositoryFolder().getType())) {
        // set values for the 'currentProcessesFolder' in the request context
        CurrentProcessesFolder currentProcessesFolder = CycleComponentFactory.getCycleComponentInstance("currentProcessesFolder", CurrentProcessesFolder.class);

        CycleProcessSolutionService processService = CycleServiceFactory.getProcessSolutionService();
        List<VirtualRepositoryFolder> folders = processService.getFoldersForProcessSolution(processSolutionArtifact.getVirtualRepositoryFolder()
                .getProcessSolutionId());
        for (VirtualRepositoryFolder virtualRepositoryFolder : folders) {
          if (virtualRepositoryFolder.getType().equals("Processes")) {
            currentProcessesFolder.setFolderId(virtualRepositoryFolder.getProcessSolutionId() + "/" + virtualRepositoryFolder.getId());
            currentProcessesFolder.setConnectorId("ps-" + virtualRepositoryFolder.getProcessSolutionId());
          }
        }

        return true;
      }
    }
    return false;
  }
  public String getFormResourceName() {
    return getDefaultFormName();
  }

}
