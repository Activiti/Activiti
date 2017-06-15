/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.spring.security;


import org.activiti.engine.ActivitiException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Josh Long
 */
public class IdentityServiceUserDetailsService
        implements UserDetailsService {

    private final IdentityService identityService;

    public IdentityServiceUserDetailsService(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Override
    public UserDetails loadUserByUsername(String userId)
            throws UsernameNotFoundException {
        User user = null;
        try {
            user = this.identityService.createUserQuery()
                    .userId(userId)
                    .singleResult();
        } catch (ActivitiException ex) {
            // don't care
        }

        if (null == user) {
            throw new UsernameNotFoundException(
                    String.format("user (%s) could not be found", userId));
        }

        // if the results not null then its active...
        boolean active = true;

        // get the granted authorities
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<GrantedAuthority>();
        List<Group> groupsForUser = identityService
                .createGroupQuery()
                .groupMember(user.getId())
                .list();

        for (Group g : groupsForUser) {
            grantedAuthorityList.add(new GroupGrantedAuthority(g));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getId(),
                user.getPassword() ,
                active, active, active, active,
                grantedAuthorityList);
    }


    public static class GroupGrantedAuthority implements GrantedAuthority {
        private final Group group;

        public GroupGrantedAuthority(Group group) {
            this.group = group;
        }

        public Group getGroup() {
            return group;
        }

        @Override
        public String getAuthority() {
            return group.getName();
        }
    }


}
