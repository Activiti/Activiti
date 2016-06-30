/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('ProcessInstancesController', ['$rootScope', '$scope', '$http', '$timeout', '$location', '$translate', '$q', 'gridConstants',
    function ($rootScope, $scope, $http, $timeout, $location, $translate, $q, gridConstants) {

		$rootScope.navigation = {selection: 'process-instances'};
		
		$scope.filter = {};
		$scope.processInstances = {};
		$scope.definitionCacheLoaded = false;

		$scope.variableFilterTypes = ActivitiAdmin.Utils.variableFilterTypes;
		$scope.variableFilterOperators = ActivitiAdmin.Utils.variableFilterOperators;

	    var filterConfig = {
	    	url: '/app/rest/activiti/process-instances',
	    	method: 'POST',
	    	success: function(data, status, headers, config) {
	    		if ($scope.definitionCacheLoaded) {
                	$scope.processQueryResponse(data);
                }
                else {
	                $rootScope.loadProcessDefinitionsCache().then(function(promise) {
	        			$rootScope.processDefinitionsCache = promise.data;

	        			$scope.definitionCacheLoaded = true;
	        			$scope.processQueryResponse(data);
	        		});
                }
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
                {name: 'PROCESS-INSTANCES.SORT.ID', id: 'processInstanceId'},
                {name: 'PROCESS-INSTANCES.SORT.START-TIME', id: 'startTime'}
            ],

            supportedProperties: [
                {id: 'finished', name: 'PROCESS-INSTANCES.FILTER.STATUS', showByDefault: true},
                {id: 'processBusinessKey', name: 'PROCESS-INSTANCES.FILTER.BUSINESS-KEY'},
                {id: 'superProcessInstanceId', name: 'PROCESS-INSTANCES.FILTER.SUPER-PROCESS-INSTANCE-ID'},
                {id: 'startedBefore', name: 'PROCESS-INSTANCES.FILTER.STARTED-BEFORE'},
                {id: 'startedAfter', name: 'PROCESS-INSTANCES.FILTER.STARTED-AFTER'},
                {id: 'finishedBefore', name: 'PROCESS-INSTANCES.FILTER.ENDED-BEFORE'},
                {id: 'finishedAfter', name: 'PROCESS-INSTANCES.FILTER.ENDED-AFTER'},
                {id: 'variable', name: 'PROCESS-INSTANCES.FILTER.VARIABLE'},
                {id: 'tenantIdLike', name: 'PROCESS-INSTANCES.FILTER.TENANT-ID'}
            ]
	    };

	    if ($rootScope.filters.forced.instanceFilter) {
	    	// Always recreate the filter and add all properties
	    	$scope.filter = new ActivitiAdmin.Utils.Filter(filterConfig, $http, $timeout, $rootScope);
    		$rootScope.filters.instanceFilter = $scope.filter;

    		for(var prop in $rootScope.filters.forced.instanceFilter) {
    			$scope.filter.addProperty({id: prop}, $rootScope.filters.forced.instanceFilter[prop]);
    		}

    		$rootScope.filters.forced.instanceFilter = undefined;

	    } else if ($rootScope.filters && $rootScope.filters.instanceFilter) {
	    	// Reuse the existing filter
	    	 $scope.filter = $rootScope.filters.instanceFilter;
	    	 $scope.filter.config = filterConfig;

	    } else {
		    $scope.filter = new ActivitiAdmin.Utils.Filter(filterConfig, $http, $timeout, $rootScope);
		    $rootScope.filters.instanceFilter = $scope.filter;
	    }

	    $scope.processInstanceSelected = function(processInstance) {
        if (processInstance && processInstance.getProperty('id')) {
          $location.path('/process-instance/' + processInstance.getProperty('id'));
        }
      };

	    if(!$scope.filter.properties.variables) {
	      $scope.filter.properties.variables = [];
	    }

	    // Set value-filter callback to convert variables to nice format
	    $scope.filter.config.valueFilter = function(prop, value) {
	      if (prop == 'variables') {
	        var actualValue = [];
	        var variable;
	        for (var i=0; i<value.length; i++) {
	          variable = value[i];

	          if (variable.name && variable.type && variable.value !== undefined && variable.value !== '' && variable.operator) {
	              var varPayload =  {
	                  name: variable.name,
	                  value: variable.value,
	                  operation: variable.operator.id,
	                  type: variable.type.id
	              };

	              if (variable.type.id == 'long' || variable.type.id == 'short' || variable.type.id == 'double' || variable.type.id == 'integer') {
	                  varPayload.value = parseFloat(varPayload.value);
	                  if (varPayload.value != NaN) {
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
	      $scope.filter.properties.variables = [];
	    };

	    $scope.setVariableFilterType = function(varFilter, type) {

	      varFilter.value = undefined;
	      if (type.id == 'boolean') {
	        varFilter.value = true;

	        if (varFilter.operator.id != 'equals') {
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
        for(var i=0; i<$scope.filter.properties.variables.length; i++) {
          if(varFilter == $scope.filter.properties.variables[i]) {
            index = i;
            break;
          }
        }

        $timeout(function() {
          var formField = $('#filter-variable-value-' + index);
          formField.focus();
        }, 100);
	    };

	    $scope.addFilterProperty = function(prop) {
	      if(prop.id != 'variable') {
	        $scope.filter.addProperty(prop);
	      } else {
	        // Add additional variable
	        $scope.filter.properties.variables.push({
	          type: $scope.variableFilterTypes[0],
	          operator: $scope.variableFilterOperators[0],
	          scope: 'variable'
	        });

	        $timeout(function() {
	          var formField = $('#filter-variable-name-' + ($scope.filter.properties.variables.length - 1));
	          formField.focus();
	        }, 100);
	      }
	    };

	    $q.all([$translate('PROCESS-INSTANCES.HEADER.ID'),
              $translate('PROCESS-INSTANCES.HEADER.BUSINESS-KEY'),
              $translate('PROCESS-INSTANCES.HEADER.PROCESS-DEFINITION'),
              $translate('PROCESS-INSTANCES.HEADER.CREATE-TIME'),
              $translate('PROCESS-INSTANCES.HEADER.END-TIME'),])
              .then(function(headers) {

          $scope.gridInstances = {
              data: 'processInstances.data',
              enableRowReordering: true,
              multiSelect: false,
              keepLastSelected : false,
              rowHeight: 36,
              afterSelectionChange: $scope.processInstanceSelected,
              columnDefs: [
                  { field: 'id', displayName: headers[0], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'businessKey', displayName: headers[1], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'processDefinition.name', displayName: headers[2], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'startTime', displayName: headers[3], cellTemplate: gridConstants.dateTemplate},
                  { field: 'endTime', displayName: headers[4], cellTemplate: gridConstants.dateTemplate}]
          };
        });

        $scope.processQueryResponse = function(processInstancesResponse) {
        	for (var i = 0; i < processInstancesResponse.data.length; i++) {
				processInstancesResponse.data[i].processDefinition =
            		$rootScope.getProcessDefinitionFromCache(processInstancesResponse.data[i].processDefinitionId);

				// Fallback to id, of process definition doesn't have a name (getProcessDefinitionFromCache returns null if not found)
				if ((processInstancesResponse.data[i].processDefinition === null || processInstancesResponse.data[i].processDefinition === undefined) && processInstancesResponse.data[i].processDefinitionId) {
				    processInstancesResponse.data[i].processDefinition = { id: processInstancesResponse.data[i].processDefinitionId, name: processInstancesResponse.data[i].processDefinitionId }
				}

            }
			$scope.processInstances = processInstancesResponse;
        };

        $scope.processDefinitionFilterChanged = function() {
        	if ($scope.filter.processDefinition && $scope.filter.processDefinition !== '-1') {
        		$scope.filter.properties.processDefinitionId = $scope.filter.processDefinition;
        		$scope.filter.refresh();
        	}
        	else {
        		var tempProcessDefinitionId = $scope.filter.properties.processDefinitionId;
        		$scope.filter.properties.processDefinitionId = null;
        		if (tempProcessDefinitionId && tempProcessDefinitionId.length > 0) {
        			$scope.filter.refresh();
        		}
        	}
        };

        $scope.executeWhenReady(function() {
          $scope.filter.refresh();
        });

    }]);
