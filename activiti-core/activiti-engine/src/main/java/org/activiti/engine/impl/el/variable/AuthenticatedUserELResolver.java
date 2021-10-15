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
package org.activiti.engine.impl.el.variable;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.identity.Authentication;

public class AuthenticatedUserELResolver
    implements VariableScopeItemELResolver {

    private static final String AUTHENTICATED_USER_KEY = "authenticatedUserId";

    @Override
    public boolean canResolve(String property, VariableScope variableScope) {
        return AUTHENTICATED_USER_KEY.equals(property);
    }

    @Override
    public Object resolve(String property, VariableScope variableScope) {
        return Authentication.getAuthenticatedUserId();
    }
}
