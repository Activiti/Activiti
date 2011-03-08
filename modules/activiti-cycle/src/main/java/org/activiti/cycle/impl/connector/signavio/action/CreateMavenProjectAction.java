package org.activiti.cycle.impl.connector.signavio.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.activiti.cycle.Content;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.connector.signavio.provider.ActivitiCompliantBpmn20Provider;
import org.activiti.cycle.impl.connector.util.TransactionalConnectorUtils;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleServiceFactory;
import org.activiti.engine.impl.util.IoUtil;

@CycleComponent(context = CycleContextType.APPLICATION)
public class CreateMavenProjectAction extends CreateTechnicalBpmnXmlAction {

  private static final long serialVersionUID = 1L;

  public static final String PARAM_TARGET_FOLDER = "targetFolderId";
  public static final String PARAM_TARGET_CONNECTOR = "targetConnectorId";
  public static final String PARAM_TARGET_NAME = "targetName";
  public static final String PARAM_COMMENT = "comment";
  public static final String CREATE_LINK_NAME = "createLink";

  private static final String REPLACE_STRING = "activiti.project.template";

  public static String ACTIVITI_HOME_PATH;

  private static Logger log = Logger.getLogger(CreateMavenProjectAction.class.getName());

  public CreateMavenProjectAction() {
    // TODO: remove when real labels are introduced in the GUI
    super("Create default maven project");
    try {
      ACTIVITI_HOME_PATH = new File("../../../").getCanonicalPath();
    } catch (IOException e) {
      ACTIVITI_HOME_PATH = new File(".").getAbsolutePath();
      log.log(Level.WARNING, "Couldn't set Activiti Home path (see nested exception for the cause), using '" + ACTIVITI_HOME_PATH + "' instead.", e);
    }
    // change backslashed on windows to forward slashes for ant
    ACTIVITI_HOME_PATH = ACTIVITI_HOME_PATH.replace("\\", "/");
  }

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {
    String targetFolderId = (String) getParameter(parameters, PARAM_TARGET_FOLDER, true, null, String.class);
    String targetName = (String) getParameter(parameters, PARAM_TARGET_NAME, false, getProcessName(artifact), String.class);
    String comment = (String) getParameter(parameters, PARAM_COMMENT, false, null, String.class);
    RepositoryConnector targetConnector = (RepositoryConnector) getParameter(parameters, PARAM_TARGET_CONNECTOR, true, null, RepositoryConnector.class);
    boolean createLink = (Boolean) getParameter(parameters, CREATE_LINK_NAME, true, Boolean.TRUE, Boolean.class);

    List<RepositoryArtifact> processes = new ArrayList<RepositoryArtifact>();
    processes.add(artifact);
    RepositoryFolder targetFolder = targetConnector.createFolder(targetFolderId, targetName);
    createMavenProject(targetFolder.getNodeId(), targetName, comment, targetConnector, createLink, processes);

  }
  public Map<RepositoryArtifact, RepositoryArtifact> createMavenProject(String targetFolderId, String targetName, String comment,
          RepositoryConnector targetConnector, boolean createLink, List<RepositoryArtifact> processes) {
    Map<RepositoryArtifact, RepositoryArtifact> bpmnArtifacts = null;
    TransactionalConnectorUtils.beginTransaction(targetConnector);
    try {
      // create maven project
      bpmnArtifacts = createProject(targetConnector, targetFolderId, targetName, processes);
      TransactionalConnectorUtils.commitTransaction(targetConnector, comment);
    } catch (RepositoryException e) {
      TransactionalConnectorUtils.rollbackTransaction(targetConnector);
      throw e;
    }

    if (createLink && bpmnArtifacts != null) {
      // TODO: We cannot link to a folder at the moment!
      // RepositoryFolder targetFolder =
      // targetConnector.getRepositoryFolder(targetFolderId);
      for (Entry<RepositoryArtifact, RepositoryArtifact> processMappedToXml : bpmnArtifacts.entrySet()) {
        RepositoryArtifactLink link = new RepositoryArtifactLinkEntity();
        link.setSourceArtifact(processMappedToXml.getKey());
        link.setTargetArtifact(processMappedToXml.getValue());
        link.setComment(comment);
        link.setLinkType(getLinkType());
        CycleRepositoryService repositoryService = CycleServiceFactory.getRepositoryService();
        repositoryService.addArtifactLink(link);
      }
    }
    return bpmnArtifacts;
  }
  public String getProcessName(RepositoryArtifact artifact) {
    return artifact.getMetadata().getName();
  }

