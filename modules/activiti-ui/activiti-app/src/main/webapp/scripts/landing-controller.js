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
activitiApp.controller('LandingController', ['$scope','$window', '$location', '$http', '$translate', '$modal', 'RuntimeAppDefinitionService', '$rootScope',
    function ($scope, $window, $location, $http, $translate, $modal, RuntimeAppDefinitionService, $rootScope) {

        $scope.model = {
          loading: true
        };

        $translate('APP.ACTION.DELETE').then(function(message) {
            $scope.appActions = [
                {
                    text: message,
                    click: 'deleteApp(app); '
                }
            ];
        });

        $scope.loadApps = function() {
            $scope.model.customAppsFetched = false;
            RuntimeAppDefinitionService.getApplications().then(function(result){
                $scope.model.apps = result.defaultApps.concat(result.customApps);
                $scope.model.customAppsFetched = true;
                $scope.model.customApps = result.customApps.length > 0;

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

                $scope.urls = {
                    editor: baseUrl + '/editor/',
                    identity: baseUrl + '/idm/',
                    workflow: baseUrl + '/workflow/',
                    analytics: baseUrl + '/analytics/'
                };


            })
        };

        $scope.appSelected = function(app) {
            if(app.fixedUrl) {
                $window.location.href = app.fixedUrl;
            }
        };

        $scope.addAppDefinition = function() {

            _internalCreateModal({
                template: 'views/modal/add-app-definition-modal.html',
                scope: $scope
            }, $modal, $scope);
        };


        $scope.deleteApp = function(app) {
            if(app && app.id) {
                RuntimeAppDefinitionService.deleteAppDefinition(app.id).then(function() {
                    $rootScope.addAlertPromise($translate('APP.MESSAGE.DELETED'), 'info')

                    // Remove app from list
                    var index = -1;
                    for(var i=0; i< $scope.model.apps.length; i++) {
                        if($scope.model.apps[i].id == app.id) {
                            index = i;
                            break;
                        }
                    }

                    if(index >= 0) {
                        $scope.model.apps.splice(index, 1);
                    }
                });
            }
        };

        $scope.loadApps();
    }]
);
