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
package com.activiti.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import com.activiti.api.security.AlfrescoPasswordEncoderOverride;
import com.activiti.api.security.AlfrescoSecurityConfigOverride;
import com.activiti.api.security.AlfrescoUserDetailsServiceOverride;
import com.activiti.api.security.AlfrescoWebAppSecurityOverride;
import com.activiti.security.AjaxAuthenticationFailureHandler;
import com.activiti.security.AjaxAuthenticationSuccessHandler;
import com.activiti.security.AjaxLogoutSuccessHandler;
import com.activiti.security.Http401UnauthorizedEntryPoint;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);
    
    // Pluggability points to override the default security configuration
    
    @Autowired(required=false)
    protected AlfrescoSecurityConfigOverride securityConfigOverride;
    
    @Autowired(required=false)
    protected AlfrescoUserDetailsServiceOverride userDetailsServiceOverride;
    
    @Autowired(required=false)
    protected AlfrescoPasswordEncoderOverride passwordEncoderOverride;

    //
    // GLOBAL CONFIG
    //

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        if (securityConfigOverride != null) {
            securityConfigOverride.configureGlobal(auth, userDetailsService());
            return;
        }
        
        try {
            auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
        } catch (Exception e) {
            logger.error("Could not configure authentication mechanism:", e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        if (passwordEncoderOverride != null) {
            return passwordEncoderOverride.createPasswordEncoder();
        }
        
        return new StandardPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        if (userDetailsServiceOverride != null) {
            return userDetailsServiceOverride.createUserDetailsService();
        }
        
        return new com.activiti.security.UserDetailsService();
    }

    //
    // FORM LOGIN (Cookie based)
    //

    @Configuration
    @Order(10) // API config first (has Order(1))
    public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private Environment env;

        @Autowired
        private AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler;

        @Autowired
        private AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;

        @Autowired
        private AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler;

        @Autowired
        private Http401UnauthorizedEntryPoint authenticationEntryPoint;
        
        @Autowired(required=false)
        protected AlfrescoWebAppSecurityOverride webAppSecurityOverride;

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring()
                .antMatchers("/bower_components/**")
                .antMatchers("/additional_components/**")
                .antMatchers("/fonts/**")
                .antMatchers("/images/**")
                .antMatchers("/scripts/**")
                .antMatchers("/styles/**")
                .antMatchers("/views/**");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            if (webAppSecurityOverride != null) {
                webAppSecurityOverride.configure(http);
                return;
            }
            
            http
                .exceptionHandling()
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .and()
                .formLogin()
                    .loginProcessingUrl("/app/authentication")
                    .successHandler(ajaxAuthenticationSuccessHandler)
                    .failureHandler(ajaxAuthenticationFailureHandler)
                    .usernameParameter("j_username")
                    .passwordParameter("j_password")
                    .permitAll()
                    .and()
                .logout()
                    .logoutUrl("/app/logout")
                    .logoutSuccessHandler(ajaxLogoutSuccessHandler)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
                    .and()
                .csrf()
                    .disable()
                .authorizeRequests()
                    .antMatchers("/*").permitAll()
                    .antMatchers("/app/rest/authenticate").permitAll()
                    .antMatchers("/app/rest/logs/**").hasAnyRole("ADMIN")
                    .antMatchers("/app/**").hasAnyRole("USER", "ADMIN")
                    .antMatchers("/metrics/**").hasAnyRole("ADMIN");
        }
    }

    //
    // BASIC AUTH
    //

    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        protected void configure(HttpSecurity http) throws Exception {
            http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf()
                .disable()
                .antMatcher("/api/**")
                .authorizeRequests()
                .antMatchers("/api/**").hasAnyRole("CLUSTER_MANAGER", "ADMIN")
                .and().httpBasic();
        }
    }
}
