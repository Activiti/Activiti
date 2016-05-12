/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.activiti.examples.bpmn.executionlistener;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * @author Yvo Swillens
 */
public class ConditionalThrowExceptionDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    boolean throwException = (boolean) execution.getVariable(execution.getCurrentActivityId());

    if (throwException) {
      throw new ActivitiException("throwException was true");
    }
  }
}
