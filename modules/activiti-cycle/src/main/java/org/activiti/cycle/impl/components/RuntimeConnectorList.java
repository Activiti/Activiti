package org.activiti.cycle.impl.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.connector.view.TagConnector;
import org.activiti.cycle.impl.processsolution.connector.ProcessSolutionConnector;
import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.service.CycleServiceFactory;

@CycleComponent(context = CycleContextType.SESSION)
public class RuntimeConnectorList implements Serializable {

  private static final long serialVersionUID = 1L;
  // the transient field keeps the servlet container from serializing the
  // connectors in the session
  // TODO: needs testing: When do servlet containers serialize/deserialize?
  // Tomcat seems to do it between shutdowns / startups. At the moment I would
  // qualify this as a 'hack' - Daniel Meyer
  protected transient Map<String, RepositoryConnector> connectors;

  protected transient List<RepositoryConnector> connectorList;

  public RuntimeConnectorList() {
    init();
  }

  public synchronized RepositoryConnector getConnectorById(String id) {
    init();

    if (id.startsWith("ps-")) {
      return new ProcessSolutionConnector(id.substring(3));
    }

    return connectors.get(id);

  }

  public synchronized List<RepositoryConnector> getConnectors() {
    init();
    List<RepositoryConnector> resultList = new ArrayList<RepositoryConnector>(connectorList);
    // add virtual connectors for process solutions:
    for (ProcessSolution processSolution : CycleServiceFactory.getProcessSolutionService().getProcessSolutions()) {
      ProcessSolutionConnector psConnector = new ProcessSolutionConnector(processSolution.getId());
      resultList.add(psConnector);
    }
    return resultList;
  }

  protected synchronized void init() {
    if (connectors != null && connectorList != null) {
      return;
    }

    // load connectors
    connectors = new HashMap<String, RepositoryConnector>();

    RepositoryConnectorFactory factory = CycleApplicationContext.get(RepositoryConnectorFactory.class);
    connectorList = factory.getConnectors();
    for (RepositoryConnector connector : connectorList) {
      connectors.put(connector.getId(), connector);
    }

    // sort connector-list
    Collections.sort(connectorList, new Comparator<RepositoryConnector>() {

      public int compare(RepositoryConnector o1, RepositoryConnector o2) {
        String name1 = o1.getName();
        String name2 = o2.getName();
        return name1.compareTo(name2);
      }
    });

    // add tag connector hard coded for the moment (at the first node in the
    // tree)
    RepositoryConnector tagConnector = new TagConnector();
    connectors.put(tagConnector.getId(), tagConnector);
    connectorList.add(0, tagConnector);
  }

  public synchronized void discardConnectors() {
    connectors = null;
    connectorList = null;
  }

  public void registerConnector(RepositoryConnector connector) {
    connectors.put(connector.getId(), connector);
    connectorList.add(connector);
  }

  public static RepositoryConnector getMyConnectorById(String id) {
    return CycleComponentFactory.getCycleComponentInstance(RuntimeConnectorList.class, RuntimeConnectorList.class).getConnectorById(id);
  }
}
