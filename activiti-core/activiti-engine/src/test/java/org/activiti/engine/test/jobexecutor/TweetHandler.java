/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

package org.activiti.engine.test.jobexecutor;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import static org.assertj.core.api.Assertions.assertThat;

public class TweetHandler implements JobHandler {

  List<String> messages = new ArrayList<String>();

  public String getType() {
    return "tweet";
  }

  public void execute(JobEntity job, String configuration, ExecutionEntity execution, CommandContext commandContext) {
    messages.add(configuration);
    assertThat(commandContext).isNotNull();
  }

  public List<String> getMessages() {
    return messages;
  }
}
