package org.activiti.osgi.blueprint;

import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.activiti.engine.cfg.ProcessEngineConfigurator;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.parse.BpmnParseHandler;

public class ConfigurationFactory {

    private DataSource dataSource;
    private String databaseSchemaUpdate;
    private boolean isCreateDiagramOnDeploy = true;
    private boolean jobExecutorActivate = true;
    private List<ProcessEngineConfigurator> configurators;
    private List<ActivitiEventListener> customEventListeners;
    private List<BpmnParseHandler> customDefaultBpmnParseHandlers;
    private ActivityBehaviorFactory activityBehaviorFactory;
    private Set<Class<?>> customMybatisMappers;

    public StandaloneProcessEngineConfiguration getConfiguration() {
      StandaloneProcessEngineConfiguration conf =
              new StandaloneProcessEngineConfiguration();
      conf.setDataSource(this.dataSource);
      conf.setDatabaseSchemaUpdate(this.databaseSchemaUpdate);
      conf.setJobExecutorActivate(this.jobExecutorActivate);
      conf.setEventListeners(this.customEventListeners);
      conf.setConfigurators(this.configurators);
      conf.setCustomDefaultBpmnParseHandlers(this.customDefaultBpmnParseHandlers);
      conf.setCustomMybatisMappers(this.customMybatisMappers);
      conf.setActivityBehaviorFactory(this.activityBehaviorFactory);
        conf.setCreateDiagramOnDeploy(this.isCreateDiagramOnDeploy);
      return conf;
    }

    public void setDataSource(DataSource dataSource) {
       this.dataSource = dataSource;
    }

    public void setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
      this.databaseSchemaUpdate = databaseSchemaUpdate;
    }

    public void setJobExecutorActivate(boolean jobExecutorActivate) {
      this.jobExecutorActivate = jobExecutorActivate;
    }

    public void setConfigurators(List<ProcessEngineConfigurator> configurators) {
      this.configurators = configurators;
    }

    public void setCustomDefaultBpmnParseHandlers(List<BpmnParseHandler> handlers) {
        this.customDefaultBpmnParseHandlers = handlers;
    }

    public void setActivityBehaviorFactory(ActivityBehaviorFactory factory) {
        this.activityBehaviorFactory = factory;
    }

    public void setCustomMybatisMappers(Set<Class<?>> customMybatisMappers) {
        this.customMybatisMappers = customMybatisMappers;
    }

    public List<ActivitiEventListener> getCustomEventListeners() {
        return this.customEventListeners;
    }

    public void setCustomEventListeners(List<ActivitiEventListener> listeners) {
        this.customEventListeners = listeners;
    }

    public void setCreateDiagramOnDeploy(boolean isSetCreateDiagramOnDeploy) {
        this.isCreateDiagramOnDeploy = isSetCreateDiagramOnDeploy;
    }
}
