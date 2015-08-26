/*
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
'use strict';

// User service
activitiModule.service('UserService', ['$http', '$q',
    function ($http, $q) {

        var httpAsPromise = function(options) {
            var deferred = $q.defer();
            $http(options).
                success(function (response, status, headers, config) {
                    deferred.resolve(response);
                })
                .error(function (response, status, headers, config) {
                    deferred.reject(response);
                });
            return deferred.promise;
        };

        /*
         * Filter users based on a filter text, in the context of workflow: for tasks, processes, etc.
         */
        this.getFilteredUsers = function (filterText, taskId, processInstanceId, tenantId, group) {
            var params = {};
            if (typeof filterText === 'string') {
               params.filter = filterText;
            }
            else {
               // Could be i.e. { email: 'user@domain.com' } or { externalId: 'externalUserId' }
               params = filterText;
            }
            if(taskId) {
                params.excludeTaskId = taskId;
            }
            if (processInstanceId) {
                params.excludeProcessId = processInstanceId;
            }

            if (group && group.id) {
                params.groupId = group.id;
            }

            return httpAsPromise({
                method: 'GET',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/workflow-users',
                params: params
            });
        };

        /*
         * Filter users based on a filter text, in the context of IDM: use no context (contrary to the getFilteredUsers method).
         */
        this.getFilteredUsersStrict = function(filterText, tenantId, group) {

            var params = {};

            params.status = 'active';

            if (filterText !== null && filterText !== undefined) {
                params.filter = filterText;
            }

            if (group && group.id) {
                params.groupId = group.id;
            }

            return httpAsPromise(
                {
                    method: 'GET',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/users',
                    params: params
                }
            )
        };

        /*
         * Get all recent users
         */
        this.getRecentUsers = function (username, taskId) {
            var params = {};
            if(taskId) {
                params.excludeTaskId = taskId;
            }

            return httpAsPromise(
                {
                    method: 'GET',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/workflow-users/' + username + '/recent-users',
                    params: params
                }
            );
        };

    }]);
