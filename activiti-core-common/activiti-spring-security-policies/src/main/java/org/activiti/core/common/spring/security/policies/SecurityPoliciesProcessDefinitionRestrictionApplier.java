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

import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.GetProcessDefinitionsPayload;

import java.util.Set;
import java.util.UUID;

public class SecurityPoliciesProcessDefinitionRestrictionApplier implements SecurityPoliciesRestrictionApplier<GetProcessDefinitionsPayload> {

    @Override
    public GetProcessDefinitionsPayload restrictToKeys(Set<String> keys) {
        return ProcessPayloadBuilder.processDefinitions().withProcessDefinitionKeys(keys).build();
    }

    @Override
    public GetProcessDefinitionsPayload denyAll() {
        //user should not see anything so give unsatisfiable condition
        return ProcessPayloadBuilder.processDefinitions().withProcessDefinitionKey("missing-" + UUID.randomUUID().toString()).build();
    }

    @Override
    public GetProcessDefinitionsPayload allowAll() {
        return ProcessPayloadBuilder.processDefinitions().build();
    }
}
