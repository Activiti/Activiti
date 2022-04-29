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

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;

/**
 */
public class JobExecutorFailRetryTest extends PluggableActivitiTestCase {

  @Deployment
  public void testFailedServiceTask() {

    // process throws no exception. Service task passes at the first time.
    RetryFailingDelegate.shallThrow = false; // do not throw exception in Service delegate
    RetryFailingDelegate.resetTimeList();
    runtimeService.startProcessInstanceByKey("failedJobRetry");

    waitForJobExecutorToProcessAllJobs(1000, 200);
    assertThat(RetryFailingDelegate.times).hasSize(1); // check number of calls of delegate

    // process throws exception two times, with 6 seconds in between
    RetryFailingDelegate.shallThrow = true; // throw exception in Service delegate
    RetryFailingDelegate.resetTimeList();
    runtimeService.startProcessInstanceByKey("failedJobRetry");

    executeJobExecutorForTime(14000, 500);
    assertThat(RetryFailingDelegate.times).hasSize(2); // check number of calls of delegate
    long timeDiff = RetryFailingDelegate.times.get(1) - RetryFailingDelegate.times.get(0);
    assertThat(timeDiff > 6000 && timeDiff < 12000).isTrue(); // check time difference between calls. Just roughly
  }
}
