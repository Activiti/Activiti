package org.activiti.cycle.impl.service;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.service.CycleConfigurationService;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleService;
import org.activiti.cycle.service.CycleTagService;

/**
 * Connector to represent customized view for a user of cycle to hide all the
 * internal configuration and {@link RepositoryConnector} stuff from the client
 * (e.g. the webapp)
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleServiceImpl implements CycleService {

  private static CycleService INSTANCE;

  private CycleRepositoryService repositoryService;

  private CycleTagService tagService;

  private CycleConfigurationService configurationService;

  private CycleServiceImpl() {
    ServiceImplConfiguration.wireServices(this);
    ServiceImplConfiguration.initializeServices(this);
  }

  void setRepositoryService(CycleRepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

  void setConfigurationService(CycleConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  void setTagService(CycleTagService tagService) {
    this.tagService = tagService;
  }

  public CycleTagService getTagService() {
    return tagService;
  }

  public CycleRepositoryService getRepositoryService() {
    return repositoryService;
  }

  public CycleConfigurationService getConfigurationService() {
    return configurationService;
  }

  public static CycleService getInstance() {
    if (INSTANCE == null) {
      synchronized (CycleServiceImpl.class) {
        if (INSTANCE == null) {
          INSTANCE = new CycleServiceImpl();
        }
      }
    }
    return INSTANCE;
  }

}