package org.activiti.engine.test.db;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobManager;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;

/**
 * 
 * @author Kristin Polenz
 */
public class JobQueryTest extends PluggableActivitiTestCase {
  
  private TimerEntity timerEntity;
  
  public void testJobQueryExceptions() throws Throwable {
    
    createJobWithoutExceptionMsg();
    
    Job job = managementService.createJobQuery().jobId(timerEntity.getId()).singleResult();
    
    assertNotNull(job);
    
    List<Job> list = managementService.createJobQuery().withException().list();
    assertEquals(list.size(), 1);
    
    deleteJobInDatabase();
    
    createJobWithoutExceptionStacktrace();
    
    job = managementService.createJobQuery().jobId(timerEntity.getId()).singleResult();
    
    assertNotNull(job);
    
    list = managementService.createJobQuery().withException().list();
    assertEquals(list.size(), 1);
    
    deleteJobInDatabase();
    
  }
  
  private void createJobWithoutExceptionMsg() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
        
        timerEntity = new TimerEntity();
        timerEntity.setLockOwner(UUID.randomUUID().toString());
        timerEntity.setDuedate(new Date());
        timerEntity.setRetries(0);

        StringWriter stringWriter = new StringWriter();
        NullPointerException exception = new NullPointerException();
        exception.printStackTrace(new PrintWriter(stringWriter));
        timerEntity.setExceptionStacktrace(stringWriter.toString());

        jobManager.insert(timerEntity);
        
        assertNotNull(timerEntity.getId());
        
        return null;
        
      }
    });
    
  }
  
  private void createJobWithoutExceptionStacktrace() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
        
        timerEntity = new TimerEntity();
        timerEntity.setLockOwner(UUID.randomUUID().toString());
        timerEntity.setDuedate(new Date());
        timerEntity.setRetries(0);
        timerEntity.setExceptionMessage("I'm supposed to fail");

        jobManager.insert(timerEntity);
        
        assertNotNull(timerEntity.getId());
        
        return null;
        
      }
    });
    
  }  
  
  private void deleteJobInDatabase() {
      CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
      commandExecutor.execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          
          timerEntity.delete();          
          return null;
        }
      });    
  }

}
