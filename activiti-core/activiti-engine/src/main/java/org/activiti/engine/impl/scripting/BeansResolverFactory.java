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


package org.activiti.engine.impl.scripting;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;


public class BeansResolverFactory implements ResolverFactory, Resolver {

  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  public Resolver createResolver(ProcessEngineConfigurationImpl processEngineConfiguration, VariableScope variableScope) {
    this.processEngineConfiguration = processEngineConfiguration;
    return this;
  }

  public boolean containsKey(Object key) {
    return processEngineConfiguration.getBeans().containsKey(key);
  }

  public Object get(Object key) {
    return processEngineConfiguration.getBeans().get(key);
  }
}
