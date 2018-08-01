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

package org.activiti.runtime.api;

import org.activiti.runtime.api.conf.ProcessRuntimeConfiguration;
import org.activiti.runtime.api.model.*;
import org.activiti.runtime.api.model.payloads.*;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;

import java.util.List;

public interface ProcessAdminRuntime {
    

    /*
     * Get all process instances by pages
     * - Notice that only in-flight or suspended processes will be returned here
     * - For already completed process instance check at the query service
     */
    Page<ProcessInstance> processInstances(Pageable pageable);

    /*
     * Get all process instances by pages filtering by
     * - Notice that only in-flight or suspended processes will be returned here
     * - For already completed process instance check at the query service
     */
    Page<ProcessInstance> processInstances(Pageable pageable,
                                           GetProcessInstancesPayload getProcessInstancesPayload);

    /*
     * Get Process Instance by id
     */
    ProcessInstance processInstance(String processInstanceId);


    /*
     * Delete a Process Instance
     */
    ProcessInstance delete(DeleteProcessPayload deleteProcessPayload);


}
