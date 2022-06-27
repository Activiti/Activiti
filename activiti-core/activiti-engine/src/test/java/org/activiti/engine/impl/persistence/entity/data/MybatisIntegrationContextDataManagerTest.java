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
package org.activiti.engine.impl.persistence.entity.data;

import org.activiti.engine.impl.persistence.entity.data.integration.MybatisIntegrationContextDataManager;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntity;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntityImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MybatisIntegrationContextDataManagerTest {

    @InjectMocks
    private MybatisIntegrationContextDataManager manager;

    @Test
    public void createShouldReturnANewInstanceOfIntegrationContextEntityImpl() {
        //when
        IntegrationContextEntity entity = manager.create();

        //then
        assertThat(entity).isInstanceOf(IntegrationContextEntityImpl.class);
    }

    @Test
    public void getManagedEntityClassShouldReturnIntegrationContextEntityImpl() {
        //when
        Class<? extends IntegrationContextEntity> managedEntityClass = manager.getManagedEntityClass();

        //then
        assertThat(managedEntityClass).isEqualTo(IntegrationContextEntityImpl.class);
    }

}
