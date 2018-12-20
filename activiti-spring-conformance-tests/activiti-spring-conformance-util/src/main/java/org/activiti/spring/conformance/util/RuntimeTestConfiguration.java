package org.activiti.spring.conformance.util;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.core.common.spring.identity.ExtendedInMemoryUserDetailsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RuntimeTestConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeTestConfiguration.class);

    @Bean
    public UserDetailsService myUserDetailsService() {
        ExtendedInMemoryUserDetailsManager extendedInMemoryUserDetailsManager = new ExtendedInMemoryUserDetailsManager();

        List<GrantedAuthority> user1 = new ArrayList<>();
        user1.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        user1.add(new SimpleGrantedAuthority("GROUP_group1"));

        extendedInMemoryUserDetailsManager.createUser(new User("user1",
                "password",
                user1));

        List<GrantedAuthority> user2 = new ArrayList<>();
        user2.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        user2.add(new SimpleGrantedAuthority("GROUP_group2"));

        extendedInMemoryUserDetailsManager.createUser(new User("user2",
                "password",
                user2));

        List<GrantedAuthority> user3 = new ArrayList<>();
        user3.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        user3.add(new SimpleGrantedAuthority("GROUP_group1"));
        user3.add(new SimpleGrantedAuthority("GROUP_group2"));

        extendedInMemoryUserDetailsManager.createUser(new User("user3",
                "password",
                user3));

        List<GrantedAuthority> user4 = new ArrayList<>();
        user4.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));

        extendedInMemoryUserDetailsManager.createUser(new User("user4",
                "password",
                user4));


        List<GrantedAuthority> adminAuthorities = new ArrayList<>();
        adminAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_ADMIN"));

        extendedInMemoryUserDetailsManager.createUser(new User("admin",
                "password",
                adminAuthorities));


        return extendedInMemoryUserDetailsManager;
    }

}
