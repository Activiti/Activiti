import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.test.Deployment;


public class JanTest extends PluggableActivitiTestCase {
	
	@Deployment
	public void testen() {
		runtimeService.startProcessInstanceByKey("testExpressionOnTimer");
		assertEquals(3, taskService.createTaskQuery().count());
//		Job job = managementService.createJobQuery().singleResult();
//		managementService.executeJob(job.getId());
		runtimeService.signalEventReceived("mySignal");
		System.out.println("blah");
	}

}
