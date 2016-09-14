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
package org.activiti.app.web;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Custom implementation of {@link FormLoginConfigurer}, to have control over the auth filter instance.
 */
public final class CustomFormLoginConfig<H extends HttpSecurityBuilder<H>> extends AbstractAuthenticationFilterConfigurer<H,CustomFormLoginConfig<H>,CustomUsernamePasswordAuthenticationFilter> {

    public CustomFormLoginConfig() {
        super(new CustomUsernamePasswordAuthenticationFilter(), null);
        getAuthenticationFilter().setPasswordParameter("username");
        getAuthenticationFilter().setPasswordParameter("password");
    }
    
    public CustomFormLoginConfig<H> usernameParameter(String usernameParameter) {
        getAuthenticationFilter().setUsernameParameter(usernameParameter);
        return this;
    }

    public CustomFormLoginConfig<H> passwordParameter(String passwordParameter) {
        getAuthenticationFilter().setPasswordParameter(passwordParameter);
        return this;
    }

    @Override
    public void init(H http) throws Exception {
        super.init(http);
        initDefaultLoginFilter(http);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer#createLoginProcessingUrlMatcher(java.lang.String)
     */
    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new CustomAntPathMatcher(loginProcessingUrl, "POST");
    }

    /**
     * If available, initializes the {@link DefaultLoginPageGeneratingFilter} shared object.
     *
     * @param http the {@link HttpSecurityBuilder} to use
     */
    private void initDefaultLoginFilter(H http) {
        DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = http.getSharedObject(DefaultLoginPageGeneratingFilter.class);
        if(loginPageGeneratingFilter != null && !isCustomLoginPage()) {
            loginPageGeneratingFilter.setFormLoginEnabled(true);
            loginPageGeneratingFilter.setUsernameParameter(getAuthenticationFilter().getUsernameParameter());
            loginPageGeneratingFilter.setPasswordParameter(getAuthenticationFilter().getPasswordParameter());
            loginPageGeneratingFilter.setLoginPageUrl(getLoginPage());
            loginPageGeneratingFilter.setFailureUrl(getFailureUrl());
            loginPageGeneratingFilter.setAuthenticationUrl(getLoginProcessingUrl());
        }
    }
}

