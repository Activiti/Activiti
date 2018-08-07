package org.activiti.spring.boot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.runtime.api.connector.Connector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class RuntimeTestConfiguration {

    public static boolean processImageConnectorExecuted = false;

    public static boolean tagImageConnectorExecuted = false;

    public static boolean discardImageConnectorExecuted = false;


    @Bean
    public UserDetailsService myUserDetailsService() {
        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();

        List<GrantedAuthority> salaboyAuthorities = new ArrayList<>();
        salaboyAuthorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        salaboyAuthorities.add(new SimpleGrantedAuthority("GROUP_activitiTeam"));

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

    @Bean
    public Connector processImageConnector() {
        return integrationContext -> {
            Map<String, Object> inBoundVariables = integrationContext.getInBoundVariables();
            System.out.println("My inbound variables keys: " + inBoundVariables.keySet());
            System.out.println("My inbound variables values: " + inBoundVariables.values());
            boolean expectedValue = (Boolean) inBoundVariables.get("expectedKey");

            integrationContext.addOutBoundVariable("approved",
                                                   expectedValue);
            processImageConnectorExecuted = true;
            return integrationContext;
        };
    }

    @Bean
    public Connector tagImageConnector() {
        return integrationContext -> {
            tagImageConnectorExecuted = true;
            return integrationContext;
        };
    }

    @Bean
    public Connector discardImageConnector() {
        return integrationContext -> {
            discardImageConnectorExecuted = true;
            return integrationContext;
        };
    }
}
