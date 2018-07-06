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
package org.activiti.spring.boot;

import org.activiti.engine.IdentityService;
import org.activiti.rest.security.BasicAuthenticationProvider;
import org.activiti.spring.security.IdentityServiceUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Installs a Spring Security adapter for the Activiti
 * {@link org.activiti.engine.IdentityService}.
 *
 * @author Josh Long
 */
@Configuration
@AutoConfigureBefore(org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
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
  
  @Configuration
  @ConditionalOnClass(name = {"org.activiti.rest.service.api.RestUrls", "org.springframework.web.servlet.DispatcherServlet"})
  @EnableWebSecurity
  public static class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    public AuthenticationProvider authenticationProvider() {
      return new BasicAuthenticationProvider();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
        .authenticationProvider(authenticationProvider())
        .csrf().disable()
        .authorizeRequests()
          .anyRequest().authenticated()
          .and()
        .httpBasic();
    }
  }
}