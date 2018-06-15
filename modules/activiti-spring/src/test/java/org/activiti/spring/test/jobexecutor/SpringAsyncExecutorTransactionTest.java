package org.activiti.spring.test.jobexecutor;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:org/activiti/spring/test/components/SpringjobExecutorTransactionTest-context.xml")
public class SpringAsyncExecutorTransactionTest extends SpringActivitiTestCase {
    @Autowired
    protected RuntimeService runtimeService;
  
    @Test
    public void testAsyncExecutorTransactionIsolation() throws Exception {
      ProcessInstance instance = runtimeService.startProcessInstanceByKey("async-test");
      assertNotNull(instance);
    }
}
