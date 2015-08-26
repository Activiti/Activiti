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

        this.getProcessDefinitions = function(appDefinitionId) {
            var url = ACTIVITI.CONFIG.contextRoot + '/app/rest/process-definitions?latest=true';
            if (appDefinitionId) {
                url += '&appDefinitionId=' + appDefinitionId;
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
