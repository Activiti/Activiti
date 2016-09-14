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
angular.module('activitiApp').service('AppDefinitionService', ['$http', '$q', '$rootScope', '$location', '$translate',
    function ($http, $q, $rootScope, $location, $translate) {

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


        /**
         * Set the deployment key of the app that should be used. In case the app definition
         * is not yet cached, it will be fetched.
         *
         * Updating this value will be reflected in the header, to make user aware of a change of context.
         */
        this.setActiveDeploymentKey = function(deploymentKey) {
            if (deploymentKey) {
                if ($rootScope.appDefinitions[deploymentKey]) {
                    $rootScope.activeAppDefinition = $rootScope.appDefinitions[deploymentKey];
                } else {
                    // Add placeholder with ID only, and fetch the actual object
                    $rootScope.activeAppDefinition = {id: deploymentKey};
                    $rootScope.appDefinitions[deploymentKey] = $rootScope.activeAppDefinition
                    this.getAppDefinition(deploymentKey).then(function(result) {
                        $rootScope.appDefinitions[result.id] = result;

                        // Also update the active definition, if the fetched definition id matches the active one
                        if($rootScope.activeAppDefinition && $rootScope.activeAppDefinition.id == result.id) {
                            $rootScope.activeAppDefinition = result;
                        }
                    }, function() {
                        // When app definition cannot be found, show error and redirect to start page
                        $rootScope.addAlertPromise($translate('APP-DEFINITION.MESSAGE.NOT-FOUND'), 'error');
                        $location.path("/");

                    });
                }
            } else {
                // Clear active app-definition
                $rootScope.activeAppDefinition = undefined;
            }
        };

        this.getAppDefinition = function (deploymentKey) {
            return httpAsPromise({
                method: 'GET',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/runtime/app-definitions/' + deploymentKey
            });
        };

    }]);
