/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.engine.test.api.tenant;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Model;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelLatestVersionWithTenantIdTest extends PluggableActivitiTestCase {
    private static final String FIRST_TENANT_ID = "firstTenantId";
    private static final String SECOND_TENANT_ID = "secondTenantId";
    private static final String THIRD_TENANT_ID = null;
    private static final String FOURTH_TENANT_ID = ProcessEngineConfiguration.NO_TENANT_ID;

    private static final String TEST_MODEL_KEY = "theModelKey";

    @Override
    protected void setUp() throws Exception {
        // same key version 1
        Model model = repositoryService.newModel();
        model.setKey(TEST_MODEL_KEY);
        model.setTenantId(FIRST_TENANT_ID);
        model.setVersion(1); // version 1
        repositoryService.saveModel(model);

        // same key  version 2
        model = repositoryService.newModel();
        model.setKey(TEST_MODEL_KEY);
        model.setTenantId(SECOND_TENANT_ID);
        model.setVersion(2); // version 2
        repositoryService.saveModel(model);

        // same key  version 3
        model = repositoryService.newModel();
        model.setKey(TEST_MODEL_KEY);
        model.setTenantId(THIRD_TENANT_ID);
        model.setVersion(3); // version 3
        repositoryService.saveModel(model);

        // same key  version 4
        model = repositoryService.newModel();
        model.setKey(TEST_MODEL_KEY);
        model.setTenantId(FOURTH_TENANT_ID);
        model.setVersion(4); // version 4
        repositoryService.saveModel(model);

        super.setUp();
    }

    public void testModelVersionTenancy() {

        // Check query
        assertThat(repositoryService.createModelQuery().modelKey(TEST_MODEL_KEY).latestVersion().modelTenantId(FIRST_TENANT_ID).singleResult().getVersion()).isEqualTo(1);
        assertThat(repositoryService.createModelQuery().modelKey(TEST_MODEL_KEY).latestVersion().modelTenantId(SECOND_TENANT_ID).singleResult().getVersion()).isEqualTo(2);
        assertThat(repositoryService.createModelQuery().modelKey(TEST_MODEL_KEY).latestVersion().modelWithoutTenantId().singleResult().getVersion()).isEqualTo(4);
        assertThat(repositoryService.createModelQuery().modelKey(TEST_MODEL_KEY).latestVersion().list().size()).isEqualTo(3);
        assertThat(repositoryService.createModelQuery().latestVersion().modelTenantIdLike("first%").list().size()).isEqualTo(1);
        assertThat(repositoryService.createModelQuery().latestVersion().modelTenantId("a%").list().size()).isEqualTo(0);

        // Clean up
        for (Model item : repositoryService.createModelQuery().list()) {
            repositoryService.deleteModel(item.getId());
        }
    }
}
