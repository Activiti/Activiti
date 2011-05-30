package org.activiti.cycle.impl.connector.signavio.action;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;

/**
 * Extracts java Classes for a provided processDefinition-XML
 * 
 * @author daniel.meyer@camunda.com
 */
public interface ServiceStubGenerator {

  /**
   * Set the xml of the process definition we want to extract java classes from.
   * 
   * @param processDefinitionXml
   *          a string representing an Xml-File
   * @throws Exception
   *           if the provided XML cannot be parsed. TODO: throw a meaningful
   *           Exception
   */
  public void setProcessDefinitionXml(String processDefinitionXml) throws Exception;

  /**
   * Returns a map of interface to classNames mappings.
   * 
   * @throws IllegalStateException
   *           if no processDefinitionXml is set (
   *           {@link #setProcessDefinitionXml(String)})
   */
  public Map<String, Set<String>> getInterfaceImplementationMap();

  /**
   * Returns a map of classNames->processNodeIds.
   * 
   * @throws IllegalStateException
   *           if no processDefinitionXml is set (
   *           {@link #setProcessDefinitionXml(String)})
   */
  public Map<String, List<String>> getElementIdMap();

  /**
   * Returns the element name for a Signavio Id.
   */
  public abstract String getSignavioElementNameForId(String signavioElemetId);

  /**
   * creates service stubs for the provided interface (if they do not exist
   * already).
   */
  public Map<String, RepositoryArtifact> generateServiceStubs(String implementedInterface, Set<String> serviceAdapterClassNames, RepositoryConnector connector,
          String parentFolderId, String currentPackageName, String processName);

  public abstract Set<RepositoryArtifact> getSkippedArtifacts();

}
