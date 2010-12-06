package org.activiti.cycle.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.connector.view.TagConnectorConfiguration;
import org.activiti.cycle.service.CycleServiceFactory;

@CycleComponent(context = CycleContextType.SESSION)
public class RuntimeConnectorList implements Serializable {

  private static final long serialVersionUID = 1L;
  // the transient field keeps the servlet container from serializing the
  // connectors in the session
  // TODO: needs testing: When do servlet containers serialize/deserialize?
  // Tomcat seems to do it
  // between shutdowns / startups. At the moment I would qualify this as a
  // 'hack' - Daniel Meyer
  private transient Map<String, RepositoryConnector> connectors;

  private transient List<RepositoryConnector> connectorList;

  public RuntimeConnectorList() {
    init();
  }

  public synchronized RepositoryConnector getConnectorById(String id) {
    init();
    return connectors.get(id);
  }

  public synchronized List<RepositoryConnector> getConnectors() {
    init();
    return connectorList;
  }

  private synchronized void init() {
    if (connectors != null && connectorList != null) {
      return;
    }

    // load connectors
    connectors = new HashMap<String, RepositoryConnector>();
    ConfigurationContainer container = CycleServiceFactory.getConfigurationService().getConfigurationContainer();
    for (RepositoryConnector connector : container.getConnectorList()) {
      connectors.put(connector.getConfiguration().getId(), connector);
    }

    // create sorted connector-list
    connectorList = new ArrayList<RepositoryConnector>(connectors.values());
    Collections.sort(connectorList, new Comparator<RepositoryConnector>() {

      public int compare(RepositoryConnector o1, RepositoryConnector o2) {
        String name1 = o1.getConfiguration().getName();
        String name2 = o2.getConfiguration().getName();
        return name1.compareTo(name2);

      }
    });

    // add tag connector hard coded for the moment (at the first node in the
    // tree)
    RepositoryConnector tagConnector = new TagConnectorConfiguration().createConnector();
    connectors.put(tagConnector.getConfiguration().getId(), tagConnector);
    connectorList.add(0, tagConnector);

  }

  public synchronized void discardConnectors() {
    connectors = null;
    connectorList = null;
  }

}
