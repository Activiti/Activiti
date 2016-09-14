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
                    data: {originalPassword: oldPassword, newPassword: newPassword}
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


        this.updateGroup = function (groupId, updatedGroupData) {

            var data = {name: updatedGroupData.name};
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
