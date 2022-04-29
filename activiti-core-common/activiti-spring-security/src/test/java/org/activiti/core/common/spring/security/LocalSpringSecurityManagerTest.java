/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.core.common.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.activiti.api.runtime.shared.security.SecurityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class LocalSpringSecurityManagerTest {

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private SecurityManager securityManager;

    @SpringBootApplication
    static class Application {

    }

    @Test
    public void contextLoads() {
        assertThat(securityManager).isInstanceOf(LocalSpringSecurityManager.class);
    }

    @Test
    @WithMockUser(username = "hruser", authorities = {"ROLE_user", "GROUP_users"})
    public void testGetAuthenticatedUserId() {

        // when
        String result = securityManager.getAuthenticatedUserId();

        // then
        assertThat(result).isEqualTo("hruser");
    }

    @Test
    @WithMockUser(username = "hruser", authorities = {"ROLE_user", "GROUP_users"})
    public void testGetAuthenticatedUserGroups() {

        // when
        List<String> result = securityManager.getAuthenticatedUserGroups();

        // then
        assertThat(result).containsExactly("users");
    }

    @Test
    @WithMockUser(username = "hruser", authorities = {"ROLE_user", "GROUP_users"})
    public void testGetAuthenticatedUserRoles() {

        // when
        List<String> result = securityManager.getAuthenticatedUserRoles();

        // then
        assertThat(result).containsExactly("user");
    }

    @Test
    @WithAnonymousUser
    public void testGetAuthenticatedUserIdAnonymous() {

        // when
        String result = securityManager.getAuthenticatedUserId();

        // then
        assertThat(result).isEqualTo("anonymous");
    }

    @Test
    public void testGetAuthenticatedUserIdInvalidUser() {
        // given
        SecurityContextHolder.clearContext();

        // when
        Throwable result = catchThrowable(() -> securityManager.getAuthenticatedUserId());

        // then
        assertThat(result).isInstanceOf(SecurityException.class);
    }

    @Test
    public void testGetAuthenticatedUserGroupInvalidUser() {
        // given
        SecurityContextHolder.clearContext();

        // when
        Throwable result = catchThrowable(() -> securityManager.getAuthenticatedUserGroups());

        // then
        assertThat(result).isInstanceOf(SecurityException.class);
    }

    @Test
    public void testGetAuthenticatedUserRolesInvalidUser() {
        // given
        SecurityContextHolder.clearContext();

        // when
        Throwable result = catchThrowable(() -> securityManager.getAuthenticatedUserRoles());

        // then
        assertThat(result).isInstanceOf(SecurityException.class);
    }

}
