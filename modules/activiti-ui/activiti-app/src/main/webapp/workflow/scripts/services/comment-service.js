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
