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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

public class ExtendedInMemoryUserDetailsManager extends InMemoryUserDetailsManager {

    private List<String> users = new ArrayList<>();
    private List<String> groups = new ArrayList<>();

    @Override
    public void createUser(UserDetails user) {
        super.createUser(user);
        users.add(user.getUsername());
        groups = user.getAuthorities().stream()
                .filter(x -> (x.getAuthority().contains("GROUP")))
                .map(x -> (x.getAuthority()))
                .collect(Collectors.toList());
    }

    public List<String> getUsers() {
        return users;
    }

    public List<String> getGroups() {
        return groups;
    }
}
