package org.activiti.spring.security.policies;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties
public class RuntimeTestConfiguraiton {

    @Bean
    public UserDetailsService myUserDetailsService() {
        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();

        List<GrantedAuthority> salaboyAuthorities = new ArrayList<>();
        salaboyAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        salaboyAuthorities.add(new SimpleGrantedAuthority("GROUP_activitiTeam"));
        salaboyAuthorities.add(new SimpleGrantedAuthority("GROUP_developers"));

        inMemoryUserDetailsManager.createUser(new User("salaboy", "password", salaboyAuthorities));

        List<GrantedAuthority> adminAuthorities = new ArrayList<>();
        adminAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_ADMIN"));

        inMemoryUserDetailsManager.createUser(new User("admin", "password", adminAuthorities));

        List<GrantedAuthority> garthAuthorities = new ArrayList<>();
        garthAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        garthAuthorities.add(new SimpleGrantedAuthority("GROUP_doctor"));

        inMemoryUserDetailsManager.createUser(new User("garth", "password", garthAuthorities));

        return inMemoryUserDetailsManager;
    }

}
