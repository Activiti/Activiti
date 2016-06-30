/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

activitiAdminApp.controller('ProcessDefinitionController', ['$scope', '$rootScope', '$http', '$timeout','$location','$routeParams', '$modal', '$translate', '$q', 'gridConstants',
    function ($scope, $rootScope, $http, $timeout, $location, $routeParams, $modal, $translate, $q, gridConstants) {
		$rootScope.navigation = {selection: 'process-definitions'};
        
		$scope.tabData = {
		    tabs: [
		       {id: 'processInstances', name: 'PROCESS-DEFINITION.TITLE.PROCESS-INSTANCES'},
		       {id: 'jobs', name: 'PROCESS-DEFINITION.TITLE.JOBS'}
		    ],
		};
		$scope.tabData.activeTab = $scope.tabData.tabs[0].id;

		$scope.returnToList = function() {
			$location.path("/process-definitions");
		};

		$scope.openDefinition = function(definitionId) {
			if (definitionId) {
				$location.path("/process-definition/" + definitionId);
			}
		};


		$scope.openDeployment = function(deploymentId) {
		    if (deploymentId) {
		        $location.path("/deployment/" + deploymentId);
		    }
		};

    $scope.editCategory = function() {
      var modalInstance = $modal.open({
        templateUrl: 'views/process-definition-edit-category-popup.html',
        controller: 'EditProcessDefinitionCategoryModalCrtl',
        resolve: {
          definition: function() {
            return $scope.definition;
          }
        }
      });

      modalInstance.result.then(function (data) {
        if(data) {
          $scope.addAlert($translate.instant('ALERT.PROCESS-DEFINITION.CATEGORY-UPDATED', $scope.definition), 'info');
          $scope.definition = data;
        }
      });
    };

    $scope.showProcessDiagram = function() {
      $modal.open({
        templateUrl: 'views/process-definition-diagram-popup.html',
        windowClass: 'modal modal-full-width',
        controller: 'ShowProcessDefinitionDiagramPopupCrtl',
        resolve: {
          definition: function() {
            return $scope.definition;
          }
        }
      });
    };

    $scope.openJob = function(job) {
      if (job && job.getProperty('id')) {
        $location.path("/job/" + job.getProperty('id'));
      }
    };

    $scope.openProcessInstance = function(instance) {
      if (instance && instance.getProperty('id')) {
        $location.path("/process-instance/" + instance.getProperty('id'));
      }
    };
    
    $scope.openStartForm = function () {
        if ($scope.startForm) {
            $location.path("/form/" + $scope.startForm.id);
        }
    };
    
    $scope.openForm = function (form) {
        if (form && form.getProperty('id')) {
            $location.path("/form/" + form.getProperty('id'));
          }
    };
    
    $scope.openDecisionTable = function (decisionTable) {
        if (decisionTable && decisionTable.getProperty('id')) {
            $location.path("/decision-table/" + decisionTable.getProperty('id'));
          }
    };

    $scope.showAllJobs = function() {
      // Populate the job-filter with process definition id
      $rootScope.filters.forced.jobFilter = {
        processDefinitionId: $scope.definition.id
      };
      $location.path("/jobs");
    };

    $scope.showAllProcesses = function() {
      // Populate the process-filter with parentId
      $rootScope.filters.forced.instanceFilter = {
          processDefinitionId: $scope.definition.id
      };
      $location.path("/process-instances");
    };

    $scope.loadProcessInstances = function() {
      $scope.processInstances = undefined;
      $http({method: 'GET', url: '/app/rest/activiti/process-definitions/' + $scope.definition.id +'/process-instances'}).
      success(function(data, status, headers, config) {
        $scope.processInstances = data;
        $scope.tabData.tabs[0].info = data.total;
      });
    };

        $scope.loadJobs = function() {
            $scope.jobs = undefined;
            $http({method: 'GET', url: '/app/rest/activiti/process-definitions/' + $scope.definition.id +'/jobs'}).
            success(function(data, status, headers, config) {
                $scope.jobs = data;
                $scope.tabData.tabs[1].info = data.total;
            });
        };
        
        function loadStartForm () {
            $scope.jobs = undefined;
            $http({method: 'GET', url: '/app/rest/activiti/process-definition-start-form/' + $scope.definition.id}).
            success(function(data, status, headers, config) {
                $scope.startForm = data;
            });
        };
        
        $scope.loadDecisionTables = function() {
            // Load decision tables
            $http({method: 'GET', url: '/app/rest/activiti/process-definition-decision-tables/' + $scope.definition.id}).
            success(function(data, status, headers, config) {
                $scope.decisionTables = data;
                $scope.tabData.tabs[2].info = data.total;
            }).
            error(function(data, status, headers, config) {
            });
        };
        
        $scope.loadForms = function() {
            // Load forms
            $http({method: 'GET', url: '/app/rest/activiti/process-definition-forms/' + $scope.definition.id}).
            success(function(data, status, headers, config) {
                $scope.forms = data;
                $scope.tabData.tabs[3].info = data.total;
            }).
            error(function(data, status, headers, config) {
            });
        };

		$scope.executeWhenReady(function() {
		    // Load definition
		    $http({method: 'GET', url: '/app/rest/activiti/process-definitions/' + $routeParams.definitionId}).
		    success(function(data, status, headers, config) {
		        $scope.definition = data;
		        $scope.loadProcessInstances();
		        $scope.loadJobs();
                $scope.tabData.tabs.push({id: 'decisionTables', name: 'PROCESS-DEFINITION.TITLE.DECISION-TABLES'});
                $scope.tabData.tabs.push({id: 'forms', name: 'PROCESS-DEFINITION.TITLE.FORMS'});
                if (data.startFormDefined) {
                    loadStartForm();
                }
                $scope.loadDecisionTables();
                $scope.loadForms();
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


		    $q.all([$translate('PROCESS-INSTANCES.HEADER.ID'),
                  $translate('PROCESS-INSTANCES.HEADER.NAME'),
                  $translate('PROCESS-INSTANCES.HEADER.STATUS'),
                  $translate('PROCESS-INSTANCES.HEADER.CREATE-TIME')])
            .then(function(headers) {
                var stateTemplate = '<div><div class="ngCellText">{{row.getProperty("endTime") && "Completed" || "Active"}}</div></div>';
                var dateTemplate = '<div><div class="ngCellText" title="{{row.getProperty(col.field) | dateformat:\'full\'}}">{{row.getProperty(col.field) | dateformat}}</div></div>';
    
                // Config for variable grid
                $scope.gridProcessInstances = {
                    data: 'processInstances.data',
                    enableRowReordering: false,
                    multiSelect: false,
                    keepLastSelected : false,
                    enableSorting: false,
                    rowHeight: 36,
                    afterSelectionChange: $scope.openProcessInstance,
                    columnDefs: [
                        { field: 'id', displayName: headers[0], width: 75},
                        { field: 'name', displayName: headers[1]},
                        { field: 'endTime', displayName: headers[2], cellTemplate: stateTemplate},
                        { field: 'startTime', displayName: headers[3], cellTemplate: dateTemplate}
                    ]
                };
            });

		    $q.all([$translate('JOBS.HEADER.ID'),
                  $translate('JOBS.HEADER.DUE-DATE'),
                  $translate('JOBS.HEADER.RETRIES'),
                  $translate('JOBS.HEADER.EXCEPTION')])
            .then(function(headers) {
                var dateTemplate = '<div><div class="ngCellText" title="{{row.getProperty(col.field) | dateformat:\'full\'}}">{{row.getProperty(col.field) | dateformat}}</div></div>';
    
                // Config for variable grid
                $scope.gridJobs = {
                    data: 'jobs.data',
                    enableRowReordering: false,
                    multiSelect: false,
                    keepLastSelected : false,
                    enableSorting: false,
                    rowHeight: 36,
                    afterSelectionChange: $scope.openJob,
                    columnDefs: [
                        { field: 'id', displayName: headers[0], width: 50},
                        { field: 'dueDate', displayName: headers[1], cellTemplate: dateTemplate},
                        { field: 'retries', displayName: headers[2]},
                        { field: 'exceptionMessage', displayName: headers[3]}
                    ]
                };
            });
		    
		    $q.all([$translate('DECISION-TABLES.HEADER.ID'),
	                $translate('DECISION-TABLES.HEADER.NAME'),
	                $translate('DECISION-TABLES.HEADER.KEY'),
	                $translate('DECISION-TABLES.HEADER.VERSION'),
	                $translate('DECISION-TABLES.HEADER.TENANT-ID')])
	            .then(function (headers) {
	                // Config for grid
	                $scope.gridDecisionTables = {
	                    data: 'decisionTables.data',
	                    enableRowReordering: true,
	                    multiSelect: false,
	                    keepLastSelected: false,
	                    rowHeight: 36,
	                    afterSelectionChange: $scope.openDecisionTable,
	                    columnDefs: [
	                        {field: 'id', displayName: headers[0], cellTemplate: gridConstants.defaultTemplate},
	                        {field: 'name', displayName: headers[1], cellTemplate: gridConstants.defaultTemplate},
	                        {field: 'key', displayName: headers[2], cellTemplate: gridConstants.defaultTemplate},
	                        {field: 'version', displayName: headers[3], cellTemplate: gridConstants.defaultTemplate},
	                        {field: 'tenantId', displayName: headers[4], cellTemplate: gridConstants.defaultTemplate}]
	                };
	            });
		    
		    $q.all([$translate('FORMS.HEADER.ID'),
	                $translate('FORMS.HEADER.NAME'),
	                $translate('FORMS.HEADER.APPID'),
	                $translate('FORMS.HEADER.TENANTID')])
	            .then(function (headers) {
	                // Config for grid
	                $scope.gridForms = {
	                    data: 'forms.data',
	                    enableRowReordering: true,
	                    multiSelect: false,
	                    keepLastSelected: false,
	                    rowHeight: 36,
	                    afterSelectionChange: $scope.openForm,
	                    columnDefs: [
	                        {field: 'id', displayName: headers[0], cellTemplate: gridConstants.defaultTemplate},
	                        {field: 'name', displayName: headers[1], cellTemplate: gridConstants.defaultTemplate},
	                        {field: 'appDeploymentId', displayName: headers[2], cellTemplate: gridConstants.defaultTemplate},
	                        {field: 'tenantId', displayName: headers[3], cellTemplate: gridConstants.defaultTemplate}]
	                };
	            });
		      
		});

}]);

activitiAdminApp.controller('EditProcessDefinitionCategoryModalCrtl',
    ['$rootScope', '$scope', '$modalInstance', '$http', 'definition', function ($rootScope, $scope, $modalInstance, $http, definition) {

  $scope.model = {
      id: definition.id,
      category: definition.category,
      name: definition.name,
      key: definition.key,
  };

  $scope.status = {loading: false};


  $scope.ok = function () {
    $scope.status.loading = true;

    var data = {
        category: $scope.model.category
    };

    $http({method: 'PUT', url: '/app/rest/activiti/process-definitions/' + $scope.model.id, data: data}).
      success(function(data, status, headers, config) {
        $modalInstance.close(data);
        $scope.status.loading = false;
      }).
      error(function(data, status, headers, config) {
        $modalInstance.close(false);
       $scope.status.loading = false;
      });
  };

  $scope.cancel = function () {
  if(!$scope.status.loading) {
    $modalInstance.dismiss('cancel');
  }
  };
}]);

activitiAdminApp.controller('ShowProcessDefinitionDiagramPopupCrtl',
    ['$rootScope', '$scope', '$modalInstance', '$http', 'definition', '$timeout', function ($rootScope, $scope, $modalInstance, $http, definition, $timeout) {

  $scope.model = {
      id: definition.id,
      name: definition.name
  };

  $scope.status = {loading: false};

  $scope.cancel = function () {
    if(!$scope.status.loading) {
      $modalInstance.dismiss('cancel');
    }
  };

  $timeout(function() {
    $("#bpmnModel").attr("data-definition-id", definition.id);
    $("#bpmnModel").attr("data-server-id", $rootScope.activeServer.id);
    $("#bpmnModel").load("./display/displaymodel.html?definitionId=" + definition.id);
  }, 200);


}]);
