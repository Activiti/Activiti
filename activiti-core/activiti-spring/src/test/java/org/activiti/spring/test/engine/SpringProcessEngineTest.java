package org.activiti.spring.test.engine;

import org.activiti.engine.ProcessEngines;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring process engine base test
 *

 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:org/activiti/spring/test/engine/springProcessEngine-context.xml")
public class SpringProcessEngineTest {

  @Test
  public void testGetEngineFromCache() {
    assertThat(ProcessEngines.getDefaultProcessEngine()).isNotNull();
    assertThat(ProcessEngines.getProcessEngine("default")).isNotNull();
  }

}
