package org.activiti.rest.conf.common;

import org.activiti.rest.security.BasicAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
      return super.authenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
      return new BasicAuthenticationProvider();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
        .authenticationProvider(authenticationProvider())
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .csrf().disable()
        .authorizeRequests()
          .anyRequest().authenticated()
          .and()
        .httpBasic();
    }
    
}
