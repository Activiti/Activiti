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
package org.activiti.api.runtime.shared.security;

import java.util.List;

public abstract class AbstractSecurityManager implements SecurityManager {

    private static final String INVALID_AUTHENTICATED_PRINCIPAL = "Invalid authenticated principal";

    private final SecurityContextPrincipalProvider securityContextPrincipalProvider;
    private final PrincipalIdentityProvider principalIdentityProvider;
    private final PrincipalGroupsProvider principalGroupsProvider;
    private final PrincipalRolesProvider principalRolesProvider;

    public AbstractSecurityManager(SecurityContextPrincipalProvider securityContextPrincipalProvider,
                                   PrincipalIdentityProvider principalIdentityProvider,
                                   PrincipalGroupsProvider principalGroupsProvider,
                                   PrincipalRolesProvider principalRolesProvider) {
        this.securityContextPrincipalProvider = securityContextPrincipalProvider;
        this.principalIdentityProvider = principalIdentityProvider;
        this.principalGroupsProvider = principalGroupsProvider;
        this.principalRolesProvider = principalRolesProvider;
    }

    @Override
    public String getAuthenticatedUserId() {
        return securityContextPrincipalProvider.getCurrentPrincipal()
                                               .map(principalIdentityProvider::getUserId)
                                               .orElseThrow(this::securityException);
    }

    @Override
    public List<String> getAuthenticatedUserGroups() {
        return securityContextPrincipalProvider.getCurrentPrincipal()
                                               .map(principalGroupsProvider::getGroups)
                                               .orElseThrow(this::securityException);
    }

    @Override
    public List<String> getAuthenticatedUserRoles() {
        return securityContextPrincipalProvider.getCurrentPrincipal()
                                               .map(principalRolesProvider::getRoles)
                                               .orElseThrow(this::securityException);
    }

    protected SecurityException securityException() {
        return new SecurityException(INVALID_AUTHENTICATED_PRINCIPAL);
    }

}
