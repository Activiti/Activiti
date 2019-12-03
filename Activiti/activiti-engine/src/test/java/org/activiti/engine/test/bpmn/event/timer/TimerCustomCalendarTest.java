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

package org.activiti.engine.test.bpmn.event.timer;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * testing custom calendar for timer definitions
 * Created by martin.grofcik
 */
public class TimerCustomCalendarTest extends ResourceActivitiTestCase {

  public TimerCustomCalendarTest() {
    super("org/activiti/engine/test/bpmn/event/timer/TimerCustomCalendarTest.activiti.cfg.xml");
  }

  @Deployment
  public void testCycleTimer() {
    List<Job> jobs = this.managementService.createTimerJobQuery().list();

    assertThat("One job is scheduled", jobs.size(), is(1));
    assertThat("Job must be scheduled by custom business calendar to Date(0)", jobs.get(0).getDuedate(), is(new Date(0)));

    managementService.moveTimerToExecutableJob(jobs.get(0).getId());
    managementService.executeJob(jobs.get(0).getId());

    jobs = this.managementService.createTimerJobQuery().list();

    assertThat("One job is scheduled (repetition is 2x)", jobs.size(), is(1));
    assertThat("Job must be scheduled by custom business calendar to Date(0)", jobs.get(0).getDuedate(), is(new Date(0)));

    managementService.moveTimerToExecutableJob(jobs.get(0).getId());
    managementService.executeJob(jobs.get(0).getId());

    jobs = this.managementService.createTimerJobQuery().list();
    assertThat("There must be no job.", jobs.isEmpty());
  }

  @Deployment
  public void testCustomDurationTimerCalendar() {
    ProcessInstance processInstance = this.runtimeService.startProcessInstanceByKey("testCustomDurationCalendar");

    List<Job> jobs = this.managementService.createTimerJobQuery().list();

    assertThat("One job is scheduled", jobs.size(), is(1));
    assertThat("Job must be scheduled by custom business calendar to Date(0)", jobs.get(0).getDuedate(), is(new Date(0)));

    managementService.moveTimerToExecutableJob(jobs.get(0).getId());
    managementService.executeJob(jobs.get(0).getId());
    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(10000, 200);

    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("receive").singleResult();
    runtimeService.trigger(execution.getId());
  }

  @Deployment
  public void testInvalidDurationTimerCalendar() {
    try {
      this.runtimeService.startProcessInstanceByKey("testCustomDurationCalendar");
      fail("Activiti exception expected - calendar not found");
    } catch (ActivitiException e) {
      assertThat(e.getMessage(), containsString("INVALID does not exist"));
    }
  }

  @Deployment
  public void testBoundaryTimer() {
    this.runtimeService.startProcessInstanceByKey("testBoundaryTimer");

    List<Job> jobs = this.managementService.createTimerJobQuery().list();
    assertThat("One job is scheduled", jobs.size(), is(1));
    assertThat("Job must be scheduled by custom business calendar to Date(0)", jobs.get(0).getDuedate(), is(new Date(0)));

    managementService.moveTimerToExecutableJob(jobs.get(0).getId());
    managementService.executeJob(jobs.get(0).getId());
    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(10000, 200);
  }

  public static class CustomBusinessCalendar implements BusinessCalendar {

    @Override
    public Date resolveDuedate(String duedateDescription) {
      return new Date(0);
    }

    @Override
    public Date resolveDuedate(String duedateDescription, int maxIterations) {
      return new Date(0);
    }

    @Override
    public Boolean validateDuedate(String duedateDescription, int maxIterations, Date endDate, Date newTimer) {
      return true;
    }

    @Override
    public Date resolveEndDate(String endDateString) {
      return new Date(0);
    }

  }

}
