package org.activiti.cycle.impl.processsolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.connector.util.TransactionalConnectorUtils;
import org.activiti.cycle.impl.db.entity.ProcessSolutionEntity;
import org.activiti.cycle.impl.db.entity.VirtualRepositoryFolderEntity;
import org.activiti.cycle.impl.service.CycleProcessSolutionServiceImpl;
import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.ProcessSolutionTemplate;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.cycle.service.CycleServiceFactory;

/**
 * Cycle Component for creating new {@link ProcessSolution}s
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(name = "processSolutionCreate", context = CycleContextType.REQUEST)
public class ProcessSolutionCreate {

  private CycleProcessSolutionServiceImpl processSolutionService = (CycleProcessSolutionServiceImpl) CycleServiceFactory.getProcessSolutionService();
  private RuntimeConnectorList connectorList = CycleComponentFactory.getCycleComponentInstance(RuntimeConnectorList.class, RuntimeConnectorList.class);
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * returns a name for the {@link ProcessSolution} to be used as folder name in
   * connectors.
   */
  protected String getNameForFs() {
    String name = "";
    for (char c : this.name.toCharArray()) {
      if (Character.isLetter(c) || Character.isDigit(c)) { // only use letters
                                                           // and digits
        name += c;
      }
    }
    return name;
  }

  public String createNewProcessSolution() {
    ProcessSolutionTemplate template = processSolutionService.getDefaultProcessSolutionTemplate();

    ProcessSolutionEntity processSolutionEntity = createProcessSolutionEntity(template);

    List<VirtualRepositoryFolderEntity> createdVirtualFolders = new ArrayList<VirtualRepositoryFolderEntity>();

    // get participating connectors
    Set<RepositoryConnector> participatingConnectors = getParticipatingConnectors(template);
    // begin transactions:
    for (RepositoryConnector participatingConnector : participatingConnectors) {
      TransactionalConnectorUtils.beginTransaction(participatingConnector);
    }

    try {
      // create folders in repositories
      for (VirtualRepositoryFolder folderTemplate : template.getVirtualRepositoryFolders()) {
        createdVirtualFolders.add(createVirtualFolder(folderTemplate, processSolutionEntity));
      }

      processSolutionService.getDao().addVirtualFoldersToSolution(processSolutionEntity.getId(), createdVirtualFolders);

      // commit repository transactions:
      for (RepositoryConnector participatingConnector : participatingConnectors) {
        TransactionalConnectorUtils.commitTransaction(participatingConnector, "created new process solution " + name);
      }

      return processSolutionEntity.getId();

    } catch (Exception e) {

      // rollback repository transactions:
      for (RepositoryConnector participatingConnector : participatingConnectors) {
        TransactionalConnectorUtils.rollbackTransaction(participatingConnector);
      }

      // delete virtual folders:
      for (VirtualRepositoryFolder virtualFolder : createdVirtualFolders) {
        processSolutionService.getDao().deleteVirtualRepositoryFolderById(virtualFolder.getId());
      }

      // delete process solution:
      processSolutionService.getDao().deleteProcessSolutionById(processSolutionEntity.getId());

      throw new RuntimeException("Could not create ProcessSolution " + e.getMessage(), e);
    }
  }

  private ProcessSolutionEntity createProcessSolutionEntity(ProcessSolutionTemplate template) {
    ProcessSolutionEntity processSolutionEntity = new ProcessSolutionEntity();
    processSolutionEntity.setLabel(name);
    processSolutionEntity.setState(template.getInitialState());
    return processSolutionService.getDao().saveProcessSolution(processSolutionEntity);
  }

  private VirtualRepositoryFolderEntity createVirtualFolder(VirtualRepositoryFolder folderTemplate, ProcessSolutionEntity processSolutionEntity) {
    String nodeId = folderTemplate.getReferencedNodeId();
    String connectorId = folderTemplate.getConnectorId();
    RepositoryConnector connector = connectorList.getConnectorById(connectorId);
    RepositoryFolder parentFolder = connector.getRepositoryFolder(nodeId);

    // create or get folder for this processSolution using connector
    RepositoryNodeCollection childNodeCollection = connector.getChildren(parentFolder.getNodeId());
    RepositoryFolder folderForThisProcessSolution = null;
    for (RepositoryFolder folder : childNodeCollection.getFolderList()) {
      if (folder.getMetadata().getName().equals(getNameForFs())) {
        // found folder for this processSolution
        folderForThisProcessSolution = folder;
      }
    }
    if (folderForThisProcessSolution == null) {
      // create new folder for this processSolution
      folderForThisProcessSolution = connector.createFolder(parentFolder.getNodeId(), getNameForFs());
    }

    // create new folder under folder for this ProcessSolution:
    RepositoryFolder actualFolder = connector.createFolder(folderForThisProcessSolution.getNodeId(), folderTemplate.getLabel());

    // create entity
    VirtualRepositoryFolderEntity entity = new VirtualRepositoryFolderEntity();
    entity.setLabel(folderTemplate.getLabel());
    entity.setConnectorId(connectorId);
    entity.setProcessSolutionId(processSolutionEntity.getId());
    entity.setReferencedNodeId(actualFolder.getNodeId());
    entity.setType(folderTemplate.getType());
    return entity;
  }

  private Set<RepositoryConnector> getParticipatingConnectors(ProcessSolutionTemplate template) {
    Set<RepositoryConnector> resultList = new HashSet<RepositoryConnector>();
    for (VirtualRepositoryFolder virtualFolder : template.getVirtualRepositoryFolders()) {
      resultList.add(connectorList.getConnectorById(virtualFolder.getConnectorId()));
    }
    return resultList;
  }
}
