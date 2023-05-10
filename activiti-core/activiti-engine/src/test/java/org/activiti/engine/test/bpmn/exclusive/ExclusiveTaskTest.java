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

package org.activiti.engine.test.bpmn.exclusive;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.test.Deployment;

/**
 *

 */
public class ExclusiveTaskTest extends PluggableActivitiTestCase {

  @Deployment
  public void testNonExclusiveService() {
    // start process
    runtimeService.startProcessInstanceByKey("exclusive");
    // now there should be 1 non-exclusive job in the database:
    Job job = managementService.createJobQuery().singleResult();
    assertThat(job).isNotNull();
    assertThat(((JobEntity) job).isExclusive()).isFalse();

    waitForJobExecutorToProcessAllJobs(6000L);

    // all the jobs are done
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testExclusiveService() {
    // start process
    runtimeService.startProcessInstanceByKey("exclusive");
    // now there should be 1 exclusive job in the database:
    Job job = managementService.createJobQuery().singleResult();
    assertThat(job).isNotNull();
    assertThat(((JobEntity) job).isExclusive()).isTrue();

    waitForJobExecutorToProcessAllJobs(6000L);

    // all the jobs are done
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testExclusiveServiceConcurrent() {
    // start process
    runtimeService.startProcessInstanceByKey("exclusive");
    // now there should be 3 exclusive jobs in the database:
    assertThat(managementService.createJobQuery().count()).isEqualTo(3);

    waitForJobExecutorToProcessAllJobs(20000L);

    // all the jobs are done
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
  }

}
