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
angular.module('activitiApp').service('ProcessService', ['$http', '$q', '$rootScope', 'RelatedContentService',
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

        this.getProcessDefinitions = function(deploymentKey) {
            var url = ACTIVITI.CONFIG.contextRoot + '/app/rest/process-definitions?latest=true';
            if (deploymentKey) {
                url += '&deploymentKey=' + deploymentKey;
            }
            return httpAsPromise(
                {
                    method: 'GET',
                    url: url
                }
            );
        };

        this.createProcess = function(processData) {
            var deferred = $q.defer();
            $http({
                method: 'POST',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/process-instances',
                data: processData
            }).success(function (response, status, headers, config) {
                $rootScope.$broadcast('new-process-created', response);
                deferred.resolve(response);
            }).error(function (response, status, headers, config) {
                $rootScope.addAlert(response.message, 'error');
                deferred.reject(response);
            });

            var promise = deferred.promise;
            return promise;
        };

        this.deleteProcess = function(processInstanceId) {
            var deferred = $q.defer();
            $http({
                method: 'DELETE',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/process-instances/' + processInstanceId
            }).success(function (response, status, headers, config) {
                $rootScope.$broadcast('processinstance-deleted', response);
                deferred.resolve(response);
            }).error(function (response, status, headers, config) {
                $rootScope.addAlert(response.message, 'error');
                deferred.reject(response);
            });

            var promise = deferred.promise;
            return promise;
        }
    }]
);
