/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.engine.impl.el;

import jakarta.el.CompositeELResolver;
import jakarta.el.ELResolver;
import org.activiti.core.el.JsonNodeELResolver;
import org.activiti.engine.impl.context.Context;
import tools.jackson.databind.json.JsonMapper;

/**
 * Defines property resolution behavior on JsonNodes.
 *
 * @see CompositeELResolver
 * @see ELResolver
 */
public class CustomMapperJsonNodeELResolver extends JsonNodeELResolver {

    @Override
    protected JsonMapper getObjectMapper() {
        return Context.getProcessEngineConfiguration().getObjectMapper();
    }
}
