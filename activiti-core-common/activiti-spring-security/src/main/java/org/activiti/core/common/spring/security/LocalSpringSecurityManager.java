/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.core.common.spring.security;

import org.activiti.api.runtime.shared.security.AbstractSecurityManager;
import org.activiti.api.runtime.shared.security.PrincipalGroupsProvider;
import org.activiti.api.runtime.shared.security.PrincipalIdentityProvider;
import org.activiti.api.runtime.shared.security.PrincipalRolesProvider;
import org.activiti.api.runtime.shared.security.SecurityContextPrincipalProvider;
import org.springframework.lang.NonNull;

/*
 * This is a simple wrapper for Spring Security Context Holder
 */
public class LocalSpringSecurityManager extends AbstractSecurityManager {
    
    public LocalSpringSecurityManager(@NonNull SecurityContextPrincipalProvider securityContextPrincipalProvider,
                                      @NonNull PrincipalIdentityProvider principalIdentityProvider,
                                      @NonNull PrincipalGroupsProvider principalGroupsProvider,
                                      @NonNull PrincipalRolesProvider principalRolesProvider) {
        super(securityContextPrincipalProvider, 
              principalIdentityProvider, 
              principalGroupsProvider,
              principalRolesProvider);
    }

}
