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
package org.activiti.core.common.spring.security;

import static java.util.Collections.emptyList;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

public class SimpleGrantedAuthoritiesResolver implements GrantedAuthoritiesResolver {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(@NonNull Principal principal) {
        return Optional.of(principal)
                .filter(this::isSupportedPrincipal)
                .map(this.getPrincipalClass()::cast)
                .map(this::getAuthorities)
                .orElseThrow(this::securityException);
    }

    protected SecurityException securityException() {
        return new SecurityException("Invalid principal authorities");
    }

    protected <T> Collection<? extends GrantedAuthority> getAuthorities(Authentication authentication) {
        return Optional.ofNullable(authentication.getAuthorities())
                       .orElseGet(this::emptyAuthorities);
    }

    protected <T> Collection<T> emptyAuthorities() {
        return emptyList();
    }

    protected Boolean isSupportedPrincipal(Principal principal) {
        return getPrincipalClass().isInstance(principal);
    }

    protected <T> Class<? extends Authentication> getPrincipalClass() {
        return Authentication.class;
    }
}
