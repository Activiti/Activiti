package org.activiti.spring.test.engine;

import org.activiti.engine.ProcessEngines;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

/**
 * Spring process engine base test
 *
 * @author: Henry Yan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:org/activiti/spring/test/engine/springProcessEngine-context.xml")
public class SpringProcessEngineTest {

    @Test
    public void testGetEngineFromCache() {
        assertNotNull(ProcessEngines.getDefaultProcessEngine());
        assertNotNull(ProcessEngines.getProcessEngine("default"));
    }

}