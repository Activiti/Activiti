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
package org.activiti.core.common.spring.identity;

import java.util.List;

import org.activiti.core.common.spring.identity.ExtendedInMemoryUserDetailsManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ExtendedInMemoryUserDetailsManagerIT {

    @Autowired
    private ExtendedInMemoryUserDetailsManager extendedInMemoryUserDetailsManager;

    @Test
    public void checkGroupAuthorities() {
        List<String> groups = extendedInMemoryUserDetailsManager.getGroups();
        assertThat(groups).isNotNull();
        groups.stream().forEach(x -> assertThat(x).contains("GROUP"));
    }

    @Test
    public void checkUsers() {
        List<String> users = extendedInMemoryUserDetailsManager.getUsers();
        assertThat(users).isNotNull();
        assertThat(users.size() > 1).isTrue();
        String adminUser = users.stream().filter(x -> x.equals("admin")).findFirst().get();
        assertThat(adminUser).isNotNull();
    }
}
