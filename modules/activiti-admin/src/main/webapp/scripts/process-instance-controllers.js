/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

activitiAdminApp.controller('ProcessInstanceController', ['$scope', '$rootScope', '$http', '$timeout','$location','$routeParams', '$modal', '$translate', '$q', 'gridConstants',
    function ($scope, $rootScope, $http, $timeout, $location, $routeParams, $modal, $translate, $q, gridConstants) {

        $rootScope.navigation = {selection: 'process-instances'};
        
		$scope.tabData = {
            tabs: [
               {id: 'tasks', name: 'PROCESS-INSTANCE.TITLE.TASKS'},
               {id: 'variables', name: 'PROCESS-INSTANCE.TITLE.VARIABLES'},
               {id: 'subProcesses', name: 'PROCESS-INSTANCE.TITLE.SUBPROCESSES'},
               {id: 'jobs', name: 'PROCESS-INSTANCE.TITLE.JOBS'}
            ]
        };

		$scope.tabData.activeTab = $scope.tabData.tabs[0].id;

		$scope.returnToList = function() {
			$location.path("/process-instances");
		};

		$scope.openTask = function(task) {
			if (task && task.getProperty('id')) {
				$location.path("/task/" + task.getProperty('id'));
			}
		};

		$scope.openJob = function(job) {
			if (job && job.getProperty('id')) {
				$location.path("/job/" + job.getProperty('id'));
			}
		};

		$scope.openProcessInstance = function(instance) {
			if (instance) {
				var id;
				if(instance.getProperty !== undefined) {
					id = instance.getProperty('id');
				} else {
					id = instance;
				}
				$location.path("/process-instance/" + id);
			}
		};

		$scope.showAllTasks = function() {
        	// Populate the task-filter with parentId
        	$rootScope.filters.forced.taskFilter = {
        		processInstanceId: $scope.process.id
        	};
        	$location.path("/tasks");
        };

        $scope.showAllSubprocesses = function() {
        	// Populate the process-filter with parentId
        	$rootScope.filters.forced.instanceFilter = {
        			superProcessInstanceId: $scope.process.id
        	};
        	$scope.returnToList();
        };

        $scope.openProcessDefinition = function(processDefinitionId) {
			if (processDefinitionId) {
				$location.path("/process-definition/" + processDefinitionId);
			}
		};

		$scope.showProcessDiagram = function() {
		    $modal.open({
		        templateUrl: 'views/process-instance-diagram-popup.html',
		        windowClass: 'modal modal-full-width',
		        controller: 'ShowProcessInstanceDiagramPopupCrtl',
		        resolve: {
		            process: function() {
		                return $scope.process;
		            }
		        }
		    });
		};
		
		$scope.openDecisionTable = function(decisionTable) {
            if (decisionTable && decisionTable.getProperty('id')) {
                $location.path("/decision-audit/" + decisionTable.getProperty('id'));
            }
        };
        
		$scope.openSubmittedForm = function(submittedForm) {
            if (submittedForm && submittedForm.getProperty('id')) {
                $location.path("/submitted-form/" + submittedForm.getProperty('id'));
            }
        };

		$scope.loadProcessDefinition = function() {
		    // Load definition
		    $http({method: 'GET', url: '/app/rest/activiti/process-definitions/' + $scope.process.processDefinitionId}).
		    success(function(data, status, headers, config) {
		        $scope.definition = data;
		    }).
		    error(function(data, status, headers, config) {
		    });
		};

		$scope.loadProcessInstance = function() {
			$scope.process = undefined;
			// Load process
			$http({method: 'GET', url: '/app/rest/activiti/process-instances/' + $routeParams.processInstanceId}).
			success(function(data, status, headers, config) {
				$scope.process = data;

				if(data) {
					$scope.processCompleted = data.endTime != undefined;
				}

				// Start loading children
				$scope.loadProcessDefinition();
				$scope.loadTasks();
				$scope.loadVariables();
				$scope.loadSubProcesses();
				$scope.loadJobs();
				
                $scope.tabData.tabs.push({id: 'decisionTables', name: 'PROCESS-INSTANCE.TITLE.DECISION-TABLES'});
                $scope.tabData.tabs.push({id: 'forms', name: 'PROCESS-INSTANCE.TITLE.FORMS'});
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
		};

		var dateTemplate = '<div><div class="ngCellText" title="{{row.getProperty(col.field) | dateformat:\'full\'}}">{{row.getProperty(col.field) | dateformat}}</div></div>';

		// Config for subtasks grid
		$q.all([$translate('TASKS.HEADER.ID'),
             $translate('TASKS.HEADER.NAME'),
             $translate('TASKS.HEADER.ASSIGNEE'),
             $translate('TASKS.HEADER.OWNER'),
             $translate('TASKS.HEADER.CREATE-TIME'),
             $translate('TASKS.HEADER.END-TIME')])
    .then(function(headers) {
        $scope.gridTasks = {
            data: 'tasks.data',
            enableRowReordering: false,
            multiSelect: false,
            keepLastSelected : false,
            enableSorting: false,
            rowHeight: 36,
            afterSelectionChange: $scope.openTask,
            columnDefs: [
                 { field: 'id', displayName: headers[0], width: 50},
                 { field: 'name', displayName: headers[1]},
                 { field: 'assignee', displayName: headers[2], cellTemplate:gridConstants.defaultTemplate},
                 { field: 'owner', displayName: headers[3],cellTemplate:gridConstants.defaultTemplate},
                 { field: 'startTime', displayName: headers[4], cellTemplate: dateTemplate},
                 { field: 'endTime', displayName: headers[5], cellTemplate: dateTemplate}
            ]
        };
    });

	  $q.all([$translate('VARIABLES.HEADER.NAME'),
            $translate('VARIABLES.HEADER.TYPE'),
            $translate('VARIABLES.HEADER.VALUE')])
   .then(function(headers) {
        var variableValueTemplate = '<div><div class="ngCellText">{{row.getProperty("variable.valueUrl") && "(Binary)" || row.getProperty(col.field)}}</div></div>';
        var variableTypeTemplate = '<div><div class="ngCellText">{{row.getProperty(col.field) && row.getProperty(col.field) || "null"}}</div></div>';

        $scope.selectedVariables = [];

        // Config for variable grid
        $scope.gridVariables = {
            data: 'variables.data',
            enableRowReordering: false,
            multiSelect: false,
            keepLastSelected : false,
            enableSorting: false,
            rowHeight: 36,
            selectedItems: $scope.selectedVariables,
            columnDefs: [
                { field: 'variable.name', displayName: headers[0]},
                { field: 'variable.type', displayName: headers[1], cellTemplate: variableTypeTemplate},
                { field: 'variable.value', displayName: headers[2], cellTemplate: variableValueTemplate}
            ]
        };
   });
	  $q.all([$translate('PROCESS-INSTANCES.HEADER.ID'),
            $translate('PROCESS-INSTANCES.HEADER.NAME'),
            $translate('PROCESS-INSTANCES.HEADER.PROCESS-DEFINITION'),
            $translate('PROCESS-INSTANCES.HEADER.STATUS')])
   .then(function(headers) {
        var subprocessStateTemplate = '<div><div class="ngCellText">{{row.getProperty("endTime") && "Completed" || "Active"}}</div></div>';
        // Config for variable grid
        $scope.gridSubprocesses = {
            data: 'subprocesses.data',
            enableRowReordering: false,
            multiSelect: false,
            keepLastSelected : false,
            enableSorting: false,
            rowHeight: 36,
            afterSelectionChange: $scope.openProcessInstance,
            columnDefs: [
                { field: 'id', displayName: headers[0]},
                { field: 'name', displayName: headers[1]},
                { field: 'processDefinitionId', displayName: headers[2]},
                { field: 'endTime', displayName: headers[3], cellTemplate: subprocessStateTemplate}
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
	  
	  $q.all([$translate('DECISION-AUDIT.HEADER.ID'),
              $translate('DECISION-AUDIT.HEADER.PROCESS-DEFINITION-ID'),
              $translate('DECISION-AUDIT.HEADER.PROCESS-INSTANCE-ID'),
              $translate('DECISION-AUDIT.HEADER.CREATED'),
              $translate('DECISION-AUDIT.HEADER.FAILED')])
          .then(function (headers) {

              $scope.gridDecisionTables = {
                  data: 'decisionTables.data',
                  enableRowReordering: false,
                  multiSelect: false,
                  keepLastSelected: false,
                  enableSorting: false,
                  rowHeight: 36,
                  afterSelectionChange: $scope.openDecisionTable,
                  columnDefs: [
                      {field: 'id', displayName: headers[0]},
                      {field: 'processDefinitionId', displayName: headers[1]},
                      {field: 'processInstanceId', displayName: headers[2]},
                      {field: 'decisionExecutionEnded', displayName: headers[3], cellTemplate: gridConstants.dateTemplate},
                      {field: 'decisionExecutionFailed', displayName: headers[4]}
                  ]
              };
          });
	  
	  $q.all([$translate('SUBMITTED-FORM.HEADER.ID'),
              $translate('SUBMITTED-FORM.HEADER.TASK-ID'),
              $translate('SUBMITTED-FORM.HEADER.PROCESS-ID'),
              $translate('SUBMITTED-FORM.HEADER.SUBMITTED'),
              $translate('SUBMITTED-FORM.HEADER.SUBMITTED-BY')])
          .then(function (headers) {

              $scope.gridForms = {
                  data: 'forms.data',
                  enableRowReordering: false,
                  multiSelect: false,
                  keepLastSelected: false,
                  enableSorting: false,
                  rowHeight: 36,
                  afterSelectionChange: $scope.openSubmittedForm,
                  columnDefs: [
                      {field: 'id', displayName: headers[0]},
                      {field: 'taskId', displayName: headers[1]},
                      {field: 'processId', displayName: headers[2]},
                      {field: 'submitted', displayName: headers[3], cellTemplate: gridConstants.dateTemplate},
                      {field: 'submittedBy', displayName: headers[4], cellTemplate: gridConstants.userObjectTemplate}
                  ]
              };
          });
	  
        $scope.showAllJobs = function() {
        	// Populate the job-filter with process id
        	$rootScope.filters.forced.jobFilter = {
        		processInstanceId: $scope.process.id
        	};
        	$location.path("/jobs");
        };

		$scope.loadTasks = function() {
			$scope.tasks = undefined;
			$http({method: 'GET', url: '/app/rest/activiti/process-instances/' + $scope.process.id +'/tasks'}).
			success(function(data, status, headers, config) {
				$scope.tasks = data;
				$scope.tabData.tabs[0].info = data.total;
			});
		};

		$scope.loadVariables = function() {
			$scope.variables = undefined;
			$http({method: 'GET', url: '/app/rest/activiti/process-instances/' + $scope.process.id +'/variables'}).
			    success(function(data, status, headers, config) {
			        $scope.variables = data;
                    $scope.tabData.tabs[1].info = data.total;
			});
		};

		$scope.loadSubProcesses = function() {
			$scope.subprocesses = undefined;
			$http({method: 'GET', url: '/app/rest/activiti/process-instances/' + $scope.process.id +'/subprocesses'}).
			success(function(data, status, headers, config) {
				$scope.subprocesses = data;
				$scope.tabData.tabs[2].info = data.total;
			});
		};

		$scope.loadJobs = function() {
			$scope.jobs = undefined;
			$http({method: 'GET', url: '/app/rest/activiti/process-instances/' + $scope.process.id +'/jobs'}).
			success(function(data, status, headers, config) {
				$scope.jobs = data;
				$scope.tabData.tabs[3].info = data.total;
			});
		};
		
		$scope.loadProcessDefinition = function() {
            // Load definition
            $http({method: 'GET', url: '/app/rest/activiti/process-definitions/' + $scope.process.processDefinitionId}).
            success(function(data, status, headers, config) {
                $scope.definition = data;
            }).
            error(function(data, status, headers, config) {
            });
        };

        $scope.loadDecisionTables = function() {
            // Load decision tables
            $http({method: 'GET', url: '/app/rest/activiti/process-instances/' + $scope.process.id +'/decision-tasks'}).
            success(function(data, status, headers, config) {
                $scope.decisionTables = data;
                $scope.tabData.tabs[4].info = data.total;
            }).
            error(function(data, status, headers, config) {
            });
        };
        
        $scope.loadForms = function() {
            // Load forms
            $http({method: 'GET', url: '/app/rest/activiti/process-submitted-forms/' + $scope.process.id}).
            success(function(data, status, headers, config) {
                $scope.forms = data;
                $scope.tabData.tabs[5].info = data.total;
            }).
            error(function(data, status, headers, config) {
            });
        };



		$scope.executeWhenReady(function() {
      $scope.loadProcessInstance();
    });


		// Dialogs
		$scope.deleteProcessInstance = function(action) {
			if(!action) {
				action = "delete";
			}
			var modalInstance = $modal.open({
				templateUrl: 'views/process-instance-delete-popup.html',
				controller: 'DeleteProcessModalInstanceCrtl',
				resolve: {
					process: function () {
				          return $scope.process;
					},
				action: function() {return action;}
				}
			});

			modalInstance.result.then(function (deleteProcessInstance) {
				if(deleteProcessInstance) {
					if(action == 'delete') {
					  $scope.addAlert($translate.instant('ALERT.PROCESS-INSTANCE.DELETED', $scope.process), 'info');
						$scope.returnToList();
					} else {
					  $scope.addAlert($translate.instant('ALERT.PROCESS-INSTANCE.TERMINATED', $scope.process), 'info');
						$scope.loadProcessInstance();
					}
				}
			});
		};

		$scope.updateSelectedVariable = function() {
		    if ($scope.selectedVariables && $scope.selectedVariables.length > 0) {
		        var selectedVariable = $scope.selectedVariables[0];
		        var modalInstance = $modal.open({
                    templateUrl: 'views/update-variable-popup.html',
                	controller: 'UpdateVariableCrtl',
                	resolve: {
                	    variable: function () {
                		    return selectedVariable.variable;
                        },
                        processInstanceId : function() {
                            return $scope.process.id;
                        }
                    }
		        });

		        modalInstance.result.then(function (updated) {
		            if (updated == true) {
		                $scope.selectedVariables.splice(0, $scope.selectedVariables.length);
		                $scope.loadVariables();
		            }
                });
		    }
		};

		$scope.deleteVariable = function() {
            if ($scope.selectedVariables && $scope.selectedVariables.length > 0) {
                var selectedVariable = $scope.selectedVariables[0];
            	var modalInstance = $modal.open({
                    templateUrl: 'views/variable-delete-popup.html',
                    controller: 'DeleteVariableCrtl',
                    resolve: {
                        variable: function () {
                            return selectedVariable.variable;
                        },
                        processInstanceId : function() {
                            return $scope.process.id;
                        }
                    }
                });

            	modalInstance.result.then(function (updated) {
            	    if (updated == true) {
            		    $scope.selectedVariables.splice(0, $scope.selectedVariables.length);
            		    $scope.loadVariables();
                    }
                });
            }
		};

		$scope.addVariable = function() {
		    var modalInstance = $modal.open({
                templateUrl: 'views/variable-add-popup.html',
                controller: 'AddVariableCrtl',
                resolve: {
                    processInstanceId : function() {
                        return $scope.process.id;
                    }
                }
            });

            modalInstance.result.then(function (updated) {
                if (updated == true) {
                    $scope.selectedVariables.splice(0, $scope.selectedVariables.length);
                    $scope.loadVariables();
                }
            });
		};

		$scope.terminateProcessInstance = function() {
			$scope.deleteProcessInstance("terminate");
		};
}]);

activitiAdminApp.controller('DeleteProcessModalInstanceCrtl',
    ['$rootScope', '$scope', '$modalInstance', '$http', 'process','action', function ($rootScope, $scope, $modalInstance, $http, process, action) {

  $scope.process = process;
  $scope.action = action;
  $scope.status = {loading: false};
  $scope.model = {};
  $scope.ok = function () {
	  $scope.status.loading = true;

	  var dataForPost = {action: $scope.action};
	  if($scope.action == 'terminate' && $scope.model.deleteReason) {
		  dataForPost.deleteReason = $scope.model.deleteReason;
	  }

	  $http({method: 'POST', url: '/app/rest/activiti/process-instances/' + $scope.process.id,
		  data: dataForPost
      }).
        success(function(data, status, headers, config) {
            $modalInstance.close(true);
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

activitiAdminApp.controller('ShowProcessInstanceDiagramPopupCrtl',
    ['$rootScope', '$scope', '$modalInstance', '$http', 'process', '$timeout', function ($rootScope, $scope, $modalInstance, $http, process, $timeout) {

  $scope.model = {
      id: process.id,
      name: process.name
  };

  $scope.status = {loading: false};

  $scope.cancel = function () {
    if(!$scope.status.loading) {
      $modalInstance.dismiss('cancel');
    }
  };

  $timeout(function() {
    $("#bpmnModel").attr("data-instance-id", process.id);
    $("#bpmnModel").attr("data-definition-id", process.processDefinitionId);
    $("#bpmnModel").attr("data-server-id", $rootScope.activeServer.id);
    $("#bpmnModel").load("./display/displaymodel.html?instanceId=" + process.id);
  }, 200);


}]);

activitiAdminApp.controller('UpdateVariableCrtl',
    ['$rootScope', '$scope', '$modalInstance', '$http', 'variable', 'processInstanceId', function ($rootScope, $scope, $modalInstance, $http, variable, processInstanceId) {

    $scope.status = {loading: false};
    $scope.originalVariable = variable;

    $scope.updateVariable = {
        name: variable.name,
        value: variable.value,
        type: variable.type
    };

    $scope.executeUpdateVariable = function() {

        $scope.status.loading = true;

        var dataForPut = {
            name: $scope.updateVariable.name,
            type: $scope.updateVariable.type
        };

        if ($scope.updateVariable.value !== null || $scope.updateVariable.value !== undefined || $scope.updateVariable.value !== '') {

            if ($scope.updateVariable.type === 'string') {

                dataForPut.value = $scope.updateVariable.value;

            } else if ($scope.updateVariable.type === 'boolean') {

                if ($scope.updateVariable.value) {
                    dataForPut.value = true;
                } else {
                    dataForPut.value = false;
                }

            } else if ($scope.updateVariable.type === 'date') {

                dataForPut.value = $scope.updateVariable.value;

            } else if ($scope.updateVariable.type === 'double'
                || $scope.updateVariable.type === 'long'
                || $scope.updateVariable.type === 'integer'
                || $scope.updateVariable.type === 'short') {

                dataForPut.value = Number($scope.updateVariable.value);

            }

        } else {

            dataForPut.value = null;

        }

        $http({method: 'PUT', url: '/app/rest/activiti/process-instances/' + processInstanceId + '/variables/' + $scope.updateVariable.name, data: dataForPut}).
            success(function(data, status, headers, config) {
                $modalInstance.close(true);
                $scope.status.loading = false;
            }).
            error(function(data, status, headers, config) {
               	$modalInstance.close(false);
                $scope.status.loading = false;
            });

    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

}]);

activitiAdminApp.controller('DeleteVariableCrtl',
    ['$rootScope', '$scope', '$modalInstance', '$http', 'variable', 'processInstanceId',
    function ($rootScope, $scope, $modalInstance, $http, variable, processInstanceId) {

    $scope.status = {loading: false};
    $scope.variable = variable;

    $scope.deleteVariable = function() {
         $http({method: 'DELETE', url: '/app/rest/activiti/process-instances/' + processInstanceId + '/variables/' + $scope.variable.name}).
            success(function(data, status, headers, config) {
                $modalInstance.close(true);
                $scope.status.loading = false;
            }).
            error(function(data, status, headers, config) {
                $modalInstance.close(false);
                $scope.status.loading = false;
            });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

}]);

activitiAdminApp.controller('AddVariableCrtl',
    ['$rootScope', '$scope', '$modalInstance', '$http','processInstanceId',
    function ($rootScope, $scope, $modalInstance, $http, processInstanceId) {

    $scope.status = {loading: false};

    $scope.types = [
        "string",
        "boolean",
        "date",
        "double",
        "integer",
        "long",
        "short"
    ];

    $scope.newVariable = { };

    $scope.createVariable = function() {

        var data = {
            name: $scope.newVariable.name,
            type: $scope.newVariable.type,
        };

         if ($scope.newVariable.type === 'string') {

            data.value = $scope.newVariable.value;

         } else if ($scope.newVariable.type === 'boolean') {

            if ($scope.newVariable.value) {
                data.value = true;
            } else {
                data.value = false;
            }

         } else if ($scope.newVariable.type === 'date') {

            data.value = $scope.newVariable.value;

         } else if ($scope.newVariable.type === 'double'
                        || $scope.newVariable.type === 'long'
                        || $scope.newVariable.type === 'integer'
                        || $scope.newVariable.type === 'short') {

            data.value = Number($scope.newVariable.value);

         }

         $http({method: 'POST', url: '/app/rest/activiti/process-instances/' + processInstanceId + '/variables', data:data}).
            success(function(data, status, headers, config) {
                $modalInstance.close(true);
                $scope.status.loading = false;
            }).
            error(function(data, status, headers, config) {
                $modalInstance.close(false);
                $scope.status.loading = false;
            });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

}]);
