package org.activiti.engine.test.cfg.executioncount;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import java.util.Random;


public class GenerateRandomValueActivity implements JavaDelegate {
  
  private static final long serialVersionUID = 1L;
  
  private static Random random = new Random();

  public void execute(DelegateExecution execution) {
    Integer value = random.nextInt(10);
    execution.setVariable("var", value);
  }

}