package org.activiti.cycle.impl.processsolution;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.db.entity.VirtualRepositoryFolderEntity;
import org.activiti.cycle.processsolution.ProcessSolutionState;
import org.activiti.cycle.processsolution.ProcessSolutionTemplate;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.cycle.service.CycleConfigurationService;
import org.activiti.cycle.service.CycleServiceFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Application-scoped {@link CycleComponent} representing the default
 * {@link ProcessSolutionTemplate}
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class DefaultProcessSolutionTemplate implements ProcessSolutionTemplate {

  private CycleConfigurationService configurationService = CycleServiceFactory.getConfigurationService();

  public List<VirtualRepositoryFolder> getVirtualRepositoryFolders() {
    try {
      return getVirtualRepositoryFoldersFromConfiguration();
    } catch (Exception e) {
      throw new RuntimeException("Default ProcessSolutionTemplate coult not be loaded: " + e.getMessage(), e);
    }
  }

  private List<VirtualRepositoryFolder> getVirtualRepositoryFoldersFromConfiguration() throws Exception {
    // load template using the configurationService
    String configurationString = configurationService.getConfigurationValue("processSolutionTemplates", "default");
    if (configurationString == null) {
      throw new RuntimeException("No ProcessSolutionTemplate found using group='processSolutionTemplates' and key='default'");
    }

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();

    Document configurationDocument = db.parse(new ByteArrayInputStream(configurationString.getBytes()));

    List<VirtualRepositoryFolder> resultList = new ArrayList<VirtualRepositoryFolder>();

    NodeList vFolderElementList = configurationDocument.getElementsByTagName("vFolder");
    for (int i = 0; i < vFolderElementList.getLength(); i++) {
      Node node = vFolderElementList.item(i);
      if (!(node instanceof Element)) {
        continue;
      }
      Element element = (Element) node;
      String type = element.getAttribute("type");
      String name = element.getAttribute("name");
      String connectorId = element.getAttribute("connectorId");
      String referencedNodeId = element.getAttribute("referencedNodeId");
      VirtualRepositoryFolderEntity virtualRepositoryFolderEntity = new VirtualRepositoryFolderEntity();
      virtualRepositoryFolderEntity.setType(type);
      virtualRepositoryFolderEntity.setLabel(name);
      virtualRepositoryFolderEntity.setConnectorId(connectorId);
      virtualRepositoryFolderEntity.setReferencedNodeId(referencedNodeId);
      resultList.add(virtualRepositoryFolderEntity);
    }

    return resultList;

  }

  public ProcessSolutionState getInitialState() {
    return ProcessSolutionState.IN_SPECIFICATION;
  }

}
