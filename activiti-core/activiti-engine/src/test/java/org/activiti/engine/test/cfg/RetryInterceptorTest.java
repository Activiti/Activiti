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
package org.activiti.engine.test.cfg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.RetryInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class RetryInterceptorTest {

  protected ProcessEngine processEngine;

  protected RetryInterceptor retryInterceptor;

  @Before
  public void setupProcessEngine() {
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration.setJdbcUrl("jdbc:h2:mem:retryInterceptorTest");
    List<CommandInterceptor> interceptors = new ArrayList<CommandInterceptor>();
    retryInterceptor = new RetryInterceptor();
    interceptors.add(retryInterceptor);
    processEngineConfiguration.setCustomPreCommandInterceptors(interceptors);
    processEngine = processEngineConfiguration.buildProcessEngine();
  }

  @After
  public void shutdownProcessEngine() {
    processEngine.close();
  }

  @Test
  public void testRetryInterceptor() {

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> processEngine.getManagementService().executeCommand(new CommandThrowingOptimisticLockingException()))
      .withMessageContaining(retryInterceptor.getNumOfRetries() + " retries failed");

    assertThat(counter.get()).isEqualTo(retryInterceptor.getNumOfRetries() + 1); // +1, we retry 3 times, so one extra for the regular execution
  }

  public static AtomicInteger counter = new AtomicInteger();

  protected class CommandThrowingOptimisticLockingException implements Command<Void> {

    public Void execute(CommandContext commandContext) {

      counter.incrementAndGet();

      throw new ActivitiOptimisticLockingException("");
    }
  }
}
