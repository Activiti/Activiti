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

/***
 * Adapts Spring Security to the Activiti {@link org.activiti.engine.IdentityService}.
 * If you've already wired Activiti to a backend identity store then this is a convenient
 * way to tie the security for the other tiers of the application through common Spring Integration facilities.
 *
 * @author Josh Long
 */
public class IdentityServiceUserDetailsService
        implements UserDetailsService {

    private final IdentityService identityService;

    public IdentityServiceUserDetailsService(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = null;
        try {
            user = this.identityService.createUserQuery()
                    .userEmail(username)
                    .singleResult();
        } catch (ActivitiException ex) {
            // don't care
        }

        if (null == user) {
            throw new UsernameNotFoundException(String.format("user (%s) could not be found", username));
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
                user.getEmail(),
                user.getPassword(),
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
