package org.activiti.cycle.impl.processsolution;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;
import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.cycle.service.CycleProcessSolutionService;
import org.activiti.cycle.service.CycleServiceFactory;

/**
 * Utils for handling process solutions
 * 
 * TODO: expose some of these methods in the {@link CycleProcessSolutionService}
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.NONE)
public class ProcessSolutionUtils {

  private CycleProcessSolutionService processSolutionService = CycleServiceFactory.getProcessSolutionService();

  public List<RepositoryArtifact> getProcessModels(VirtualRepositoryFolder processes) {
    RepositoryConnector connector = RuntimeConnectorList.getMyConnectorById(processes.getConnectorId());
    List<RepositoryArtifact> resultList = new ArrayList<RepositoryArtifact>();
    String currentFolder = processes.getReferencedNodeId();
    getProcessModelsRec(connector, currentFolder, resultList);
    return resultList;
  }

  public VirtualRepositoryFolder getImplementationFolder(ProcessSolution processSolution) {
    // TODO: add dedicate query for this
    List<VirtualRepositoryFolder> virtualFolders = processSolutionService.getFoldersForProcessSolution(processSolution.getId());
    for (VirtualRepositoryFolder virtualRepositoryFolder : virtualFolders) {
      if ("Implementation".equals(virtualRepositoryFolder.getType())) {
        return virtualRepositoryFolder;
      }
    }
    return null;
  }

  public VirtualRepositoryFolder getProcessesFolder(ProcessSolution processSolution) {
    // TODO: add dedicate query for this
    List<VirtualRepositoryFolder> virtualFolders = processSolutionService.getFoldersForProcessSolution(processSolution.getId());
    for (VirtualRepositoryFolder virtualRepositoryFolder : virtualFolders) {
      if ("Processes".equals(virtualRepositoryFolder.getType())) {
        return virtualRepositoryFolder;
      }
    }
    return null;
  }

  private void getProcessModelsRec(RepositoryConnector connector, String currentFolder, List<RepositoryArtifact> resultList) {
    RepositoryNodeCollection childNodes = connector.getChildren(currentFolder);
    for (RepositoryArtifact repositoryArtifact : childNodes.getArtifactList()) {
      if (repositoryArtifact.getArtifactType().equals(CycleComponentFactory.getCycleComponentInstance(SignavioBpmn20ArtifactType.class))) {
        resultList.add(repositoryArtifact);
      }
    }
    for (RepositoryFolder folder : childNodes.getFolderList()) {
      getProcessModelsRec(connector, folder.getNodeId(), resultList);
    }
  }
}
