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
package org.activiti.examples.bpmn.tasklistener;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;



public class TaskDeleteListener implements TaskListener {

  private static final long serialVersionUID = 1L;
  private static List<String> messages = new ArrayList<String>();

  public static List<String> getCurrentMessages() {
    return messages;
  }

  public static void clear() {
    messages.clear();
  }

  @Override
  public void notify(DelegateTask delegateTask) {
    messages.add("Delete Task Listener executed.");
  }
}
