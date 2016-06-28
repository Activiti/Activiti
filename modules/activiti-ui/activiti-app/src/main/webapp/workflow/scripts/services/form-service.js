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

// Task service
angular.module('activitiApp').service('FormService', ['$http', '$q',
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

        this.getTaskForm = function(taskId) {

            return httpAsPromise(
                {
                    method: 'GET',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/task-forms/' + taskId
                }
            );
        };

        this.completeTaskForm = function(taskId, data) {

            var promise = httpAsPromise(
                {
                    method: 'POST',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/task-forms/' + taskId,
                    data: data
                }
            );

            return promise;
        };
        
        this.saveTaskForm = function(taskId, data) {

            var promise = httpAsPromise(
                {
                    method: 'POST',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/task-forms/' + taskId + '/save-form',
                    data: data
                }
            );

            return promise;
        };

        this.getStartForm = function(processDefinitionId) {

            return httpAsPromise(
                {
                    method: 'GET',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/process-definitions/' + processDefinitionId + "/start-form"
                }
            );
        };

        this.completeStartForm = function(data) {

            var promise = httpAsPromise(
                {
                    method: 'POST',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/process-instances/',
                    data: data
                }
            );

            return promise;
        };



    }]);
