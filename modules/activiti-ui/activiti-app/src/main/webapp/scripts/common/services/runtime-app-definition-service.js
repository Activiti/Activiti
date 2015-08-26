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

// User service
activitiApp.service('RuntimeAppDefinitionService', ['$http', '$q', '$location', 'AuthenticationSharedService', 'appName',
    function ($http, $q, $location, AuthenticationSharedService, appName) {

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

        this.getApplications = function () {

            var defaultApps = [];

            // Determine the full url with a context root (if any)
            var baseUrl = $location.absUrl();
            var index = baseUrl.indexOf('/#');
            if (index >= 0) {
                baseUrl = baseUrl.substring(0, index);
            }
            index = baseUrl.indexOf('?');
            if (index >= 0) {
                baseUrl = baseUrl.substring(0, index);
            }
            if (baseUrl[baseUrl.length - 1] == '/') {
                baseUrl = baseUrl.substring(0, baseUrl.length - 1);
            }
            if (appName.length > 0 && baseUrl.substring(baseUrl.length - appName.length) == appName) {
                baseUrl = baseUrl.substring(0, baseUrl.length - appName.length - 1);
            }

            var urls = {
                editor: baseUrl + '/editor/',
                identity: baseUrl + '/idm/',
                workflow: baseUrl + '/workflow/',
                admin: 'http://localhost:8080/activiti-admin',
                analytics: baseUrl + '/analytics/'
            };

            var transformAppsResponse = function(value) {
                var response = JSON.parse(value);
                var customApps = [];
                for (var i = 0; i < response.data.length; i++) {

                    var app = response.data[i];
                    if (app.defaultAppId !== undefined && app.defaultAppId !== null) {

                        // Default app
                        if (app.defaultAppId === 'kickstart') {

                            defaultApps.push(
                                {
                                    id: 'kickstart',
                                    titleKey: 'APP.KICKSTART.TITLE',
                                    descriptionKey: 'APP.KICKSTART.DESCRIPTION',
                                    defaultAppId : app.defaultAppId,
                                    theme: 'theme-1',
                                    icon: 'icon icon-choice',
                                    fixedBaseUrl: urls.editor + '/#/',
                                    fixedUrl: urls.editor,
                                    pages: ['processes', 'forms', 'apps', 'stencils']
                                });

                        } else if (app.defaultAppId === 'tasks') {

                            defaultApps.push(
                                {
                                    id: 'tasks',
                                    titleKey: 'APP.TASKS.TITLE',
                                    descriptionKey: 'APP.TASKS.DESCRIPTION',
                                    defaultAppId : app.defaultAppId,
                                    theme: 'theme-2',
                                    icon: 'icon icon-clock',
                                    fixedBaseUrl: urls.workflow + '/#/',
                                    fixedUrl: urls.workflow,
                                    pages: ['tasks', 'processes']
                                });

                        } else if (app.defaultAppId === 'identity') {

                            var identityApp = {
                                id: 'identity',
                                titleKey: 'APP.IDENTITY-MANAGEMENT.TITLE',
                                descriptionKey: 'APP.IDENTITY-MANAGEMENT.DESCRIPTION',
                                defaultAppId : app.defaultAppId,
                                theme: 'theme-3',
                                icon: 'icon icon-user',
                                fixedBaseUrl: urls.identity + '/#/',
                                fixedUrl: urls.identity
                            };

                                identityApp.pages = ['tenant-mgmt', 'user-mgmt', 'functional-group-mgmt', 'profile'];

                            defaultApps.push(identityApp);

                        } else if (app.defaultAppId === 'analytics') {

                            defaultApps.push(
                                {
                                    id: 'analytics',
                                    titleKey: 'APP.ANALYTICS.TITLE',
                                    descriptionKey: 'APP.ANALYTICS.DESCRIPTION',
                                    defaultAppId : app.defaultAppId,
                                    theme: 'theme-6',
                                    icon: 'glyphicon glyphicon-stats',
                                    fixedBaseUrl: urls.analytics + '/#/',
                                    fixedUrl: urls.analytics
                                });

                        }

                    } else {

                        // Custom app
                        app.icon = 'glyphicon ' + app.icon;
                        app.fixedBaseUrl = baseUrl + '/workflow/#/apps/' + app.id + '/';
                        app.fixedUrl = app.fixedBaseUrl + 'tasks';
                        app.pages = [ 'tasks', 'processes' ];
                        app.deletable = true;
                        customApps.push(app);

                    }

                }

                return {
                    defaultApps: defaultApps,
                    customApps: customApps
                };
            };

            return httpAsPromise({
                method: 'GET',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/runtime/app-definitions',
                transformResponse: transformAppsResponse
            });
        };

        this.deleteAppDefinition = function (appDefinitionId) {
            var promise = httpAsPromise({
                method: 'DELETE',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/runtime/app-definitions/' + appDefinitionId
            });

            return promise;
        };
    }]);