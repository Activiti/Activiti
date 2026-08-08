/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.spring.boot.variable;

import org.activiti.engine.cfg.ProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.variable.store.ByteArrayVariableContentStore;
import org.activiti.engine.impl.variable.store.VariableContentStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configures a {@link VariableContentStore} and registers it with the
 * {@link ProcessEngineConfigurationImpl}.
 *
 * <p>By default, registers the {@link ByteArrayVariableContentStore} which
 * preserves the existing ACT_GE_BYTEARRAY behaviour. To use a different store,
 * simply declare a {@link VariableContentStore} bean in your application context
 * (e.g. an S3VariableContentStore or FilesystemVariableContentStore).
 *
 * <p>The chosen store is then injected into the ProcessEngineConfiguration so
 * that {@link org.activiti.engine.impl.variable.ExternalStoreVariableType} is
 * registered (unless the ByteArrayVariableContentStore is used, which keeps the
 * classic path active).
 */
@AutoConfiguration
public class VariableStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(VariableContentStore.class)
    public VariableContentStore defaultVariableContentStore() {
        return new ByteArrayVariableContentStore();
    }

    @Bean
    public ProcessEngineConfigurator variableStoreEngineConfigurator(VariableContentStore variableContentStore) {
        return new VariableStoreProcessEngineConfigurator(variableContentStore);
    }
}
