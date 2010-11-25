package com.camunda.training.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


public class FileIssueDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    System.out.println("We create a issue for execution id " + execution.getId());

  }

}
