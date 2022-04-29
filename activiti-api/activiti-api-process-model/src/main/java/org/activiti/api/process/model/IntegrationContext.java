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
package org.activiti.api.process.model;

import java.util.Map;

public interface IntegrationContext {

    String getId();

    String getProcessInstanceId();

    String getRootProcessInstanceId();

    String getParentProcessInstanceId();

    String getExecutionId();

    String getProcessDefinitionId();

    String getProcessDefinitionKey();

    Integer getProcessDefinitionVersion();

    String getBusinessKey();

    String getConnectorType();

    String getAppVersion();

    String getClientId();
    String getClientName();
    String getClientType();

    Map<String, Object> getInBoundVariables();

    Map<String, Object> getOutBoundVariables();

    void addOutBoundVariable(String name, Object value);

    void addOutBoundVariables(Map<String, Object> variables);

    <T> T getInBoundVariable(String name);

    <T> T getInBoundVariable(String name, Class<T> type);

    <T> T getOutBoundVariable(String name);

    <T> T getOutBoundVariable(String name, Class<T> type);
}
