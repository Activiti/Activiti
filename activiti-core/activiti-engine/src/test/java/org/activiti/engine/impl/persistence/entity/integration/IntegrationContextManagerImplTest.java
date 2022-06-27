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
package org.activiti.engine.impl.persistence.entity.integration;

import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.integration.IntegrationContextDataManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class IntegrationContextManagerImplTest {

    @InjectMocks
    private IntegrationContextManagerImpl manager;

    @Mock
    private IntegrationContextDataManager dataManager;

    @Test
    public void getDataManagerShouldReturnIntegrationContextDataManager() {
        //when
        DataManager<IntegrationContextEntity> retrievedDataManager = manager.getDataManager();

        //then
        assertThat(retrievedDataManager).isEqualTo(dataManager);
    }

    @Test
    public void findByIdShouldReturnResultFromDataManager() {
        //given
        IntegrationContextEntity entity = mock(IntegrationContextEntity.class);
        given(dataManager.findById("id")).willReturn(entity);

        //when
        IntegrationContextEntity retrievedResult = manager.findById("id");

        //then
        assertThat(retrievedResult).isEqualTo(entity);
    }

}
