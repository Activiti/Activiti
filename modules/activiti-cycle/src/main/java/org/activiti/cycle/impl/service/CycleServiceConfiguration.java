package org.activiti.cycle.impl.service;

import org.activiti.cycle.impl.db.impl.CycleDaoMyBatisImpl;
import org.activiti.cycle.service.CycleConfigurationService;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleTagService;

/**
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleServiceConfiguration {

  private static CycleServiceConfiguration INSTANCE;

  private CycleRepositoryServiceImpl repositoryService;

  private CycleTagServiceImpl tagService;

  private CycleConfigurationServiceImpl configurationService;

  private CycleServiceConfiguration() {
    wireServices();
    initializeServices();
  }

  public void wireServices() {
    // instantiate dependencies:
    CycleDaoMyBatisImpl dao = new CycleDaoMyBatisImpl();
    repositoryService = new CycleRepositoryServiceImpl();
    configurationService = new CycleConfigurationServiceImpl();
    tagService = new CycleTagServiceImpl();

    // wire-up
    repositoryService.setLinkDao(dao);
    configurationService.setCycleConfigurationDao(dao);
    tagService.setTagDao(dao);
    repositoryService.setCycleServiceConfiguration(this);
    tagService.setCycleServiceConfiguration(this);
    configurationService.setCycleServiceConfiguration(this);
  }

  public void initializeServices() {
    // initialize
    tagService.initialize();
    repositoryService.initialize();
    configurationService.initialize();
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

  public static CycleServiceConfiguration getInstance() {
    if (INSTANCE == null) {
      synchronized (CycleServiceConfiguration.class) {
        if (INSTANCE == null) {
          INSTANCE = new CycleServiceConfiguration();
        }
      }
    }
    return INSTANCE;
  }

}