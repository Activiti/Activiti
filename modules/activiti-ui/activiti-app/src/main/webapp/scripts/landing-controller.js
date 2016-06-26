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
