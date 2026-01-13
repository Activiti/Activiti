/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.test.bpmn.usertask;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import java.util.Calendar;
import java.util.Date;

public class UpdateTaskDetailsTaskListener implements TaskListener {

  private static final long serialVersionUID = 1L;

  @Override
  public void notify(DelegateTask delegateTask) {
      // calculate new dueDate
      Calendar cal = Calendar.getInstance();
      cal.setTime (delegateTask.getDueDate());
      cal.add (Calendar.DATE, 1);
      Date newDueDate = cal.getTime();

      // set new dueDate
      delegateTask.setDueDate(newDueDate);
  }

}
