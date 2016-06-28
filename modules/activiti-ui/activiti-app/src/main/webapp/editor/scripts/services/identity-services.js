/* Licensed under the Apache License, Version 2.0 (the "License");
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
'use strict';

angular.module('activitiModeler').service('UserService', ['$http', '$q',
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
         * Filter users based on a filter text.
         */
        this.getFilteredUsers = function (filterText, taskId, processInstanceId) {
            var params = {filter: filterText};
            if(taskId) {
                params.excludeTaskId = taskId;
            }
            if (processInstanceId) {
                params.exclusdeProcessId = processInstanceId;
            }

            return httpAsPromise({
                method: 'GET',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/workflow-users',
                params: params
            });
        };

    }]);

angular.module('activitiModeler').service('GroupService', ['$http', '$q',
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
         * Filter functional groups based on a filter text.
         */
        this.getFilteredGroups = function (filterText) {
            var params;
            if(filterText) {
                params = {filter: filterText};
            }

            return httpAsPromise({
                method: 'GET',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/editor-groups',
                params: params
            });
        };
    }]);
