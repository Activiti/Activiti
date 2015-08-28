
package org.activiti5.engine.test.api.task;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class DeleteCandidateTaskListener implements TaskListener {
  public void notify(DelegateTask delegateTask) {
    delegateTask.deleteCandidateUser("admin");
  }
}
