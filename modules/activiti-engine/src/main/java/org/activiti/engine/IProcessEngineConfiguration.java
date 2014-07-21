package org.activiti.engine;

import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.impl.calendar.BusinessCalendarManager;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.event.EventHandler;
import org.activiti.engine.impl.form.FormTypes;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.variable.VariableTypes;
import org.activiti.engine.runtime.Clock;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.validation.ProcessValidator;
import java.util.Map;

/**
 * Interface for a Process Engine configurator.
 *
 */
public interface IProcessEngineConfiguration extends EngineServices {

    ActivitiEventDispatcher getEventDispatcher();

    HistoryLevel getHistoryLevel();

    boolean isDbIdentityUsed();

    String getDatabaseSchemaUpdate();

    Clock getClock();

    CommandExecutor getCommandExecutor();

    VariableTypes getVariableTypes();

    DeploymentManager getDeploymentManager();

    DelegateInterceptor getDelegateInterceptor();

    EventHandler getEventHandler(String eventType);

    JobExecutor getJobExecutor();

    Map<String, JobHandler> getJobHandlers();

    boolean isCreateDiagramOnDeploy();

    ProcessDiagramGenerator getProcessDiagramGenerator();

    String getActivityFontName();

    String getLabelFontName();

    ClassLoader getClassLoader();

    BusinessCalendarManager getBusinessCalendarManager();

    boolean isEnableSafeBpmnXml();

    String getXmlEncoding();

    ProcessValidator getProcessValidator();

    String getMailServerDefaultFrom();

    String getMailSesionJndi();

    String getMailServerHost();

    int getMailServerPort();

    boolean getMailServerUseSSL();

    boolean getMailServerUseTLS();

    String getMailServerUsername();

    String getMailServerPassword();

    ExpressionManager getExpressionManager();

    ScriptingEngines getScriptingEngines();

    IdGenerator getIdGenerator();

    FormTypes getFormTypes();

    Map<Object, Object> getBeans();
}