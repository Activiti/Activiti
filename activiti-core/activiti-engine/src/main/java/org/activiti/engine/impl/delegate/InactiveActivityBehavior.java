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
package org.activiti.engine.impl.delegate;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * If the behaviour of an element in a process implements this interface, it has a 'background job' functionality.
 *
 * The instance will be called at the end of executing the engine operations for each {@link ExecutionEntity} that currently is at the activity AND is inactive.
 *

 */
@Internal
public interface InactiveActivityBehavior {

  void executeInactive(ExecutionEntity executionEntity);

}
