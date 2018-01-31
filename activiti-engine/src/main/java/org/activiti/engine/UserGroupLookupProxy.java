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

package org.activiti.engine;

import java.util.List;

/*
 * Proxy for class that will be used to supply groups for a user.
 * User's groups will then be used to match on queries for which user is a member of group and is therefore candidate for task.
 * Concrete implementation to be set in ProcessEngineConfiguration.
 */
public interface UserGroupLookupProxy {

    List<String> getGroupsForCandidateUser(String candidateUser);
}
