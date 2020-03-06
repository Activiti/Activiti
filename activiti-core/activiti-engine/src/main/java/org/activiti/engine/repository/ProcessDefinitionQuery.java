/*
 * Copyright 2020 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.repository;

import java.util.Set;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.query.Query;

/**
 * Allows programmatic querying of {@link ProcessDefinition}s.
 */
@Internal
public interface ProcessDefinitionQuery extends Query<ProcessDefinitionQuery, ProcessDefinition> {

    /**
     * Only select process definition with the given id.
     */
    ProcessDefinitionQuery processDefinitionId(String processDefinitionId);

    /**
     * Only select process definitions with the given ids.
     */
    ProcessDefinitionQuery processDefinitionIds(Set<String> processDefinitionIds);

    /**
     * Only select process definitions with the given category.
     */
    ProcessDefinitionQuery processDefinitionCategory(String processDefinitionCategory);

    /**
     * Only select process definitions where the category matches the given parameter. The syntax that should be used is the same as in SQL, eg. %activiti%
     */
    ProcessDefinitionQuery processDefinitionCategoryLike(String processDefinitionCategoryLike);

    /**
     * Only select deployments that have a different category then the given one.
     *
     * @see DeploymentBuilder#category(String)
     */
    ProcessDefinitionQuery processDefinitionCategoryNotEquals(String categoryNotEquals);

    /**
     * Only select process definitions with the given name.
     */
    ProcessDefinitionQuery processDefinitionName(String processDefinitionName);

    /**
     * Only select process definitions where the name matches the given parameter. The syntax that should be used is the same as in SQL, eg. %activiti%
     */
    ProcessDefinitionQuery processDefinitionNameLike(String processDefinitionNameLike);

    /**
     * Only select process definitions that are deployed in a deployment with the given deployment id
     */
    ProcessDefinitionQuery deploymentId(String deploymentId);

    /**
     * Select process definitions that are deployed in deployments with the given set of ids
     */
    ProcessDefinitionQuery deploymentIds(Set<String> deploymentIds);

    /**
     * Only select process definition with the given key.
     */
    ProcessDefinitionQuery processDefinitionKey(String processDefinitionKey);

    /**
     * Only select process definition with the given keys.
     */
    ProcessDefinitionQuery processDefinitionKeys(Set<String> processDefinitionKeys);

    /**
     * Only select process definitions where the key matches the given parameter. The syntax that should be used is the same as in SQL, eg. %activiti%
     */
    ProcessDefinitionQuery processDefinitionKeyLike(String processDefinitionKeyLike);

    /**
     * Only select process definition with a certain version. Particulary useful when used in combination with {@link #processDefinitionKey(String)}
     */
    ProcessDefinitionQuery processDefinitionVersion(Integer processDefinitionVersion);

    /**
     * Only select process definitions which version are greater than a certain version.
     */
    ProcessDefinitionQuery processDefinitionVersionGreaterThan(Integer processDefinitionVersion);

    /**
     * Only select process definitions which version are greater than or equals a certain version.
     */
    ProcessDefinitionQuery processDefinitionVersionGreaterThanOrEquals(Integer processDefinitionVersion);

    /**
     * Only select process definitions which version are lower than a certain version.
     */
    ProcessDefinitionQuery processDefinitionVersionLowerThan(Integer processDefinitionVersion);

    /**
     * Only select process definitions which version are lower than or equals a certain version.
     */
    ProcessDefinitionQuery processDefinitionVersionLowerThanOrEquals(Integer processDefinitionVersion);

    /**
     * Only select the process definitions which are the latest deployed (ie. which have the highest version number for the given key).
     * <p>
     * Can also be used without any other criteria (ie. query.latest().list()),
     * which will then give all the latest versions of all the deployed process definitions.
     *
     * @throws ActivitiIllegalArgumentException if used in combination with {@link #groupId(string)}, {@link #processDefinitionVersion(int)} or {@link #deploymentId(String)}
     */
    ProcessDefinitionQuery latestVersion();

    /**
     * Only select process definition with the given resource name.
     */
    ProcessDefinitionQuery processDefinitionResourceName(String resourceName);

    /**
     * Only select process definition with a resource name like the given .
     */
    ProcessDefinitionQuery processDefinitionResourceNameLike(String resourceNameLike);

    /**
     * Only selects process definitions which given userId is authoriezed to start
     */
    ProcessDefinitionQuery startableByUser(String userId);

    /**
     * Only selects process definitions which are suspended
     */
    ProcessDefinitionQuery suspended();

    /**
     * Only selects process definitions which are active
     */
    ProcessDefinitionQuery active();

    /**
     * Only select process definitions that have the given tenant id.
     */
    ProcessDefinitionQuery processDefinitionTenantId(String tenantId);

    /**
     * Only select process definitions with a tenant id like the given one.
     */
    ProcessDefinitionQuery processDefinitionTenantIdLike(String tenantIdLike);

    /**
     * Only select process definitions that do not have a tenant id.
     */
    ProcessDefinitionQuery processDefinitionWithoutTenantId();

    // Support for event subscriptions /////////////////////////////////////

    /**
     * Selects the single process definition which has a start message event with the messageName.
     */
    ProcessDefinitionQuery messageEventSubscriptionName(String messageName);

    // ordering ////////////////////////////////////////////////////////////

    /**
     * Order by the category of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}).
     */
    ProcessDefinitionQuery orderByProcessDefinitionCategory();

    /**
     * Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}).
     */
    ProcessDefinitionQuery orderByProcessDefinitionKey();

    /**
     * Order by the id of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}).
     */
    ProcessDefinitionQuery orderByProcessDefinitionId();

    /**
     * Order by the version of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}).
     */
    ProcessDefinitionQuery orderByProcessDefinitionVersion();

    /**
     * Order by the app version of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}).
     */
    ProcessDefinitionQuery orderByProcessDefinitionAppVersion();

    /**
     * Order by the name of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}).
     */
    ProcessDefinitionQuery orderByProcessDefinitionName();

    /**
     * Order by deployment id (needs to be followed by {@link #asc()} or {@link #desc()}).
     */
    ProcessDefinitionQuery orderByDeploymentId();

    /**
     * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
     */
    ProcessDefinitionQuery orderByTenantId();

}
