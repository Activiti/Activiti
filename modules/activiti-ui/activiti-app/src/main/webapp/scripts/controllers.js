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
activitiApp.controller('LoginController', ['$scope', '$location', 'AuthenticationSharedService', '$timeout',
    function ($scope, $location, AuthenticationSharedService, $timeout) {

        $scope.model = {
            loading: false
        };
        $scope.login = function () {

            $scope.model.loading = true;

            jQuery('#username').trigger('change');
            jQuery('#password').trigger('change');

            $timeout(function() {
                AuthenticationSharedService.login({
                    username: $scope.username,
                    password: $scope.password,
                    success: function () {
                        $scope.model.loading = false;
                    },
                    error: function() {
                        $scope.model.loading = false;
                    }
                });
            });


        };
    }]
);

activitiApp.controller('AddAppDefinitionPopupCrtl', ['$rootScope', '$scope', '$http', '$translate', function ($rootScope, $scope, $http, $translate) {

    $scope.popup = {
        loading: true,
        selectedApps: []
    };
    
    $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/editor/app-definitions'}).
	    success(function(response, status, headers, config) {
	        $scope.popup.apps = response;
	        $scope.popup.loading = false;
	    }).
	    error(function(response, status, headers, config) {
	        $scope.popup.loading = false;
        });
    
    $scope.selectApp = function(app) {
        var index = $scope.popup.selectedApps.indexOf(app.id);
        if (index >= 0) {
            $scope.popup.selectedApps.splice(index, 1);
        } else {
            $scope.popup.selectedApps.push(app.id);
        }
    };
    
    $scope.isAppSelected = function(app) {
        if ($scope.popup.selectedApps.indexOf(app.id) >= 0) {
            return true;
        } else {
            return false;
        }
    };
    
    $scope.cancel = function() {
        $scope.close();
    };
    
    $scope.deploy = function() {

        $scope.popup.loading = true;
        var appDefinitions = [];
        for (var i = 0; i < $scope.popup.selectedApps.length; i++) {
            appDefinitions.push({id: $scope.popup.selectedApps[i]});
        }
        var data = {appDefinitions: appDefinitions};
        delete Array.prototype.toJSON;

        $scope.errorMessage = undefined;

        $http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/runtime/app-definitions', data: data}).
            success(function(response, status, headers, config) {
                $scope.loadApps();

                $scope.popup.loading = false;
                $scope.close();
            }).
            error(function(response, status, headers, config) {
                $scope.popup.loading = false;

                if (response && response.messageKey) {
                    $translate(response.messageKey, response.customData).then(function(message) {
                        $scope.errorMessage = message;
                    });
                }
            });
    };
    
    $scope.close = function() {
        $scope.$hide();
    }
    
}]);