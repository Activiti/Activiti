package org.activiti.cycle.impl.connector.signavio.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.engine.impl.util.IoUtil;

public class CreateMavenProjectAction extends CreateTechnicalBpmnXmlAction {

  private static final long serialVersionUID = 1L;

  public static final String PARAM_TARGET_FOLDER = "targetFolderId";
  public static final String PARAM_TARGET_CONNECTOR = "targetConnectorId";
  public static final String PARAM_TARGET_NAME = "targetName";
  public static final String PARAM_COMMENT = "comment";
  public static final String CREATE_LINK_NAME = "createLink";

  private static final String REPLACE_STRING = "activiti.project.template";

  public CreateMavenProjectAction() {
    // TODO: remove when real labels are introduced in the GUI
    super("Create default maven project");
  }

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {
    String targetFolderId = (String) getParameter(parameters, PARAM_TARGET_FOLDER, true, null, String.class);
    String targetName = (String) getParameter(parameters, PARAM_TARGET_NAME, false, getProcessName(artifact), String.class);
    String comment = (String) getParameter(parameters, PARAM_COMMENT, false, null, String.class);
    RepositoryConnector targetConnector = (RepositoryConnector) getParameter(parameters, PARAM_TARGET_CONNECTOR, true, null, RepositoryConnector.class);
    boolean createLink = (Boolean) getParameter(parameters, CREATE_LINK_NAME, true, Boolean.TRUE, Boolean.class);

    RepositoryArtifact bpmnArtifact = createProject(targetConnector, targetFolderId, targetName, createBpmnXml(connector, artifact));

    // TODO: Think about that more, does it make sense like this?
    targetConnector.commitPendingChanges(comment);

    if (createLink && bpmnArtifact != null) {
      // TODO: We cannot link to a folder at the moment!
      // RepositoryFolder targetFolder =
      // targetConnector.getRepositoryFolder(targetFolderId);

      RepositoryArtifactLink link = new RepositoryArtifactLinkEntity();
      link.setSourceArtifact(artifact);
      link.setTargetArtifact(bpmnArtifact);
      link.setComment(comment);
      link.setLinkType(getLinkType());
      connector.getConfiguration().getCycleService().addArtifactLink(link);
    }
  }
  
  public String getProcessName(RepositoryArtifact artifact) {
    return artifact.getMetadata().getName();
  }

  @Override
  public String getFormResourceName() {
    return getFormNameForClass(CreateTechnicalBpmnXmlAction.class);
  }

  /**
   * create a project from the Maven template and return the RepositoryArtifact
   * representing the bpmn process model
   */
  public RepositoryArtifact createProject(RepositoryConnector connector, String rootFolderId, String projectName, String processDefinitionXml) {
    RepositoryArtifact result = null;
    try {
      ZipInputStream projectTemplateInputStream = new ZipInputStream(getProjectTemplate());
      ZipEntry zipEntry = null;
      
      String rootSubstitution = null;
      
      while ((zipEntry = projectTemplateInputStream.getNextEntry()) != null) {
        String zipName = zipEntry.getName();
        if (zipName.endsWith("/")) {
          zipName = zipName.substring(0, zipName.length() - 1);
        }
        String path = "";
        String name = zipName;
        if (zipName.contains("/")) {
          path = zipName.substring(0, zipName.lastIndexOf("/"));
          name = zipName.substring(zipName.lastIndexOf("/") + 1);
        }
        if ("".equals(path)) {
          // root folder is named after the project, not like the template
          // folder name
          rootSubstitution = name;
          name = projectName;
        }
        else {
          // rename the root folder in all other paths as well
          path = path.replace(rootSubstitution, projectName);
        }
        String absolutePath = rootFolderId + "/" + path;
        boolean isBpmnModel = false;
        if (zipEntry.isDirectory()) {
          connector.createFolder(absolutePath, name);
        } else {
          Content content = new Content();

          if ("template.bpmn20.xml".equals(name)) {
            // This file shall be replaced with the process definition
            content.setValue(processDefinitionXml);
            name = projectName + ".bpmn20.xml";
            isBpmnModel = true;
            log.log(Level.INFO, "Create processdefinition from Signavio process model " + projectName);            
          } else {
            byte[] bytes = IoUtil.readInputStream(projectTemplateInputStream, "ZIP entry '" + zipName + "'");
            String txtContent = new String(bytes).replaceAll(REPLACE_STRING, projectName);
            content.setValue(txtContent);
          }
          log.log(Level.INFO, "Create new artifact from zip entry '" + zipEntry.getName() + "' in folder '" + absolutePath + "' with name '" + name + "'");
          RepositoryArtifact artifact = connector.createArtifact(absolutePath, name, null, content);
          if (isBpmnModel) {
            result = artifact;
          }
        }
        projectTemplateInputStream.closeEntry();
      }
      projectTemplateInputStream.close();
    } catch (IOException ex) {
      throw new RepositoryException("Couldn't create maven project due to IO errors", ex);
    }
    return result;
  }

  protected InputStream getProjectTemplate() {
    return this.getClass().getResourceAsStream("activiti-cycle-maven-template.zip");
  }

}
