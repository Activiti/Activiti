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

import org.activiti.core.common.spring.security.SimpleGrantedAuthoritiesResolver;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;
import java.util.List;


public class SimpleGrantedAuthoritiesResolverTest {

    private SimpleGrantedAuthoritiesResolver subject = new SimpleGrantedAuthoritiesResolver();

    @Test
    public void testGetAuthorities() {
        // given
        // given
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("GROUP_users",
                                                                                "ROLE_admin");

        Authentication authentication = new UsernamePasswordAuthenticationToken("user",
                                                                                "password",
                                                                                authorities);

        // when
        Collection<? extends GrantedAuthority> result = subject.getAuthorities(authentication);

        // then
        assertThat(result).isNotEmpty()
                          .asList()
                          .containsAll(authorities);
    }

}
