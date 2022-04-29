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
package org.activiti.api.runtime.shared.security;

import java.util.List;

public interface SecurityManager {

    /**
     * Get currently authenticated user id from application security context
     *
     * @return currently authenticate user id or empty string if anonymous user
     *
     */
    String getAuthenticatedUserId();

    /**
     * Get group names for currently authenticated user from application security context
     *
     * @return list of group names the current user is member of
     *
     * @throws SecurityException if principal security context is not valid
     */
    List<String> getAuthenticatedUserGroups() throws SecurityException;

    /**
     * Get list of role names for currently authenticated user from application security context
     *
     * @return list of roles names or empty collection
     *
     * @throws SecurityException if principal security context is not valid
     */
    List<String> getAuthenticatedUserRoles() throws SecurityException;

}
