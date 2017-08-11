/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.services.identity.basic;

import org.activiti.engine.UserGroupLookupProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.List;

public class BasicUserGroupLookupProxy  implements UserGroupLookupProxy {

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public List<String> getGroupsForCandidateUser(String candidateUser) {
        List<String> groups = new ArrayList<>();

        UserDetails userDetails = userDetailsService.loadUserByUsername(candidateUser);
        for(GrantedAuthority authority:userDetails.getAuthorities()){
            groups.add(authority.getAuthority());
        }

        return groups;
    }
}
