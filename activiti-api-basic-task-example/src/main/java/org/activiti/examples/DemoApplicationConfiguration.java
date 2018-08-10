package org.activiti.examples;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DemoApplicationConfiguration {

    @Bean
    public UserDetailsService myUserDetailsService() {
        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();

        List<GrantedAuthority> salaboyAuthorities = new ArrayList<>();
        salaboyAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        salaboyAuthorities.add(new SimpleGrantedAuthority("GROUP_activitiTeam"));
        inMemoryUserDetailsManager.createUser(new User("salaboy", "password", salaboyAuthorities));

        List<GrantedAuthority> ryanAuthorities = new ArrayList<>();
        ryanAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        ryanAuthorities.add(new SimpleGrantedAuthority("GROUP_activitiTeam"));
        inMemoryUserDetailsManager.createUser(new User("ryandawsonuk", "password", ryanAuthorities));

        List<GrantedAuthority> eliasAuthorities = new ArrayList<>();
        eliasAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        eliasAuthorities.add(new SimpleGrantedAuthority("GROUP_activitiTeam"));
        inMemoryUserDetailsManager.createUser(new User("erdemedeiros", "password", eliasAuthorities));

        List<GrantedAuthority> otherAuthorities = new ArrayList<>();
        otherAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        otherAuthorities.add(new SimpleGrantedAuthority("GROUP_otherTeam"));
        inMemoryUserDetailsManager.createUser(new User("other", "password", otherAuthorities));

        List<GrantedAuthority> adminAuthorities = new ArrayList<>();
        adminAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_ADMIN"));

        inMemoryUserDetailsManager.createUser(new User("admin", "password", adminAuthorities));

        return inMemoryUserDetailsManager;
    }


}
