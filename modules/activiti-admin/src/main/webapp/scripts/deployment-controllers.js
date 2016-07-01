/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

activitiAdminApp.controller('DeploymentController', ['$scope', '$rootScope', '$http', '$timeout','$location','$routeParams', '$modal', '$translate', '$q',
    function ($scope, $rootScope, $http, $timeout, $location, $routeParams, $modal, $translate, $q) {
		
        $rootScope.navigation = {selection: 'deployments'};
        
		$scope.returnToList = function() {
			$location.path("/deployments");
		};
		
		$scope.openDefinition = function(definition) {
			if (definition && definition.getProperty('id')) {
				$location.path("/process-definition/" + definition.getProperty('id'));
			}
		};
		
		$scope.showAllDefinitions = function() {
		    // Populate the process-filter with parentId
		    $rootScope.filters.forced.processDefinitionFilter = {
		            deploymentId: $scope.deployment.id
		    };
		    $location.path("/process-definitions");
		};
		
		$q.all([$translate('PROCESS-DEFINITIONS.HEADER.ID'), 
            $translate('PROCESS-DEFINITIONS.HEADER.NAME'),
            $translate('PROCESS-DEFINITIONS.HEADER.VERSION'),
            $translate('PROCESS-DEFINITIONS.HEADER.KEY')])
            .then(function(headers) { 
        
                $scope.gridDefinitions = {
                    data: 'definitions.data',
                    enableRowReordering: false,
                    multiSelect: false,
                    keepLastSelected : false,
                    enableSorting: false,
                    rowHeight: 36,
                    afterSelectionChange: $scope.openDefinition,
                    columnDefs: [
                          { field: 'id', displayName: headers[0]},
                          { field: 'name', displayName: headers[1]},
                          { field: 'version', displayName: headers[2]},
                          { field: 'key', displayName: headers[3]}
                    ]
                };
        });
		
		$scope.deleteDeployment = function() {
		    var modalInstance = $modal.open({
                templateUrl: 'views/confirm-popup.html',
                controller: 'ConfirmPopupCrtl',
                resolve: {
                    model: function () {
                      return {
                          confirm: $translate.instant('DEPLOYMENTS.ACTION.DELETE'), 
                          title: $translate.instant('DEPLOYMENTS.ACTION.DELETE'), 
                          message: $translate.instant('DEPLOYMENTS.POPUP.DELETE.CONFIRM-MESSAGE', $scope.deployment)
                          };
                    }
                }
                
            });
            
            modalInstance.result.then(function (result) {
                if (result === true) {
                    $http({method: 'DELETE', url: '/app/rest/activiti/deployments/' + $routeParams.deploymentId}).
                    success(function(data, status, headers, config) {
                        $scope.addAlert($translate.instant('ALERT.DEPLOYMENT.DELETED-DEPLOYMENT', $scope.deployment), 'info');
                        $scope.returnToList();
                    }).
                    error(function(data, status, headers, config) {
                        $scope.addAlert($translate.instant('ALERT.DEPLOYMENT.DELETE-ERROR', data), 'error');
                    });
                }
            });
            
        };
        
		$scope.executeWhenReady(function() {
		    // Load deployment
		    $http({method: 'GET', url: '/app/rest/activiti/deployments/' + $routeParams.deploymentId}).
  	    	    success(function(data, status, headers, config) {
  	    	        $scope.deployment = data;
  	    	        
                    $http({method: 'GET', url: '/app/rest/activiti/app?deploymentId=' + $routeParams.deploymentId}).
                    success(function(appData, status, headers, config) {
                        $scope.appDeployment = appData;
                        $scope.appId = appData.id;
                    });
  	    	    }).
  	    	    error(function(data, status, headers, config) {
                    if (data && data.message) {
                        // Extract error-message
                        $rootScope.addAlert(data.message, 'error');
                    } else {
                        // Use default error-message
                        $rootScope.addAlert($translate.instant('ALERT.GENERAL.HTTP-ERROR'), 'error');
                    }
  	    	    });
  		
		    // Load process definitions
		    $http({method: 'GET', url: '/app/rest/activiti/process-definitions?deploymentId=' + $routeParams.deploymentId}).
  	    	    success(function(data, status, headers, config) {
  	    	        $scope.definitions = data;
  	    	    });
		    
  	     });
}]);