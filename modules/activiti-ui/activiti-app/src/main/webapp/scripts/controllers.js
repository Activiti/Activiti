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