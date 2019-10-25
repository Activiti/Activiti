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

import org.activiti.api.runtime.shared.security.PrincipalRolesProvider;
import org.springframework.lang.NonNull;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public class AuthenticationPrincipalRolesProvider implements PrincipalRolesProvider {
    
    private final GrantedAuthoritiesResolver grantedAuthoritiesResolver;
    private final GrantedAuthoritiesRolesMapper grantedAuthoritiesRolesMapper;

    public AuthenticationPrincipalRolesProvider(@NonNull GrantedAuthoritiesResolver grantedAuthoritiesResolver,
                                                @NonNull GrantedAuthoritiesRolesMapper grantedAuthoritiesRolesMapper) {
        this.grantedAuthoritiesResolver = grantedAuthoritiesResolver;
        this.grantedAuthoritiesRolesMapper = grantedAuthoritiesRolesMapper;
    }
    
    @Override
    public List<String> getRoles(@NonNull Principal principal) {
        return Optional.of(principal)
                       .map(grantedAuthoritiesResolver::getAuthorities)
                       .map(grantedAuthoritiesRolesMapper::getRoles)
                       .orElseThrow(this::securityException);
    }
    
    protected SecurityException securityException() {
        return new SecurityException("Invalid principal rolese");
    }    
    
}
