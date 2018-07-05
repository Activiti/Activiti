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

package org.activiti.engine.integration;

import org.activiti.engine.impl.cmd.integration.DeleteIntegrationContextCmd;
import org.activiti.engine.impl.cmd.integration.RetrieveIntegrationContextsCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntity;

public class IntegrationContextServiceImpl implements IntegrationContextService {

    private CommandExecutor commandExecutor;

    public IntegrationContextServiceImpl(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public IntegrationContextEntity findById(String id) {
        return commandExecutor.execute(new RetrieveIntegrationContextsCmd(id));
    }

    @Override
    public void deleteIntegrationContext(IntegrationContextEntity integrationContextEntity) {
        commandExecutor.execute(new DeleteIntegrationContextCmd(integrationContextEntity));
    }
}
