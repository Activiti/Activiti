/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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


package org.activiti.engine.impl.persistence.entity;

import java.text.MessageFormat;

import org.activiti.bpmn.model.Signal;

/**


 */
public class SignalEventSubscriptionEntityImpl extends EventSubscriptionEntityImpl implements SignalEventSubscriptionEntity {

  private static final long serialVersionUID = 1L;

  // Using json here, but not worth of adding json dependency lib for this
  private static final String CONFIGURATION_TEMPLATE = "'{'\"scope\":\"{0}\"'}'";

  public SignalEventSubscriptionEntityImpl() {
    eventType = EVENT_TYPE;
  }

  @Override
  public void setConfiguration(String configuration) {
    if (configuration != null && configuration.contains("{\"scope\":")) {
      this.configuration = configuration;
    } else {
      this.configuration = MessageFormat.format(CONFIGURATION_TEMPLATE, configuration);
    }
  }

  public boolean isProcessInstanceScoped() {
    String scope = extractScopeFormConfiguration();
    return (scope != null) && (Signal.SCOPE_PROCESS_INSTANCE.equals(scope));
  }

  public boolean isGlobalScoped() {
    String scope = extractScopeFormConfiguration();
    return (scope == null) || (Signal.SCOPE_GLOBAL.equals(scope));
  }

  protected String extractScopeFormConfiguration() {
    if (this.configuration == null) {
      return null;
    } else {
      return this.configuration.substring(10, this.configuration.length() - 2); // 10 --> length of {"scope": and -2 for removing"}
    }
  }

}
