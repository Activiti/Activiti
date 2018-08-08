/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.spring.identity;

import org.activiti.runtime.api.identity.UserGroupManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ActivitiUserGroupManagerImplIT {

    @Autowired
    private UserGroupManager userGroupManager;

    @Test
    public void onlyAdminShouldHaveAdminRole() throws Exception {
        assertThat(userGroupManager.getUserRoles("user")).doesNotContain("ACTIVITI_ADMIN");
        assertThat(userGroupManager.getUserRoles("admin")).contains("ACTIVITI_ADMIN");
    }

    @org.springframework.context.annotation.Configuration
    @ComponentScan("org.activiti.spring.identity")
    public static class Configuration {

    }
}
