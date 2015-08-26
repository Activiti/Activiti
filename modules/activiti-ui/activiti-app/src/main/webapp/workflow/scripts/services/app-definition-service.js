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
         * Set the id of the app definition that should be used. In case the app definition
         * is not yet cached, it will be fetched.
         *
         * Updating this value will be reflected in the header, to make user aware of a change of context.
         */
        this.setActiveAppDefinitionId = function(appDefinitionId) {
            if (appDefinitionId) {
                if ($rootScope.appDefinitions[appDefinitionId]) {
                    $rootScope.activeAppDefinition = $rootScope.appDefinitions[appDefinitionId];
                } else {
                    // Add placeholder with ID only, and fetch the actual object
                    $rootScope.activeAppDefinition = {id: appDefinitionId};
                    $rootScope.appDefinitions[appDefinitionId] = $rootScope.activeAppDefinition
                    this.getAppDefinition(appDefinitionId).then(function(result) {
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

        this.getAppDefinition = function (appDefinitionId) {
            return httpAsPromise({
                method: 'GET',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/runtime/app-definitions/' + appDefinitionId
            });
        };

        this.getIntegrationAppDefinitionId = function () {
            if (ACTIVITI.CONFIG.integrationProfile) {
                var apps = $rootScope.account.apps;
                for (var i = 0, il = apps.length; i <il; i++) {
                    if (apps[i].name === ACTIVITI.CONFIG.integrationProfile) {
                        return apps[i].id;
                    }
                }
                // Return false so we can see that an app definition was looked for but not found
                return false;
            }
            return null;
        };
    }]);
