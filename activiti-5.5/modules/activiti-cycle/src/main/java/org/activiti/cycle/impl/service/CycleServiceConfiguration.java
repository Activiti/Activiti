package org.activiti.cycle.impl.service;

import org.activiti.cycle.impl.db.impl.CycleDaoMyBatisImpl;
import org.activiti.cycle.service.CycleConfigurationService;
import org.activiti.cycle.service.CycleContentService;
import org.activiti.cycle.service.CycleEventService;
import org.activiti.cycle.service.CyclePluginService;
import org.activiti.cycle.service.CycleProcessSolutionService;
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

  private CycleCommentServiceImpl commentService;

  private CycleConfigurationServiceImpl configurationService;

  private CyclePluginServiceImpl cyclePluginServiceImpl;

  private CycleContentServiceImpl cycleContentServiceImpl;

  private CycleProcessSolutionServiceImpl cycleProcessSolutionServiceImpl;

  private CycleEventServiceImpl cycleEventServiceImpl;

  private CycleServiceConfiguration() {
    wireServices();
    initializeServices();
  }

  private void wireServices() {
    // instantiate dependencies:
    CycleDaoMyBatisImpl dao = new CycleDaoMyBatisImpl();
    repositoryService = new CycleRepositoryServiceImpl();
    configurationService = new CycleConfigurationServiceImpl();
    tagService = new CycleTagServiceImpl();
    commentService = new CycleCommentServiceImpl();
    cyclePluginServiceImpl = new CyclePluginServiceImpl();
    cycleContentServiceImpl = new CycleContentServiceImpl();
    cycleProcessSolutionServiceImpl = new CycleProcessSolutionServiceImpl();
    cycleEventServiceImpl = new CycleEventServiceImpl();

    // wire-up
    repositoryService.setLinkDao(dao);
    configurationService.setCycleConfigurationDao(dao);
    configurationService.setCycleRepositoryConnectorConfigurationDao(dao);
    tagService.setTagDao(dao);
    commentService.setTagDao(dao);
    cycleProcessSolutionServiceImpl.setDao(dao);
    repositoryService.setCycleServiceConfiguration(this);
    tagService.setCycleServiceConfiguration(this);
    configurationService.setCycleServiceConfiguration(this);

  }

  private void initializeServices() {
    // initialize
    tagService.initialize();
    commentService.initialize();
    repositoryService.initialize();
    configurationService.initialize();
  }

  public CycleTagServiceImpl getTagService() {
    return tagService;
  }

  public CycleCommentServiceImpl getCommentService() {
    return commentService;
  }

  public CycleRepositoryServiceImpl getRepositoryService() {
    return repositoryService;
  }

  public CycleConfigurationServiceImpl getConfigurationService() {
    return configurationService;
  }

  public CyclePluginServiceImpl getPluginService() {
    return cyclePluginServiceImpl;
  }

  public CycleContentServiceImpl getContentService() {
    return cycleContentServiceImpl;
  }

  public CycleProcessSolutionServiceImpl getProcessSolutionService() {
    return cycleProcessSolutionServiceImpl;
  }

  public CycleEventServiceImpl getCycleEventService() {
    return cycleEventServiceImpl;
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