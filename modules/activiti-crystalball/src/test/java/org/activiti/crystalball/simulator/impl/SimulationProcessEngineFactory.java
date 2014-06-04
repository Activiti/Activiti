package org.activiti.crystalball.simulator.impl;

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


import java.util.concurrent.atomic.AtomicLong;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author martin.grofcik
 */
public class SimulationProcessEngineFactory implements FactoryBean<ProcessEngineImpl> {
  protected final ProcessEngineConfiguration processEngineConfiguration;
  protected final AtomicLong uniqueLongId;

  public SimulationProcessEngineFactory(ProcessEngineConfiguration processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
    this.uniqueLongId = new AtomicLong(0);
  }

  @Override
  public ProcessEngineImpl getObject() {
    this.processEngineConfiguration.setProcessEngineName("simRunProcessEngine-" + uniqueLongId.getAndIncrement());

    return (ProcessEngineImpl) this.processEngineConfiguration.buildProcessEngine();
  }

  @Override
  public Class<?> getObjectType() {
    return ProcessEngineImpl.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }
}
