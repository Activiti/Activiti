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
package org.activiti.core.common.spring.security.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.core.common.spring.security.LocalSpringSecurityContextPrincipalProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Optional;


public class LocalSpringSecurityContextPrincipalProviderTest {

    private LocalSpringSecurityContextPrincipalProvider subject;

    @BeforeEach
    public void setUp() {
        subject = new LocalSpringSecurityContextPrincipalProvider();
    }

    @Test
    public void testGetCurrentPrincipalAuthenticated() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken("user",
                                                                                "password",
                                                                                AuthorityUtils.createAuthorityList("ROLE_user"));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        Optional<Principal> principal = subject.getCurrentPrincipal();

        // then
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(principal).isNotEmpty()
                             .contains(authentication);
    }

    @Test
    public void testGetCurrentPrincipalNotAuthenticated() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken("user",
                                                                                "password");

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        Optional<Principal> principal = subject.getCurrentPrincipal();

        // then
        assertThat(authentication.isAuthenticated()).isFalse();
        assertThat(principal).isEmpty();
    }

    @Test
    public void testGetCurrentPrincipalEmpty() {
        // given
        SecurityContextHolder.clearContext();

        // when
        Optional<Principal> principal = subject.getCurrentPrincipal();

        // then
        assertThat(principal).isEmpty();
    }

}
