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
