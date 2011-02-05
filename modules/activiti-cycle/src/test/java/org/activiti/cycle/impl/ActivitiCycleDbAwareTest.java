package org.activiti.cycle.impl;

import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleMapContext;
import org.activiti.cycle.context.CycleRequestContext;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

public abstract class ActivitiCycleDbAwareTest extends PluggableActivitiTestCase {

  @Override
  protected void setUp() throws Exception {
    ensureCycleDbCreated();
    setupCycleContextInfrastructure();
  }

  @Override
  protected void tearDown() throws Exception {
    tearDownCycleContextInfrastructure();
    ensureCycleDbDropped();
  }

  protected void ensureCycleDbCreated() {
    CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {

      public Object execute(CommandContext commandContext) {
        DbSqlSession session = commandContext.getSession(DbSqlSession.class);
//        session.executeSchemaResourceOperation("cycle/drop", "drop");
//        session.executeSchemaResourceOperation("cycle/create", "create");
        return null;
      }
    });
  }

  protected void ensureCycleDbDropped() {
    CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {

      public Object execute(CommandContext commandContext) {
        DbSqlSession session = commandContext.getSession(DbSqlSession.class);
//        session.executeSchemaResourceOperation("cycle/drop", "drop");
        return null;
      }
    });
  }

  protected void setupCycleContextInfrastructure() {
    CycleApplicationContext.setWrappedContext(new CycleMapContext());
    CycleSessionContext.setContext(new CycleMapContext());
    CycleRequestContext.setContext(new CycleMapContext());
    populateApplicationContext();
    populateSessionContext();
    populateRequestContext();
  }

  protected void populateApplicationContext() {
  }

  protected void populateSessionContext() {
    CycleSessionContext.set("cuid", "testuser");
  }

  protected void populateRequestContext() {
  }

  protected void tearDownCycleContextInfrastructure() {
    CycleApplicationContext.setWrappedContext(null);
    CycleSessionContext.clearContext();
    CycleRequestContext.clearContext();
  }

}