  /**
   * create a project from the Maven template and return the RepositoryArtifact
   * representing the bpmn process model
   */
  public Map<RepositoryArtifact, RepositoryArtifact> createProject(RepositoryConnector connector, String rootFolderId, String projectName,
          List<RepositoryArtifact> processes) {
    Map<RepositoryArtifact, RepositoryArtifact> resultList = null;
    try {
      ZipInputStream projectTemplateInputStream = new ZipInputStream(getProjectTemplate());
      ZipEntry zipEntry = null;

      while ((zipEntry = projectTemplateInputStream.getNextEntry()) != null) {
        String zipName = zipEntry.getName();
        zipName = zipName.replaceAll("activiti-cycle-maven-template/", "");
        if (zipName.endsWith("/")) {
          zipName = zipName.substring(0, zipName.length() - 1);
        }
        String path = "";
        String name = zipName;
        if (zipName.contains("/")) {
          path = zipName.substring(0, zipName.lastIndexOf("/"));
          name = zipName.substring(zipName.lastIndexOf("/") + 1);
        }
        String absolutePath = rootFolderId + "/" + path;
        if (zipEntry.isDirectory()) {
          if (!zipEntry.getName().equals("activiti-cycle-maven-template/")) {
            connector.createFolder(absolutePath, name);
          }
        } else {
          if ("template.bpmn20.xml".equals(name)) {
            resultList = generateProcesses(processes, absolutePath, connector);
          } else {
            Content content = new Content();
            byte[] bytes = IoUtil.readInputStream(projectTemplateInputStream, "ZIP entry '" + zipName + "'");
            String txtContent = new String(bytes).replaceAll(REPLACE_STRING, projectName).replaceAll("@@ACTIVITI.HOME@@", ACTIVITI_HOME_PATH);
            content.setValue(txtContent);
            log.log(Level.INFO, "Create new artifact from zip entry '" + zipEntry.getName() + "' in folder '" + absolutePath + "' with name '" + name + "'");
            connector.createArtifact(absolutePath, name, null, content);
          }
        }
        projectTemplateInputStream.closeEntry();
      }
      projectTemplateInputStream.close();
    } catch (IOException ex) {
      throw new RepositoryException("Couldn't create maven project due to IO errors", ex);
    }
    return resultList;
  }

  /**
   * generates bpmn20.xml artifacts for the provided process models
   */
  protected Map<RepositoryArtifact, RepositoryArtifact> generateProcesses(List<RepositoryArtifact> processes, String path, RepositoryConnector targetConnector) {
    Map<RepositoryArtifact, RepositoryArtifact> resultList = new HashMap<RepositoryArtifact, RepositoryArtifact>();
    RuntimeConnectorList runtimeConnectorList = CycleComponentFactory.getCycleComponentInstance(RuntimeConnectorList.class, RuntimeConnectorList.class);
    for (RepositoryArtifact process : processes) {
      log.log(Level.INFO, "Create processdefinition from Signavio process model " + process.getMetadata().getName());
      String name = getProcessName(process) + ".bpmn20.xml";
      Content content = new Content();
      RepositoryConnector connector = runtimeConnectorList.getConnectorById(process.getConnectorId());
      content.setValue(ActivitiCompliantBpmn20Provider.createBpmnXml(connector, process));
      resultList.put(process, targetConnector.createArtifact(path, name, null, content));
    }
    return resultList;

  }
  protected InputStream getProjectTemplate() {
    return this.getClass().getResourceAsStream("activiti-cycle-maven-template.zip");
  }

}
