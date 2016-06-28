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
angular.module('activitiApp').service('TaskService', ['$http', '$q', '$rootScope', 'RelatedContentService',
    function ($http, $q, $rootScope, RelatedContentService) {

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

        this.getProcessInstanceTasks = function(processInstanceId, isCompleted) {

            var data = {
                processInstanceId: processInstanceId
            };

            if (isCompleted) {
                data.state = 'completed'
            }

            return httpAsPromise(
                {
                    method: 'POST',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/query/tasks',
                    data: data
                }
            );

        };

        this.involveUserInTask = function(userId, taskId) {

            var involveData = {
                userId: userId
            };

            var promise = httpAsPromise(
                {
                    method: 'PUT',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/action/involve',
                    data: involveData
                }
            );

            return promise;
        };

        this.involveUserInTaskByEmail = function(email, taskId) {

            var involveData = {
                email: email
            };

            var promise = httpAsPromise(
                {
                    method: 'PUT',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/action/involve',
                    data: involveData
                }
            );

            return promise;
        };

        this.removeInvolvedUserInTask = function(user, taskId) {

            var removeInvolvedData = {};
            if (user.id !== null && user.id !== undefined) {
                removeInvolvedData.userId = user.id;
            } else {
                removeInvolvedData.email = user.email;
            }

            var promise = httpAsPromise(
                {
                    method: 'PUT',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/action/remove-involved',
                    data: removeInvolvedData
                }
            );

            return promise;
        };

        this.queryTasks = function(data) {
            return httpAsPromise(
                {
                    method: 'POST',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/query/tasks',
                    data: data
                }
            );
        };

        /**
         * Simple completion of a task without submitting form-data.
         */
        this.completeTask = function(taskId) {
            var deferred = $q.defer();
            $http( {
                method: 'PUT',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/action/complete'
            }).
            success(function (response, status, headers, config) {
                $rootScope.$broadcast('task-completed', {taskId: taskId});
                deferred.resolve(response);
            })
            .error(function (response, status, headers, config) {
                $rootScope.addAlert(response.message, 'error');
                deferred.reject(response);
            });

            var promise = deferred.promise;
            return promise;
        };

        this.claimTask = function(taskId) {
            var promise = httpAsPromise(
                {
                    method: 'PUT',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/action/claim'
                }
            );

            return promise;
        };

        this.getRelatedContent = function(taskId) {
            var deferred = $q.defer();
            $http({
                method: 'GET',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/content'
            }).
            success(function (response, status, headers, config) {
                // Add raw URL property to all content
                if(response && response.data) {
                    for(var i=0; i< response.data.length; i++) {
                        RelatedContentService.addUrlToContent(response.data[i]);
                    }
                }
                deferred.resolve(response);
            })
            .error(function (response, status, headers, config) {
                deferred.reject(response);
            });
            return deferred.promise;
        };

        this.assignTask = function(taskId, userId) {
            var assignData = {
                assignee: userId
            };

            var promise = httpAsPromise(
                {
                    method: 'PUT',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/action/assign',
                    data: assignData
                }
            );

            return promise;
        };

        this.assignTaskByEmail = function(taskId, email) {
            var promise = httpAsPromise(
                {
                    method: 'PUT',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/action/assign',
                    data: {email: email}
                }
            );

            return promise;
        };

        this.updateTask = function(taskId, data) {
            return httpAsPromise(
                {
                    method: 'PUT',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId,
                    data: data
                }
            );
        };

        this.createTask = function(taskData) {
            var deferred = $q.defer();
            $http({
                method: 'POST',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks',
                data: taskData
            }).
            success(function (response, status, headers, config) {
                $rootScope.$broadcast('new-task-created', response);
                deferred.resolve(response);
            })
            .error(function (response, status, headers, config) {
                $rootScope.addAlert(response.message, 'error');
                deferred.reject(response);
            });

            var promise = deferred.promise;
            return promise;
        };

        this.getProcessInstanceVariables = function(taskId) {
          var data = {
            taskId: taskId
          };

          return httpAsPromise(
            {
              method: 'GET',
              url: ACTIVITI.CONFIG.contextRoot + '/app/rest/task-forms/' + taskId + '/variables',
              data: data
            }
          );
        }
    }]);
