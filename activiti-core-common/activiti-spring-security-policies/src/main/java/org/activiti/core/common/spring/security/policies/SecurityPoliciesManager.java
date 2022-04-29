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
package org.activiti.core.common.spring.security.policies;

import java.util.Map;
import java.util.Set;

public interface SecurityPoliciesManager {

    boolean canRead(String processDefinitionKey,
                    String serviceName);

    boolean canWrite(String processDefinitionKey,
                     String serviceName);

    boolean canRead(String processDefinitionKey);

    boolean canWrite(String processDefinitionKey);

    boolean arePoliciesDefined();

    Map<String, Set<String>> getAllowedKeys(SecurityPolicyAccess... securityPolicyAccess);

}
