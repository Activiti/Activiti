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

import org.activiti.api.runtime.shared.security.PrincipalGroupsProvider;
import org.springframework.lang.NonNull;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public class AuthenticationPrincipalGroupsProvider implements PrincipalGroupsProvider {
    
    private final GrantedAuthoritiesResolver grantedAuthoritiesResolver;
    private final GrantedAuthoritiesGroupsMapper grantedAuthoritiesGroupsMapper;

    public AuthenticationPrincipalGroupsProvider(@NonNull GrantedAuthoritiesResolver grantedAuthoritiesResolver,
                                                  @NonNull GrantedAuthoritiesGroupsMapper grantedAuthoritiesGroupsMapper) {
        this.grantedAuthoritiesResolver = grantedAuthoritiesResolver;
        this.grantedAuthoritiesGroupsMapper = grantedAuthoritiesGroupsMapper;
    }
    
    @Override
    public List<String> getGroups(@NonNull Principal principal) {
        return Optional.of(principal)
                       .map(grantedAuthoritiesResolver::getAuthorities)
                       .map(grantedAuthoritiesGroupsMapper::getGroups)
                       .orElseThrow(this::securityException);
    }

    protected SecurityException securityException() {
        return new SecurityException("Invalid principal groups");
    }    

}
