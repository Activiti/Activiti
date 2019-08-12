package org.activiti.engine.test.profiler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.engine.cfg.ProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandInterceptor;

/**

 */
public class ActivitiProfiler implements ProcessEngineConfigurator {

    protected static ActivitiProfiler INSTANCE = new ActivitiProfiler();

    protected ProfileSession currentProfileSession;
    protected List<ProfileSession> profileSessions = new ArrayList<ProfileSession>();

    public static ActivitiProfiler getInstance() {
        return INSTANCE;
    }

    @Override
    public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

        // Command interceptor
        List<CommandInterceptor> interceptors = new ArrayList<CommandInterceptor>();
        interceptors.add(new TotalExecutionTimeCommandInterceptor());
        processEngineConfiguration.setCustomPreCommandInterceptors(interceptors);

        // DbsqlSession
        processEngineConfiguration.setDbSqlSessionFactory(new ProfilingDbSqlSessionFactory());
    }

    @Override
    public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {

    }

    @Override
    public int getPriority() {
        return 0;
    }
    
    public void reset() {
      if (currentProfileSession != null) {
        stopCurrentProfileSession();
      }
      this.currentProfileSession = null;
      this.profileSessions.clear();
    }

    public void startProfileSession(String name) {
        currentProfileSession = new ProfileSession(name);
        profileSessions.add(currentProfileSession);
    }

    public void stopCurrentProfileSession() {
        currentProfileSession.setEndTime(new Date());
        currentProfileSession = null;
    }

    public ProfileSession getCurrentProfileSession() {
        return currentProfileSession;
    }

    public void setCurrentProfileSession(ProfileSession currentProfileSession) {
        this.currentProfileSession = currentProfileSession;
    }

    public List<ProfileSession> getProfileSessions() {
        return profileSessions;
    }

    public void setProfileSessions(List<ProfileSession> profileSessions) {
        this.profileSessions = profileSessions;
    }

}
