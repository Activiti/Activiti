/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('TasksController', ['$scope', '$rootScope', '$http', '$timeout','$location','$translate', '$q', 'gridConstants',
    function ($scope, $rootScope, $http, $timeout, $location, $translate, $q, gridConstants) {
		$rootScope.navigation = {selection: 'tasks'};
        
		$scope.taskData = {};
		$scope.selectedTasks = [];
	  
		$scope.variableFilterTypes = ActivitiAdmin.Utils.variableFilterTypes;
		$scope.variableFilterOperators = ActivitiAdmin.Utils.variableFilterOperators;

	    var filterConfig = {
		    	url: '/app/rest/activiti/tasks',
		    	method: 'POST',
		    	success: function(data, status, headers, config) {
	                $scope.taskData = data;
	            },
	            error: function(data, status, headers, config) {
	                if (data && data.message) {
	                    // Extract error-message
	                    $rootScope.addAlert(data.message, 'error');
	                } else {
	                    // Use default error-message
	                    $rootScope.addAlert($translate.instant('ALERT.GENERAL.HTTP-ERROR'), 'error');
	                }
	            },
	            
	            sortObjects: [
	                {name: 'TASKS.SORT.NAME', id: 'name'},
	                {name: 'TASKS.SORT.DUE-DATE', id: 'dueDate'},
	                {name: 'TASKS.SORT.START-TIME', id: 'start'},
	                {name: 'TASKS.SORT.END-TIME', id: 'endTime'},
	                {name: 'TASKS.SORT.PRIORITY', id: 'priority'}
	            ],
	            
	            options: {
	            	finished: [
	                 	{name: 'TASKS.FILTER.STATUS-ANY', value: ''},
	                 	{name: 'TASKS.FILTER.STATUS-ACTIVE', value: 'false'},
	                 	{name: 'TASKS.FILTER.STATUS-COMPLETE', value: 'true'}
	                ]
	            },
	            
	            supportedProperties: [
	                {id: 'finished', name: 'TASKS.FILTER.FINISHED', showByDefault: true},
	                {id: 'taskNameLike', name: 'TASKS.FILTER.NAME', showByDefault: true},
	                {id: 'taskAssignee', name: 'TASKS.FILTER.ASSIGNEE', showByDefault: true},
	                {id: 'taskOwner', name: 'TASKS.FILTER.OWNER'},
	                {id: 'parentTaskId', name: 'TASKS.FILTER.PARENT-TASK-ID'},
	                {id: 'processInstanceId', name: 'TASKS.FILTER.PROCESS-INSTANCE-ID'},
	                {id: 'tenantIdLike', name: 'TASKS.FILTER.TENANT-ID'},
	                {id: 'dueBefore', name: 'TASKS.FILTER.DUE-BEFORE'},
	                {id: 'dueAfter', name: 'TASKS.FILTER.DUE-AFTER'},
	                {id: 'taskCreatedBefore', name: 'TASKS.FILTER.CREATED-BEFORE'},
	                {id: 'taskCreatedAfter', name: 'TASKS.FILTER.CREATED-AFTER'},
	                {id: 'taskCompletedBefore', name: 'TASKS.FILTER.ENDED-BEFORE'},
	                {id: 'taskCompletedAfter', name: 'TASKS.FILTER.ENDED-AFTER'},
	                {id: 'processVariables', name: 'TASKS.FILTER.PROCESS-VARIABLE'},
	                {id: 'taskVariables', name: 'TASKS.FILTER.TASK-VARIABLE'}
	            ]
		    };

	    if($rootScope.filters.forced.taskFilter) {
	    	// Always recreate the filter and add all properties
	    	$scope.filter = new ActivitiAdmin.Utils.Filter(filterConfig, $http, $timeout, $rootScope);
    		$rootScope.filters.taskFilter = $scope.filter;
    		
    		for(var prop in $rootScope.filters.forced.taskFilter) {
    			$scope.filter.addProperty({id: prop}, $rootScope.filters.forced.taskFilter[prop]);
    		}
    		
    		$rootScope.filters.forced.taskFilter = undefined;
    		
	    } else {
	    	if($rootScope.filters && $rootScope.filters.taskFilter) {
	    		// Reuse the existing filter
	    		$scope.filter = $rootScope.filters.taskFilter;
	    		$scope.filter.config = filterConfig;
	    	} else {
	    		$scope.filter = new ActivitiAdmin.Utils.Filter(filterConfig, $http, $timeout, $rootScope);
	    		$rootScope.filters.taskFilter = $scope.filter;
	    	}
	    }
	    
	    if(!$scope.filter.properties.processvariables) {
        $scope.filter.properties.processVariables = [];
        $scope.filter.properties.taskVariables = [];
      }
      
      // Set value-filter callback to convert variables to nice format
      $scope.filter.config.valueFilter = function(prop, value) {
        if(prop == 'processVariables' || prop == 'taskVariables') {
          var actualValue = [];
          var variable;
          for(var i=0; i<value.length; i++) {
            variable = value[i];
            
            if(variable.name && variable.type && variable.value !== undefined && variable.value !== '' && variable.operator) {
              var varPayload =  {
                  name: variable.name,
                  value: variable.value,
                  operation: variable.operator.id,
                  type: variable.type.id
              };
              
              if(variable.type.id == 'long' || variable.type.id == 'short' || variable.type.id == 'double' || variable.type.id == 'integer') {
                varPayload.value = parseFloat(varPayload.value);
                if(varPayload.value != NaN) {
                  // Return valid value for number
                  actualValue.push(varPayload);
                }
              } else {
                // Return valid value
                actualValue.push(varPayload);
              }
            }
          }
          return actualValue;
        } else {
          return value;
        }
      };
      
      $scope.clearFilters = function() {
        $scope.filter.clear();
        $scope.filter.properties.processVariables = [];
        $scope.filter.properties.taskVariables = [];
      };
      
      $scope.setVariableFilterType = function(varFilter, type) {
        
        varFilter.value = undefined;
        if(type.id == 'boolean') {
          varFilter.value = true;
          
          if(varFilter.operator.id != 'equals') {
            varFilter.operator = $scope.variableFilterOperators[0];
          }
        }
        
        varFilter.type = type;
        $scope.highlightVariableValue(varFilter);
        $scope.filter.refresh();
      };
      
      $scope.setVariableFilterOperator = function(varFilter, operator) {
        if(operator.id == 'like') {
          varFilter.value = '';
          varFilter.type = $scope.variableFilterTypes[0];
        }
        
        if(varFilter.type.id == 'boolean' && operator.id != 'equals') {
          varFilter.operator = $scope.variableFilterOperators[0];
        } else {
          varFilter.operator = operator;
        }
        $scope.highlightVariableValue(varFilter);
        $scope.filter.refresh();
      };
      
      $scope.highlightVariableValue = function(varFilter) {
        var index = -1;
        for(var i=0; i<$scope.filter.properties[varFilter.scope].length; i++) {
          if(varFilter == $scope.filter.properties[varFilter.scope][i]) {
            index = i;
            break;
          }
        }
        
        $timeout(function() {
          var formField = $('#filter-' + varFilter.scope + '-value-' + index);
          formField.focus();
        }, 100);
      };
      
      $scope.addFilterProperty = function(prop) {
        
        if(prop.id != 'taskVariables' && prop.id != 'processVariables') {
          $scope.filter.addProperty(prop);
        } else {
          // Add additional variable
          $scope.filter.properties[prop.id].push({
            type: $scope.variableFilterTypes[0],
            operator: $scope.variableFilterOperators[0],
            scope: prop.id
          });
          
          $timeout(function() {
            var formField = $('#filter-' + prop.id + '-name-' + ($scope.filter.properties[prop.id].length - 1));
            formField.focus();
          }, 100);
        }
      };
	    
	    $scope.taskSelected = function(task) {
	    	if(task && task.getProperty('id')) {
	    		$location.path('/task/' + task.getProperty('id'));
	    	}
	    };
	    
	    $q.all([$translate('TASKS.HEADER.ID'), 
	            $translate('TASKS.HEADER.NAME'),
	            $translate('TASKS.HEADER.ASSIGNEE'),
	            $translate('TASKS.HEADER.OWNER'),
	            $translate('TASKS.HEADER.CREATE-TIME'),
	            $translate('TASKS.HEADER.END-TIME'),
	            $translate('TASKS.HEADER.PRIORITY')])
	    .then(function(headers) { 

          $scope.gridDefinitions = {
              data: 'taskData.data',
              enableRowReordering: true,
              multiSelect: false,
              keepLastSelected : false,
              enableSorting: false,
              rowHeight: 36,
              afterSelectionChange: $scope.taskSelected,
              columnDefs: [{ field: 'id', displayName: headers[0], width: 50, cellTemplate: gridConstants.defaultTemplate},
                  { field: 'name', displayName: headers[1], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'assignee', displayName: headers[2], width: 150, cellTemplate: gridConstants.defaultTemplate},
                  { field: 'owner', displayName: headers[3], width: 150, cellTemplate: gridConstants.defaultTemplate},
                  { field: 'startTime', displayName: headers[4], cellTemplate: gridConstants.dateTemplate},
                  { field: 'endTime', displayName: headers[5], cellTemplate: gridConstants.dateTemplate},
                  { field: 'priority', displayName: headers[6], cellTemplate: gridConstants.defaultTemplate}
              ]
          };
	    });
        
        // Task state filtering
        $scope.getTaskStateLabel = function(value) {
        	for(var i=0; i<$scope.filter.config.taskStates.length; i++) {
        		var state = $scope.filter.config.taskStates[i];
        		if(value == state.value) {
        			return state.name;
        		}
        	}
        	return $scope.filter.config.taskStates[0].name;
        };
        
        $scope.setStateFilter = function(state) {
        	if(state.value !== $scope.filter.properties.finished) {
        		$scope.filter.properties.finished = state.value;
        		$scope.filter.refresh();
        	}
        };
        
        $scope.executeWhenReady(function() {
          $scope.filter.refresh();
        });
    }]);
