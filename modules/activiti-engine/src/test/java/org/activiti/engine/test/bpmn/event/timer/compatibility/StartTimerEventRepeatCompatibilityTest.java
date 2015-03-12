package org.activiti.engine.test.bpmn.event.timer.compatibility;

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.ManagementServiceImpl;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.api.event.TestActivitiEntityEventListener;

import java.util.Date;
import java.util.List;

public class StartTimerEventRepeatCompatibilityTest extends TimerEventCompatibilityTest {

  private TestActivitiEntityEventListener listener;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    listener = new TestActivitiEntityEventListener(Job.class);
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    if (listener != null) {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }

  /**
   * Timer repetition
   */
  @Deployment
  public void testCycleDateStartTimerEvent() throws Exception {

    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(1, jobs.size());

    JobEntity job= (JobEntity) jobs.get(0);
    changeConfigurationToPlainText(job);

    jobs = managementService.createJobQuery().list();
    assertEquals(1, jobs.size());
     job= (JobEntity) jobs.get(0);
    String activityId = TimerEventHandler.getActivityIdFromConfiguration(job.getJobHandlerConfiguration());
    assertTrue(activityId.equals(job.getJobHandlerConfiguration()));

    Clock previousClock = processEngineConfiguration.getClock();

    Clock testClock = new DefaultClockImpl();

    processEngineConfiguration.setClock(testClock);

    Date now = new Date();
    testClock.setCurrentTime(now);

    listener.clearEventsReceived();

    try {
      waitForJobExecutorToProcessAllJobs(2000, 500);
      fail("there must be a pending job");
    } catch (Exception e) {
      //expected failure
    }

    moveByMinutes(60);

    try {
      waitForJobExecutorToProcessAllJobs(2000, 500);
      fail("there must be a pending job");
    } catch (Exception e) {
      //expected failure
    }

    moveByMinutes(60);

    try {
      waitForJobExecutorToProcessAllJobs(2000, 500);
      fail("there must be a pending job");
    } catch (Exception e) {
      //expected failure
    }

    try {
      waitForJobExecutorToProcessAllJobs(2000, 500);
      fail("there must be a pending job");
    } catch (Exception e) {
      //expected failure
    }

    try {
      waitForJobExecutorToProcessAllJobs(2000, 500);
      fail("there must be a pending job");
    } catch (Exception e) {
      //expected failure
    }

    moveByMinutes(60);

    try {
      waitForJobExecutorToProcessAllJobs(2000, 500);
      fail("there must be a pending job");
    } catch (Exception e) {
      //expected failure
    }
    moveByMinutes(60);

    try {
      waitForJobExecutorToProcessAllJobs(2000, 500);
    } catch (Exception e) {
      fail("this is supposed to be the last execution.");
    }

    // count timer fired events
    int timerFiredCount = 0;
    List<ActivitiEvent> eventsReceived = listener.getEventsReceived();
    for (ActivitiEvent eventReceived : eventsReceived) {
      if (ActivitiEventType.TIMER_FIRED.equals(eventReceived.getType())) {
        timerFiredCount++;
      }
    }
    listener.clearEventsReceived();
    processEngineConfiguration.setClock(previousClock);

    assertEquals(4, timerFiredCount);
  }


}
