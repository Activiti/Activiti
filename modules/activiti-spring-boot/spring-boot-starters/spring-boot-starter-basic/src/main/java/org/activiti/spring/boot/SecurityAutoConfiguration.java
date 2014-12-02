package org.activiti.spring.boot;

import org.activiti.engine.IdentityService;
import org.activiti.spring.security.IdentityServiceUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Installs a Spring Security adapter for the Activiti
 * {@link org.activiti.engine.IdentityService}.
 *
 * @author Josh Long
 */
@Configuration
@AutoConfigureBefore(org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class)
public class SecurityAutoConfiguration {

  @Configuration
  @ConditionalOnClass( UserDetailsService.class)
  public static class UserDetailsServiceConfiguration
          extends GlobalAuthenticationConfigurerAdapter {

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
      auth.userDetailsService( userDetailsService());
    }

    @Bean
    public UserDetailsService userDetailsService() {
      return new IdentityServiceUserDetailsService(this.identityService);
    }

    @Autowired
    private IdentityService identityService;
  }
}