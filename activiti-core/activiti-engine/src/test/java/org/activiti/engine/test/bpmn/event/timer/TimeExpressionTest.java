package org.activiti.engine.test.bpmn.event.timer;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * Test timer expression according to act-865
 *

 */

public class TimeExpressionTest extends PluggableActivitiTestCase {

  private Date testExpression(String timeExpression) {
    // Set the clock fixed
    HashMap<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("dueDate", timeExpression);

    // After process start, there should be timer created
    ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("intermediateTimerEventExample", variables1);
    assertThat(managementService.createTimerJobQuery().processInstanceId(pi1.getId()).count()).isEqualTo(1);

    List<Job> jobs = managementService.createTimerJobQuery().executable().list();
    assertThat(jobs).hasSize(1);
    return jobs.get(0).getDuedate();
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/timer/IntermediateTimerEventTest.testExpression.bpmn20.xml" })
  public void testTimeExpressionComplete() throws Exception {
    Date dt = new Date();

    Date dueDate = testExpression(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dt));
    assertThat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dueDate)).isEqualTo(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dt));
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/timer/IntermediateTimerEventTest.testExpression.bpmn20.xml" })
  public void testTimeExpressionWithoutSeconds() throws Exception {
    Date dt = new Date();

    Date dueDate = testExpression(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(dt));
    assertThat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(dueDate)).isEqualTo(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(dt));
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/timer/IntermediateTimerEventTest.testExpression.bpmn20.xml" })
  public void testTimeExpressionWithoutMinutes() throws Exception {
    Date dt = new Date();

    Date dueDate = testExpression(new SimpleDateFormat("yyyy-MM-dd'T'HH").format(new Date()));
    assertThat(new SimpleDateFormat("yyyy-MM-dd'T'HH").format(dueDate)).isEqualTo(new SimpleDateFormat("yyyy-MM-dd'T'HH").format(dt));
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/timer/IntermediateTimerEventTest.testExpression.bpmn20.xml" })
  public void testTimeExpressionWithoutTime() throws Exception {
    Date dt = new Date();

    Date dueDate = testExpression(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    assertThat(new SimpleDateFormat("yyyy-MM-dd").format(dueDate)).isEqualTo(new SimpleDateFormat("yyyy-MM-dd").format(dt));
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/timer/IntermediateTimerEventTest.testExpression.bpmn20.xml" })
  public void testTimeExpressionWithoutDay() throws Exception {
    Date dt = new Date();

    Date dueDate = testExpression(new SimpleDateFormat("yyyy-MM").format(new Date()));
    assertThat(new SimpleDateFormat("yyyy-MM").format(dueDate)).isEqualTo(new SimpleDateFormat("yyyy-MM").format(dt));
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/timer/IntermediateTimerEventTest.testExpression.bpmn20.xml" })
  public void testTimeExpressionWithoutMonth() throws Exception {
    Date dt = new Date();

    Date dueDate = testExpression(new SimpleDateFormat("yyyy").format(new Date()));
    assertThat(new SimpleDateFormat("yyyy").format(dueDate)).isEqualTo(new SimpleDateFormat("yyyy").format(dt));
  }
}
