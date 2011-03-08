package org.activiti.cycle.impl.processsolution.listener;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.cycle.Content;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.event.CycleCompensatingEventListener;
import org.activiti.cycle.impl.components.CycleEmailDispatcher;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.connector.signavio.action.CreateMavenProjectAction;
import org.activiti.cycle.impl.connector.signavio.provider.ActivitiCompliantBpmn20Provider;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.cycle.impl.processsolution.ProcessSolutionUtils;
import org.activiti.cycle.impl.processsolution.event.SpecificationDoneEvent;
import org.activiti.cycle.impl.processsolution.event.TechnicalProjectCreatedEvent;
import org.activiti.cycle.impl.processsolution.event.TechnicalProjectUpdatedEvent;
import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.cycle.service.CycleEventService;
import org.activiti.cycle.service.CycleProcessSolutionService;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleServiceFactory;
import org.activiti.engine.identity.User;

/**
 * {@link CycleCompensatingEventListener} generating a technical project-stub
 * (in the first iteration) and updating an existing technical project in
 * subsequent iterations.
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class SpecificationDoneGenerateProjectListener implements CycleCompensatingEventListener<SpecificationDoneEvent> {

  private CycleProcessSolutionService processSolutionService = CycleServiceFactory.getProcessSolutionService();
  private CycleRepositoryService repositoryservice = CycleServiceFactory.getRepositoryService();
  private CycleEventService eventService = CycleServiceFactory.getEventService();
  private ProcessSolutionUtils processSolutionUtils = CycleComponentFactory.getCycleComponentInstance(ProcessSolutionUtils.class);

  public void onEvent(SpecificationDoneEvent event) {
    ProcessSolution processSolution = event.getProcessSolution();
    VirtualRepositoryFolder implementationFolder = processSolutionUtils.getImplementationFolder(processSolution);
    if (implementationFolder == null) {
      return;
    }
    RepositoryFolder underlyingFolder = repositoryservice
            .getRepositoryFolder(implementationFolder.getConnectorId(), implementationFolder.getReferencedNodeId());

    if (repositoryservice.getChildren(underlyingFolder.getConnectorId(), underlyingFolder.getNodeId()).asList().size() > 0) {
      Map<RepositoryArtifact, RepositoryArtifact> processesMappedToBpmnXml = updateProject(processSolution, underlyingFolder);
      sendEmailUpdated(processSolution, processesMappedToBpmnXml);
      // fire an event signifying that a new technical project has been
      // updated:
      eventService.fireEvent(new TechnicalProjectUpdatedEvent(processSolution, underlyingFolder));
    } else {
      Map<RepositoryArtifact, RepositoryArtifact> processesMappedToBpmnXml = createProject(processSolution, underlyingFolder);
      sendEmailCreated(processSolution, processesMappedToBpmnXml);
      // fire an event signifying that a new technical project has been
      // generated:
      eventService.fireEvent(new TechnicalProjectCreatedEvent(processSolution, underlyingFolder));
    }

  }

  protected Map<RepositoryArtifact, RepositoryArtifact> updateProject(ProcessSolution processSolution, RepositoryFolder underlyingTechnicalFolder) {
    VirtualRepositoryFolder processes = processSolutionUtils.getProcessesFolder(processSolution);
    // get all processmodels in the processes folder
    List<RepositoryArtifact> processModels = processSolutionUtils.getProcessModels(processes);
    return updateProcessModels(underlyingTechnicalFolder, processModels);
  }

  protected Map<RepositoryArtifact, RepositoryArtifact> updateProcessModels(RepositoryFolder underlyingTechnicalFolder, List<RepositoryArtifact> processModels) {
    List<RepositoryArtifact> newModels = new ArrayList<RepositoryArtifact>();
    Map<RepositoryArtifact, RepositoryArtifact> resultMap = new HashMap<RepositoryArtifact, RepositoryArtifact>();
    // update existing models:
    for (RepositoryArtifact processModel : processModels) {
      List<RepositoryArtifactLink> links = repositoryservice.getArtifactLinks(processModel.getConnectorId(), processModel.getNodeId());
      if (links.size() == 0) {
        newModels.add(processModel);
      }
      for (RepositoryArtifactLink link : links) {
        if (!RepositoryArtifactLinkEntity.TYPE_IMPLEMENTS.equals(link.getLinkType())) {
          continue;
        }
        RepositoryArtifact implementationArtifact = link.getTargetArtifact();
        if (!implementationArtifact.getConnectorId().equals(underlyingTechnicalFolder.getConnectorId())) {
          continue;
        }
        if (!implementationArtifact.getNodeId().startsWith(underlyingTechnicalFolder.getNodeId())) {
          continue;
        }
        // only update technical model if the ProcessModel is last changed after
        // the implementation
        Date processModelUpdated = processModel.getMetadata().getLastChanged();
        Date implementationUpdated = implementationArtifact.getMetadata().getLastChanged();
        if (processModelUpdated == null || implementationUpdated == null || processModelUpdated.after(implementationUpdated)) {
          RepositoryConnector processModelConnector = RuntimeConnectorList.getMyConnectorById(processModel.getConnectorId());
          Content newContent = new Content();
          newContent.setValue(ActivitiCompliantBpmn20Provider.createBpmnXml(processModelConnector, processModel));
          repositoryservice.updateContent(implementationArtifact.getConnectorId(), implementationArtifact.getNodeId(), newContent);
          resultMap.put(processModel, implementationArtifact);
        }
      }
    }
    // add new models:
    RepositoryConnector targetConnector = RuntimeConnectorList.getMyConnectorById(underlyingTechnicalFolder.getConnectorId());
    // get folder for processModels:
    // TODO: hard-coding this ATM:
    RepositoryFolder processesFolder = targetConnector.getRepositoryFolder(underlyingTechnicalFolder.getNodeId() + "/src/main/resources/diagrams");
    for (RepositoryArtifact processModel : newModels) {
      RepositoryConnector processModelConnector = RuntimeConnectorList.getMyConnectorById(processModel.getConnectorId());
      // do not create artifact if this model is contained in a "backup" folder:
      RepositoryFolder parentFolder = processModelConnector.getRepositoryFolder(processModel.getMetadata().getParentFolderId());
      if ("backup".equals(parentFolder.getMetadata().getName())) {
        continue;
      }
      Content newContent = new Content();
      newContent.setValue(ActivitiCompliantBpmn20Provider.createBpmnXml(processModelConnector, processModel));
      String artifactName = processModel.getMetadata().getName() + ".bpmn20.xml";
      RepositoryArtifact implementationArtifact = repositoryservice.createArtifact(processesFolder.getConnectorId(), processesFolder.getNodeId(), artifactName,
              null, newContent);
      // create link:
      RepositoryArtifactLinkEntity link = new RepositoryArtifactLinkEntity();
      link.setLinkType(RepositoryArtifactLinkEntity.TYPE_IMPLEMENTS);
      link.setSourceArtifact(processModel);
      link.setTargetArtifact(implementationArtifact);
      repositoryservice.addArtifactLink(link);
      resultMap.put(processModel, implementationArtifact);
    }
    return resultMap;
  }
  protected Map<RepositoryArtifact, RepositoryArtifact> createProject(ProcessSolution processSolution, RepositoryFolder underlyingTechnicalFolder) {
    VirtualRepositoryFolder processes = processSolutionUtils.getProcessesFolder(processSolution);
    // get all processmodels in the processes folder
    List<RepositoryArtifact> processModels = processSolutionUtils.getProcessModels(processes);
    // configure parameters for CreateMavenProjectAction
    CreateMavenProjectAction createMavenProjectAction = new CreateMavenProjectAction();
    RepositoryConnector targetConnector = RuntimeConnectorList.getMyConnectorById(underlyingTechnicalFolder.getConnectorId());
    String targetFolderId = underlyingTechnicalFolder.getNodeId();
    String targetName = processSolution.getLabel();
    String comment = "";
    boolean createLink = true;
    // create the technical project
    return createMavenProjectAction.createMavenProject(targetFolderId, targetName, comment, targetConnector, createLink, processModels);
  }

  protected void sendEmailCreated(ProcessSolution processSolution, Map<RepositoryArtifact, RepositoryArtifact> processesMappedToBpmnXml) {
    CycleEmailDispatcher cycleEmailDispatcher = CycleComponentFactory.getCycleComponentInstance(CycleEmailDispatcher.class, CycleEmailDispatcher.class);
    for (User user : processSolutionService.getProcessSolutionCollaborators(processSolution.getId(), null)) {
      StringWriter writer = new StringWriter();
      writer.append("Hi " + user.getFirstName() + " " + user.getLastName() + ", <br /><br />");
      writer.append("Technical implementation models for the process solution " + processSolution.getLabel() + " have been created: <br />");
      writer.append("<ul>");
      for (Entry<RepositoryArtifact, RepositoryArtifact> processMappedToBpmnXml : processesMappedToBpmnXml.entrySet()) {
        writer.append("<li>");
        writer.append("The bpmn20.xml file for the process ");
        writer.append(processMappedToBpmnXml.getKey().getMetadata().getName());
        writer.append(" is located in ");
        writer.append(processMappedToBpmnXml.getValue().getNodeId() + ".");
        writer.append("</li>");
      }
      writer.append("</ul>");
      cycleEmailDispatcher.sendEmail("activiti-cycle@localhost", user.getEmail(), "Technical Model created", writer.toString());
    }
  }

  protected void sendEmailUpdated(ProcessSolution processSolution, Map<RepositoryArtifact, RepositoryArtifact> processesMappedToBpmnXml) {
    CycleEmailDispatcher cycleEmailDispatcher = CycleComponentFactory.getCycleComponentInstance(CycleEmailDispatcher.class, CycleEmailDispatcher.class);
    for (User user : processSolutionService.getProcessSolutionCollaborators(processSolution.getId(), null)) {
      StringWriter writer = new StringWriter();
      writer.append("Hi " + user.getFirstName() + " " + user.getLastName() + ", <br /><br />");
      writer.append("Technical implementation models for the process solution " + processSolution.getLabel() + " have been updated/created: <br />");
      writer.append("<ul>");
      for (Entry<RepositoryArtifact, RepositoryArtifact> processMappedToBpmnXml : processesMappedToBpmnXml.entrySet()) {
        writer.append("<li>");
        writer.append("The bpmn20.xml file for the process ");
        writer.append(processMappedToBpmnXml.getKey().getMetadata().getName());
        writer.append(" is located in ");
        writer.append(processMappedToBpmnXml.getValue().getNodeId() + ".");
        writer.append("</li>");
      }
      writer.append("</ul>");
      cycleEmailDispatcher.sendEmail("activiti-cycle@localhost", user.getEmail(), "Technical Models updated/created", writer.toString());
    }
  }

  public void compensateEvent(SpecificationDoneEvent event) {
    // TODO: delete technical implementation project in first iteration
  }

}
