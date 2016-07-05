/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

activitiAdminApp.controller('AppDeploymentController', ['$scope', '$rootScope', '$http', '$timeout','$location','$routeParams', '$modal', '$translate', '$q',
    function ($scope, $rootScope, $http, $timeout, $location, $routeParams, $modal, $translate, $q) {
		
        $rootScope.navigation = {selection: 'apps'};
        
        $scope.tabData = {
            tabs: [
                {id: 'processDefinitions', name: 'APP-DEPLOYMENT.TITLE.PROCESS-DEFINITIONS'},
                {id: 'decisionTables', name: 'APP-DEPLOYMENT.TITLE.DECISION-TABLES'},
                {id: 'forms', name: 'APP-DEPLOYMENT.TITLE.FORMS'}
            ]
        };
        $scope.tabData.activeTab = $scope.tabData.tabs[0].id;
		
		$scope.returnToList = function() {
			$location.path("/apps");
		};
		
		$scope.openProcessDefinition = function(definition) {
			if (definition && definition.getProperty('id')) {
				$location.path("/process-definition/" + definition.getProperty('id'));
			}
		};

        $scope.openDecisionTable = function(decisionTable) {
            if (decisionTable && decisionTable.getProperty('id')) {
                $location.path("/decision-table/" + decisionTable.getProperty('id'));
            }
        };
        
        $scope.openForm = function(form) {
            if (form && form.getProperty('id')) {
                $location.path("/form/" + form.getProperty('id'));
            }
        };
		
		$scope.showAllProcessDefinitions = function() {
		    // Populate the process-filter with parentId
		    $rootScope.filters.forced.processDefinitionFilter = {
		            deploymentId: $scope.deployment.id
		    };
		    $location.path("/process-definitions");
		};

        $scope.showAllDecisionTables = function() {
		    // Populate the process-filter with parentId
		    $rootScope.filters.forced.processDefinitionFilter = {
		            deploymentId: $scope.deployment.id
		    };
		    $location.path("/process-definitions");
		};
		
		 $scope.showRedoplyApp = function() {
		     var modalInstance = $modal.open({
	                templateUrl: 'views/redeploy-app-popup.html',
	                controller: 'RedpoloyAppCrtl',
	                resolve: {
	                    app: function () {
	                        return $scope.appDeployment;
	                    }
	                }
	            });
		 };
		 
		 $scope.deleteApp = function() {
		     var modalInstance = $modal.open({
                 templateUrl: 'views/confirm-popup.html',
                 controller: 'ConfirmPopupCrtl',
                 resolve: {
                     model: function () {
                       return {
                           confirm: $translate.instant('APPS.ACTION.DELETE'), 
                           title: $translate.instant('APPS.ACTION.DELETE'), 
                           message: $translate.instant('APPS.POPUP.DELETE.CONFIRM-MESSAGE', $scope.appDeployment)
                           };
                     }
                 }
                 
             });
		     
		     modalInstance.result.then(function (result) {
		         if (result === true) {
		             $http({method: 'DELETE', url: '/app/rest/activiti/apps/' + $routeParams.appId}).
	                 success(function(data, status, headers, config) {
	                     $scope.addAlert($translate.instant('ALERT.APP-DEPLOYMENT.DELETED-APP', $scope.appDeployment), 'info');
	                     $scope.returnToList();
	                 }).
	                 error(function(data, status, headers, config) {
	                     $scope.addAlert($translate.instant('ALERT.APP-DEPLOYMENT.DELETE-ERROR', data), 'error');
                     });
		         }
		     });
		     
         };
		
		$q.all([$translate('PROCESS-DEFINITIONS.HEADER.ID'), 
            $translate('PROCESS-DEFINITIONS.HEADER.NAME'),
            $translate('PROCESS-DEFINITIONS.HEADER.VERSION'),
            $translate('PROCESS-DEFINITIONS.HEADER.KEY')])
            .then(function(headers) { 
        
                $scope.gridProcessDefinitions = {
                    data: 'processDefinitions.data',
                    enableRowReordering: false,
                    multiSelect: false,
                    keepLastSelected : false,
                    enableSorting: false,
                    rowHeight: 36,
                    afterSelectionChange: $scope.openProcessDefinition,
                    columnDefs: [
                          { field: 'id', displayName: headers[0]},
                          { field: 'name', displayName: headers[1]},
                          { field: 'version', displayName: headers[2]},
                          { field: 'key', displayName: headers[3]}
                    ]
                };
        });

        $q.all([$translate('DECISION-TABLES.HEADER.ID'),
                $translate('DECISION-TABLES.HEADER.NAME'),
                $translate('DECISION-TABLES.HEADER.VERSION'),
                $translate('DECISION-TABLES.HEADER.KEY')])
            .then(function(headers) {

                $scope.gridDecisionTables = {
                    data: 'decisionTables.data',
                    enableRowReordering: false,
                    multiSelect: false,
                    keepLastSelected : false,
                    enableSorting: false,
                    rowHeight: 36,
                    afterSelectionChange: $scope.openDecisionTable,
                    columnDefs: [
                        { field: 'id', displayName: headers[0]},
                        { field: 'name', displayName: headers[1]},
                        { field: 'version', displayName: headers[2]},
                        { field: 'key', displayName: headers[3]}
                    ]
                };
        });

        $q.all([$translate('FORMS.HEADER.ID'),
                $translate('FORMS.HEADER.NAME')])
            .then(function(headers) {

                $scope.gridForms = {
                    data: 'forms.data',
                    enableRowReordering: false,
                    multiSelect: false,
                    keepLastSelected : false,
                    enableSorting: false,
                    rowHeight: 36,
                    afterSelectionChange: $scope.openForm,
                    columnDefs: [
                        { field: 'id', displayName: headers[0]},
                        { field: 'name', displayName: headers[1]}
                    ]
                };
        });
        
		$scope.executeWhenReady(function() {
		    // Load deployment
		    $http({method: 'GET', url: '/app/rest/activiti/apps/' + $routeParams.appId}).
  	    	    success(function(data, status, headers, config) {
  	    	        $scope.appDeployment = data;
  	    	        $scope.downloadAppUrl = "app/rest/activiti/apps/export/" + data.deploymentId;

                    // Load process definitions
                    $http({method: 'GET', url: '/app/rest/activiti/apps/process-definitions/' + data.deploymentId}).
                        success(function(processDefinitionsData, status, headers, config) {
                            $scope.processDefinitions = processDefinitionsData;
                            $scope.tabData.tabs[0].info = processDefinitionsData.total;
                        });

                    // Load decision tables
                    if (data.dmnDeploymentId) {
                        $http({method: 'GET', url: '/app/rest/activiti/apps/decision-tables/' + data.dmnDeploymentId}).
                            success(function(decisionTablesData, status, headers, config) {
                                $scope.decisionTables = decisionTablesData;
                                $scope.tabData.tabs[1].info = decisionTablesData.total;
                        });
                    } else {
                        $scope.decisionTables = {"size":0,"total":0,"start":0,"data":[]};
                        $scope.tabData.tabs[1].info = 0;
                    }

                    // Load forms
                    $http({method: 'GET', url: '/app/rest/activiti/forms', params: {appId:data.id}}).
                        success(function(formsData, status, headers, config) {
                            $scope.forms = formsData;
                            $scope.tabData.tabs[2].info = formsData.total;
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
  		

  	        });
}]);

/**\
 * Controller for the upload a model from the process Modeler.
 */
activitiAdminApp.controller('RedpoloyAppCrtl',
    ['$scope', '$modalInstance', '$http', 'app', function ($scope, $modalInstance, $http, app) {

        $scope.status = {loading: false};
        $scope.model = {};
        $scope.app = app;
        $scope.selectTargetServer = function (targetServer) {
            $scope.targetServer = targetServer;
            $scope.model = {};
        };

        $scope.deployApp = function () {
            $scope.status.loading = true;
            $http({method: 'GET', 
                url: '/app/rest/activiti/apps/redeploy/'+app.deploymentId
            }).
            success(function(data, status, headers, config) {
                $scope.status.loading = false;
                if (data.error) {
                    $scope.model.error = true;
                    if (data.errorDescription) { 
                        $scope.model.errorMessage = data.errorDescription;
                    }
                } else {
                    $modalInstance.close(true);
                }
            }).
            error(function(data, status, headers, config) {
                $scope.status.loading = false;
                
                // handle app conflict only if conflictingAppId is provided
                if (status === 409 && data.customData && data.customData.conflictingAppId) { 
                    $scope.model.isConflict = true;
                    $scope.model.conflictAppId = data.customData.conflictingAppId;
                    $scope.model.conflictingAppName = data.customData.conflictingAppName;
                    $scope.model.conflictingAppCreatedBy = data.customData.conflictingAppCreatedBy;
                    $scope.model.conflictMessage = data.message;
                } else {
                    $scope.model.error = true;
                    if (data.message) { 
                        $scope.model.errorMessage = data.message;
                    }
                }
            });
        };
        
        $scope.deployAppAsNewVersion = function (appId) {
            $scope.status.loading = true;
            $http({method: 'GET', 
                url: '/app/rest/activiti/apps/redeploy/' + app.deploymentId + '/' + appId
            }).
            success(function(data, status, headers, config) {
                $scope.status.loading = false;
                if (data.error) {
                    $scope.model.error = true;
                    if (data.errorDescription) { 
                        $scope.model.errorMessage = data.errorDescription;
                    }
                } else {
                    $modalInstance.close(true);
                }
            }).
            error(function(data, status, headers, config) {
                $scope.model.error = true;
                $scope.status.loading = false;
                if (data.message) { 
                    $scope.model.errorMessage = data.message;
                }
            });
        };
        

        $scope.cancel = function () {
            if (!$scope.status.loading) {
                $modalInstance.dismiss('cancel');
            }
        };

    }]);