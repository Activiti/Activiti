/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.cfg;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;


public abstract class AbstractProcessEngineConfigurator implements ProcessEngineConfigurator {

  public static int DEFAULT_CONFIGURATOR_PRIORITY = 10000;

  @Override
  public int getPriority() {
    return DEFAULT_CONFIGURATOR_PRIORITY;
  }

  public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

  }

  public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {

  }

}
