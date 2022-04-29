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
package org.activiti.engine.impl.el;

import javax.el.CompositeELResolver;
import javax.el.ELResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.core.el.JsonNodeELResolver;
import org.activiti.engine.impl.context.Context;

/**
 * Defines property resolution behavior on JsonNodes.
 *
 * @see CompositeELResolver
 * @see ELResolver
 */
public class CustomMapperJsonNodeELResolver extends JsonNodeELResolver {

    @Override
    protected ObjectMapper getObjectMapper() {
        return Context.getProcessEngineConfiguration().getObjectMapper();
    }
}
