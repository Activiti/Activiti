package org.activiti.upgrade.test;

import junit.framework.TestCase;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.util.LogUtil;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;


public abstract class UpgradeTestCase extends TestCase {
  
  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }

  static ProcessEngine processEngine; 
  static RuntimeService runtimeService;
  static TaskService taskService;
  static HistoryService historyService;
  static ManagementService managementService;

  protected static void runBeforeAndAfterInDevelopmentMode(UpgradeTestCase upgradeTest) {
    setProcessEngine(ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine());

    upgradeTest.runInTheOldVersion();
    Result result = JUnitCore.runClasses(upgradeTest.getClass());
    System.err.println();
    System.err.println("Tests run: "+result.getRunCount());
    System.err.println("Failures : "+result.getFailureCount());
    System.err.println();
    for (Failure failure: result.getFailures()) {
      System.err.println(failure.getDescription());
      Throwable e = failure.getException();
      if (e!=null) {
        e.printStackTrace();
      }
    }
  }

  public static void setProcessEngine(ProcessEngine processEngine) {
    UpgradeTestCase.processEngine = processEngine;
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    historyService = processEngine.getHistoryService();
    managementService = processEngine.getManagementService();
  }

  public abstract void runInTheOldVersion();
}
