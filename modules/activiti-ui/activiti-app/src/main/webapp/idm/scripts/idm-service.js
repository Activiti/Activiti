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

angular.module('activitiApp').service('IdmService', ['$http', '$q', '$rootScope',
    function ($http, $q, $rootScope) {

        var httpAsPromise = function (options) {
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

        this.getIdmInfo = function () {
            return httpAsPromise(
                {
                    method: 'GET',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/idm-info'
                }
            )
        };


        /*
            PROFILE
        */

        this.getProfile = function () {
            return httpAsPromise(
                {
                    method: 'GET',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/profile'
                }
            )
        };

        this.updateProfileDetails = function(userData, successCallback, errorCallback) {
            var deferred = $q.defer();
            $http({
                method: 'POST',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/profile',
                data: userData
            }).success(function (response, status, headers, config) {
                if (successCallback) {
                    successCallback(response);
                }
                deferred.resolve(response);
            }).error(function (response, status, headers, config) {
                if (errorCallback) {
                    errorCallback(response, status);
                }
                deferred.reject(response);
            });

            var promise = deferred.promise;
            return promise;
        };

        this.changePassword = function(oldPassword, newPassword) {
            return httpAsPromise(
                {
                    method: 'POST',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/profile-password',
                    data: {oldPassword: oldPassword, newPassword: newPassword}
                }
            )
        };


        /*
            GROUPS
        */

        this.getGroups = function() {

            var params = {};
            return httpAsPromise(
                {
                    method: 'GET',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/groups',
                    params: params
                }
            )
        };

        this.getFunctionalGroups = function() {

            var params = {};
            params.functional = 'true';

            return httpAsPromise(
                {
                    method: 'GET',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/groups',
                    params: params
                }
            )
        };

        this.getGroup = function (groupId, includeAllUsers) {

            var params = {};
            if (includeAllUsers !== null && includeAllUsers !== undefined) {
                params.includeAllUsers = includeAllUsers;
            }

            return httpAsPromise(
                {
                    method: 'GET',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/groups/' + groupId,
                    params: params
                }
            )
        };

        this.getUsersForGroup = function(groupId, filter, page, pageSize) {

            var params = {};

            if (filter !== null && filter !== undefined) {
                params.filter = filter;
            }

            if (page !== null && page !== undefined) {
                params.page = page;
            }

            if (pageSize !== null && pageSize !== undefined) {
                params.pageSize = pageSize
            }

            return httpAsPromise(
                {
                    method: 'GET',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/groups/' + groupId + '/users',
                    params: params
                }
            )

        };

        this.createGroup = function (createGroupData) {

            return httpAsPromise(
                {
                    method: 'POST',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/groups',
                    data: createGroupData
                }
            )
        };


        this.updateGroup = function (groupId, tenantId, updatedGroupData) {

            var data = {name: updatedGroupData.name};
            if (tenantId !== null && tenantId !== undefined && tenantId !== -1) {
                data.tenantId = tenantId;
            }

            return httpAsPromise(
                {
                    method: 'PUT',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/groups/' + groupId ,
                    data: data
                }
            )
        };

        this.deleteGroup = function(groupId) {
            return httpAsPromise(
                {
                    method: 'DELETE',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/groups/' + groupId
                }
            )
        };

        this.activateGroup = function(groupId) {
            return httpAsPromise(
                {
                    method: 'POST',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/groups/' + groupId + '/action/activate'
                }
            )
        };

        this.deleteGroupMember = function(groupId, userId) {
            return httpAsPromise(
                {
                    method: 'DELETE',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/groups/' + groupId + '/members/' + userId
                }
            )
        };

        this.addGroupMember = function(groupId, userId) {
            return httpAsPromise(
                {
                    method: 'POST',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/groups/' + groupId + '/members/' + userId
                }
            )
        };

    }]);
