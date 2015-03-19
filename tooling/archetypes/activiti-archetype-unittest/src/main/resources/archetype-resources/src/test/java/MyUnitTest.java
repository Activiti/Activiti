package ${package};
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class MyUnitTest {

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();

	@Test
	@Deployment(resources = {"${packageInPathFormat}/my-process.bpmn20.xml"})
	public void test() {
		ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey("my-process");
		assertNotNull(processInstance);

		Task task = activitiRule.getTaskService().createTaskQuery().singleResult();
		assertEquals("Activiti is awesome!", task.getName());
	}

}
