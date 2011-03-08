package org.activiti.cycle.impl.processsolution.listener;

import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.cycle.Content;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.event.CycleEventListener;
import org.activiti.cycle.impl.components.CycleEmailDispatcher;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.connector.signavio.provider.ActivitiCompliantBpmn20Provider;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.cycle.impl.processsolution.ProcessSolutionUtils;
import org.activiti.cycle.impl.processsolution.event.ImplementationDoneEvent;
import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.cycle.service.CycleProcessSolutionService;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleServiceFactory;
import org.activiti.engine.identity.User;

/**
 * {@link CycleEventListener} for {@link ImplementationDoneEvent}s.
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class ImplementationDoneUpdateOperationalProcessModels implements CycleEventListener<ImplementationDoneEvent> {

  private CycleProcessSolutionService processSolutionService = CycleServiceFactory.getProcessSolutionService();
  private CycleRepositoryService repositoryService = CycleServiceFactory.getRepositoryService();

  public void onEvent(ImplementationDoneEvent event) {
    List<RepositoryArtifact> updatedOperationalModels = new ArrayList<RepositoryArtifact>();
    VirtualRepositoryFolder processesFolder = getProcessSolutionUtils().getProcessesFolder(event.getProcessSolution());
    VirtualRepositoryFolder implementationFolder = getProcessSolutionUtils().getImplementationFolder(event.getProcessSolution());
    List<RepositoryArtifact> processModels = getProcessSolutionUtils().getProcessModels(processesFolder);
    for (RepositoryArtifact operationalProcessModel : processModels) {
      List<RepositoryArtifactLink> links = repositoryService.getArtifactLinks(operationalProcessModel.getConnectorId(), operationalProcessModel.getNodeId());
      for (RepositoryArtifactLink repositoryArtifactLink : links) {
        if (!RepositoryArtifactLinkEntity.TYPE_IMPLEMENTS.equals(repositoryArtifactLink.getLinkType())) {
          continue;
        }
        RepositoryArtifact implementedProcessModel = repositoryArtifactLink.getTargetArtifact();
        // check whether the implemented process model is last changed after the
        // operational process model:
        RepositoryConnector operationalProcessModelConnector = RuntimeConnectorList.getMyConnectorById(operationalProcessModel.getConnectorId());
        if (!isEeSignavio(operationalProcessModelConnector)) {
          continue;
        }
        RepositoryConnector implementedProcessModelConnector = RuntimeConnectorList.getMyConnectorById(implementedProcessModel.getConnectorId());
        String operationalProcessModelBpmn = ActivitiCompliantBpmn20Provider.createBpmnXml(operationalProcessModelConnector, operationalProcessModel);
        Content implementedProcessModelBpmnContent = implementedProcessModelConnector.getContent(implementedProcessModel.getNodeId());
        String implementedProcessModelBpmn = implementedProcessModelBpmnContent.asString();
        if (implementedProcessModelBpmn.equals(operationalProcessModelBpmn)) {
          // no need to update
          continue;
        }
        // backup operational process model:
        RepositoryFolder backupFolder = null;
        RepositoryNodeCollection childnodes = operationalProcessModelConnector.getChildren(operationalProcessModel.getMetadata().getParentFolderId());
        for (RepositoryFolder potentialBackupFolder : childnodes.getFolderList()) {
          if ("backup".equals(potentialBackupFolder.getMetadata().getName())) {
            backupFolder = potentialBackupFolder;
            break;
          }
        }
        if (backupFolder == null) {
          backupFolder = operationalProcessModelConnector.createFolder(operationalProcessModel.getMetadata().getParentFolderId(), "backup");
        }
        Content content = operationalProcessModelConnector.getContent(operationalProcessModel.getNodeId());
        String backupModelName = operationalProcessModel.getMetadata().getName() + "_" + DateFormat.getDateTimeInstance().format(new Date());
        operationalProcessModelConnector.createArtifact(backupFolder.getNodeId(), backupModelName, null, content);
        // FIXME: limitation of the SignavioConnector: ATM we cannot update the
        // content of an artifact. This is why we delete the artifact and create
        // a new artifact instead.
        operationalProcessModelConnector.deleteArtifact(operationalProcessModel.getNodeId());
        // create the new model:
        SignavioConnectorInterface signavioConnectorInterface = (SignavioConnectorInterface) operationalProcessModelConnector;
        String json = signavioConnectorInterface.transformBpmn20XmltoJson(implementedProcessModelBpmn);
        Content jsoncontent = new Content();
        jsoncontent.setValue(json);
        String parentfolderId = operationalProcessModel.getMetadata().getParentFolderId();
        RepositoryFolder parentFolder = operationalProcessModelConnector.getRepositoryFolder(parentfolderId);
        RepositoryArtifact newArtifact = operationalProcessModelConnector.createArtifact(parentFolder.getNodeId(), operationalProcessModel.getMetadata()
                .getName(), null, jsoncontent);
        // delete the link to the deleted artifact
        repositoryService.deleteLink(repositoryArtifactLink.getId());
        // create new link
        RepositoryArtifactLinkEntity newLink = new RepositoryArtifactLinkEntity();
        newLink.setSourceArtifact(newArtifact);
        newLink.setTargetArtifact(implementedProcessModel);
        newLink.setLinkType(RepositoryArtifactLinkEntity.TYPE_IMPLEMENTS);
        repositoryService.addArtifactLink(newLink);
        updatedOperationalModels.add(newArtifact);

      }
    }
    if (updatedOperationalModels.size() > 0) {
      sendEmailnotification(event.getProcessSolution(), updatedOperationalModels);
    }
  }

  private boolean isEeSignavio(RepositoryConnector connector) {
    SignavioConnectorInterface connectorInterface = (SignavioConnectorInterface) connector;
    return !connectorInterface.getConfiguration().getSignavioUrl().endsWith("activiti-modeler/")
            && !connectorInterface.getConfiguration().getSignavioUrl().endsWith("activiti-modeler");
  }

  private void sendEmailnotification(ProcessSolution processSolution, List<RepositoryArtifact> updatedOperationalModels) {
    CycleEmailDispatcher cycleEmailDispatcher = CycleComponentFactory.getCycleComponentInstance(CycleEmailDispatcher.class);
    for (User user : processSolutionService.getProcessSolutionCollaborators(processSolution.getId(), null)) {
      StringWriter writer = new StringWriter();
      writer.append("Hi " + user.getFirstName() + " " + user.getLastName() + ", <br /><br />");
      writer.append("The following operational models for the process solution " + processSolution.getLabel() + " have been updated: <br />");
      writer.append("<ul>");
      for (RepositoryArtifact updatedModel : updatedOperationalModels) {
        writer.append("<li>");
        writer.append(updatedModel.getMetadata().getName());
        writer.append("</li>");
      }
      writer.append("</ul>");
      cycleEmailDispatcher.sendEmail("activiti-cycle@localhost", user.getEmail(), "Operational models updated", writer.toString());
    }
  }

  protected ProcessSolutionUtils getProcessSolutionUtils() {
    return CycleComponentFactory.getCycleComponentInstance(ProcessSolutionUtils.class);
  }
}
