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
