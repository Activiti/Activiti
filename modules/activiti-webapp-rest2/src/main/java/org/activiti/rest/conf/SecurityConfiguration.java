package org.activiti.rest.conf;

import org.activiti.rest.security.BasicAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@EnableWebMvcSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
  
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
