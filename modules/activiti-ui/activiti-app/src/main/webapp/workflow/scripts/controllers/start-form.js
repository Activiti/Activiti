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

angular.module('activitiApp')
    .controller('StartFormController', ['$rootScope', '$scope', '$translate', '$http', '$timeout','$location', '$route', '$modal', '$routeParams', '$popover',
        function ($rootScope, $scope, $translate, $http, $timeout, $location, $route, $modal, $routeParams, $popover) {
   
    $scope.model.initializing = true;    
        
    $scope.$watch('selectedProcessInstance', function(newValue) {
       
        if (newValue && newValue.id && (newValue.id != $rootScope.root.selectedProcessId || !$scope.model.initializing)) {
            $scope.model.processInstance = newValue;

            $scope.getProcessInstance(newValue.id);
            $rootScope.root.showStartForm = false;
            $scope.model.formData = undefined;
            $scope.model.initializing = false;
        }
    });
        
    $scope.getProcessInstance = function(processInstanceId) {
        $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/process-instances/' + processInstanceId}).
            success(function(response, status, headers, config) {
                $scope.model.processInstance = response;
                $scope.loadStartForm(processInstanceId);
            }).
            error(function(response, status, headers, config) {
                console.log('Something went wrong: ' + response);
            });
    };

    $scope.loadStartForm = function(processInstanceId) {
        $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/process-instances/' + processInstanceId + '/start-form'}).
            success(function(response, status, headers, config) {
                $scope.model.formData = response;
            }).
            error(function(response, status, headers, config) {
                console.log('Something went wrong: ' + response);
            });
    };
    
    $scope.openProcessInstance = function() {
        $rootScope.root.showStartForm = false;
        $scope.model.formData = undefined;
    };
    
    $scope.getProcessInstance($rootScope.root.selectedProcessId);
}]);
