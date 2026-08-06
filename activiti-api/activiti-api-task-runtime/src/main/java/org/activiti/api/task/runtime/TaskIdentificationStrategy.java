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
package org.activiti.api.task.runtime;

/**
 * Strategy used to identify the next task for the authenticated user.
 * <p>
 * Strategy behavior is documented per enum value.
 */
public enum TaskIdentificationStrategy {
    /**
     * Selection and claiming flow:
     * <ul>
     *   <li>Check oldest assigned tasks first.</li>
     *   <li>If no assigned task is found, evaluate candidate tasks (oldest first).</li>
     *   <li>Inspect up to 3 candidate tasks.</li>
     *   <li>Try to claim each candidate before returning it.</li>
     *   <li>If a claim fails with {@code ActivitiTaskAlreadyClaimedException}, continue with the next candidate.</li>
     * </ul>
     */
    CLAIM_BEFORE_OPEN_OLDEST_FIRST,
}
