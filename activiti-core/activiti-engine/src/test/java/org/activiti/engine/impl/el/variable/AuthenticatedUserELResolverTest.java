/*
 * Copyright 2020 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.el.variable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.identity.Authentication;
import org.junit.AfterClass;
import org.junit.Test;

public class AuthenticatedUserELResolverTest {

    private static final String AUTHENTICATED_USER_KEY = "authenticatedUserId";

    private AuthenticatedUserELResolver resolver = new AuthenticatedUserELResolver();

    @AfterClass
    public static void tearDown() {
        Authentication.setAuthenticatedUserId(null);
    }

    @Test
    public void canResolve_should_returnTrueWhenPropertyIsAuthenticatedUser() {
        //when
        boolean canResolve = resolver.canResolve(AUTHENTICATED_USER_KEY, mock(VariableScope.class));
        //then
        assertThat(canResolve).isTrue();
    }

    @Test
    public void canResolve_should_returnFalseWhenPropertyIsNotAuthenticatedUser() {
        //when
        boolean canResolve = resolver.canResolve("anyOtherProperty", mock(VariableScope.class));
        //then
        assertThat(canResolve).isFalse();
    }

    @Test
    public void resolve_should_returnAuthenticatedUser() {
        //given
        Authentication.setAuthenticatedUserId("jane");

        //when
        Object result = resolver.resolve(AUTHENTICATED_USER_KEY, mock(VariableScope.class));

        //then
        assertThat(result).isEqualTo("jane");
    }
}
