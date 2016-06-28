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

// Comment service
angular.module('activitiApp').service('CommentService', ['$http', '$q',
    function ($http, $q) {

        /*
         * Get all comments on a task
         */
        this.getTaskComments = function (taskId) {

            var deferred = $q.defer();

            $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/comments', params: {latestFirst: true}}).
                success(function (response, status, headers, config) {
                    deferred.resolve(response);
                })
                .error(function (response, status, headers, config) {
                    deferred.reject(response);
                });

            return deferred.promise;
        };

        /*
         * Create a new comment on a task
         */
        this.createTaskComment = function (taskId, message) {
            var deferred = $q.defer();

            var data = {message: message};
            $http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/comments', data: data}).
                success(function (response, status, headers, config) {
                    deferred.resolve(response);
                })
                .error(function (response, status, headers, config) {
                    deferred.reject(response);
                });

            var promise = deferred.promise;
            return promise;
        };
        
        /*
         * Get all comments on a process instance
         */
        this.getProcessInstanceComments = function (processInstanceId) {

            var deferred = $q.defer();

            $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/process-instances/' + processInstanceId + '/comments', params: {latestFirst: true}}).
                success(function (response, status, headers, config) {
                    deferred.resolve(response);
                })
                .error(function (response, status, headers, config) {
                    deferred.reject(response);
                });

            return deferred.promise;
        };

        /*
         * Create a new comment on a process instance
         */
        this.createProcessInstanceComment = function (processInstanceId, message) {
            var deferred = $q.defer();

            var data = {message: message};
            $http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/process-instances/' + processInstanceId + '/comments', data: data}).
                success(function (response, status, headers, config) {
                    deferred.resolve(response);
                })
                .error(function (response, status, headers, config) {
                    deferred.reject(response);
                });

            var promise = deferred.promise;
            return promise;
        };

    }]);
