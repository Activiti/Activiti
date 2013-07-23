package org.activiti.standalone.testing;

import static org.junit.Assert.assertNotNull;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@link JavaDelegate} class with autowired bean.
 * @author Willem Salembier
 */
public class ServiceTaskDelegate implements JavaDelegate {

  @Autowired
  private SpringBean bean;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    assertNotNull("Bean was not autowired", bean);
    execution.setVariable("output", bean.upperCase("Hello World!"));
    assertNotNull("Bean was not autowired", execution.getVariable("output"));
  }

}
