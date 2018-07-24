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

package org.activiti.cloud.services.identity.basic;

import java.util.ArrayList;
import java.util.List;

import org.activiti.runtime.api.identity.IdentityLookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class BasicIdentityLookup implements IdentityLookup {

    private UserDetailsService userDetailsServiceBean;

    @Autowired
    public BasicIdentityLookup(UserDetailsService userDetailsServiceBean) {
        this.userDetailsServiceBean = userDetailsServiceBean;
    }

    @Override
    public List<String> getGroupsForCandidateUser(String candidateUser) {
        List<String> groups = new ArrayList<>();

        UserDetails userDetails = userDetailsServiceBean.loadUserByUsername(candidateUser);
        if (userDetails != null) {
            for (GrantedAuthority authority : userDetails.getAuthorities()) {
                groups.add(authority.getAuthority());
            }
        }

        return groups;
    }
}
