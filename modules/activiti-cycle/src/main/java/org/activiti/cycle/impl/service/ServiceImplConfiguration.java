package org.activiti.cycle.impl.service;

import org.activiti.cycle.impl.db.impl.CycleDaoMyBatisImpl;

/**
 * Performs the configuration and wiring of the CycleServiceImplementations. I
 * do not want the service implementations to be aware of the concrete
 * implementations of their dependencies.
 * 
 * @author daniel.meyer@camunda.com
 */
public class ServiceImplConfiguration {

  public static void wireServices(CycleServiceImpl cycleServiceImpl) {
    // instantiate dependencies:
    CycleDaoMyBatisImpl dao = new CycleDaoMyBatisImpl();
    CycleRepositoryServiceImpl repositoryServiceImpl = new CycleRepositoryServiceImpl();
    CycleConfigurationServiceImpl configurationServiceImpl = new CycleConfigurationServiceImpl();
    CycleTagServiceImpl tagServiceImpl = new CycleTagServiceImpl();

    // wire-up
    repositoryServiceImpl.setLinkDao(dao);
    configurationServiceImpl.setCycleConfigurationDao(dao);
    tagServiceImpl.setTagDao(dao);
    repositoryServiceImpl.setCycleService(cycleServiceImpl);
    tagServiceImpl.setCycleService(cycleServiceImpl);    
    cycleServiceImpl.setConfigurationService(configurationServiceImpl);
    cycleServiceImpl.setRepositoryService(repositoryServiceImpl);
    cycleServiceImpl.setTagService(tagServiceImpl);
  }

  public static void initializeServices(CycleServiceImpl cycleServiceImpl) {
    CycleRepositoryServiceImpl repositoryServiceImpl = (CycleRepositoryServiceImpl) cycleServiceImpl.getRepositoryService();
    CycleConfigurationServiceImpl configurationServiceImpl = (CycleConfigurationServiceImpl) cycleServiceImpl.getConfigurationService();
    CycleTagServiceImpl tagServiceImpl = (CycleTagServiceImpl) cycleServiceImpl.getTagService();

    // initialize
    tagServiceImpl.initialize();
    repositoryServiceImpl.initialize();
    configurationServiceImpl.initialize();

  }
}
