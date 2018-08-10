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

        List<GrantedAuthority> systemAuthorities = new ArrayList<>();
        systemAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        inMemoryUserDetailsManager.createUser(new User("system", "password", systemAuthorities));

        List<GrantedAuthority> adminAuthorities = new ArrayList<>();
        adminAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_ADMIN"));

        inMemoryUserDetailsManager.createUser(new User("admin", "password", adminAuthorities));

        return inMemoryUserDetailsManager;
    }


}
