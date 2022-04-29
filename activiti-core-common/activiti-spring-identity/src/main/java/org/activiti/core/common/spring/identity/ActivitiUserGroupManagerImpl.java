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
package org.activiti.core.common.spring.identity;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.stream.Collectors;

public class ActivitiUserGroupManagerImpl implements UserGroupManager {

    private final UserDetailsService userDetailsService;

    public ActivitiUserGroupManagerImpl(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public List<String> getUserGroups(String username) {

        return userDetailsService.loadUserByUsername(username).getAuthorities().stream()
                .filter((GrantedAuthority a) -> a.getAuthority().startsWith("GROUP_"))
                .map((GrantedAuthority a) -> a.getAuthority().substring(6))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserRoles(String username) {
        return userDetailsService.loadUserByUsername(username).getAuthorities().stream()
                .filter((GrantedAuthority a) -> a.getAuthority().startsWith("ROLE_"))
                .map((GrantedAuthority a) -> a.getAuthority().substring(5))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getGroups() {
        return ((ExtendedInMemoryUserDetailsManager) userDetailsService).getGroups();
    }

    @Override
    public List<String> getUsers() {
        return ((ExtendedInMemoryUserDetailsManager) userDetailsService).getUsers();
    }
}
