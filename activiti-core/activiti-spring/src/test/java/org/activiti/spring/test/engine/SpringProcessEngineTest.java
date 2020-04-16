package org.activiti.spring.test.engine;

import org.activiti.engine.ProcessEngines;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring process engine base test
 *

 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:org/activiti/spring/test/engine/springProcessEngine-context.xml")
public class SpringProcessEngineTest {

  @Test
  public void testGetEngineFromCache() {
    assertThat(ProcessEngines.getDefaultProcessEngine()).isNotNull();
    assertThat(ProcessEngines.getProcessEngine("default")).isNotNull();
  }

}
