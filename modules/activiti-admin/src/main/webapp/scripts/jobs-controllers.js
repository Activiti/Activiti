/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('JobsController', ['$scope', '$rootScope', '$http', '$timeout','$location','$translate', '$q', 'gridConstants',
    function ($scope, $rootScope, $http, $timeout, $location, $translate, $q, gridConstants) {
		$rootScope.navigation = {selection: 'jobs'};
		
		$scope.jobData = {};
	    $scope.selectedJobs = [];

	    var filterConfig = {
		    	url: '/app/rest/activiti/jobs',
		    	method: 'GET',
		    	success: function(data, status, headers, config) {
		    		if ($scope.definitionCacheLoaded)
	                {
	                	$scope.processQueryResponse(data);
	                }
	                else
	                {
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
	                {name: 'JOBS.SORT.ID', id: 'id'},
	                {name: 'JOBS.SORT.DUE-DATE', id: 'dueDate'},
	                {name: 'JOBS.SORT.PROCESS-INSTANCE-ID', id: 'processInstanceId'}
	            ],
	            
	            supportedProperties: [
	                {id: 'processInstanceId', name: 'JOBS.FILTER.PROCESS-INSTANCE-ID'},
	                {id: 'tenantIdLike', name: 'JOBS.FILTER.TENANT-ID'},
	                {id: 'dueBefore', name: 'JOBS.FILTER.DUE-BEFORE', showByDefault: true},
	                {id: 'dueAfter', name: 'JOBS.FILTER.DUE-AFTER', showByDefault: true},
	                {id: 'withException', name: 'JOBS.FILTER.EXCEPTION'}
	            ]
		    };

    		
	    if ($rootScope.filters.forced.jobFilter) {
	    	// Always recreate the filter and add all properties
	    	$scope.filter = new ActivitiAdmin.Utils.Filter(filterConfig, $http, $timeout, $rootScope);
    		$rootScope.filters.jobFilter = $scope.filter;
    		
    		for (var prop in $rootScope.filters.forced.jobFilter) {
    			$scope.filter.addProperty({id: prop}, $rootScope.filters.forced.jobFilter[prop]);
    		}
    		
    		$rootScope.filters.forced.jobFilter = undefined;
    		
	    } else {
	    	if ($rootScope.filters && $rootScope.filters.jobFilter) {
	    		// Reuse the existing filter
	    		$scope.filter = $rootScope.filters.jobFilter;
	    		$scope.filter.config = filterConfig;
	    	} else {
	    		$scope.filter = new ActivitiAdmin.Utils.Filter(filterConfig, $http, $timeout, $rootScope);
	    		$rootScope.filters.jobFilter = $scope.filter;
	    	}
	    }
	    
	    $scope.jobSelected = function(job) {
	    	if (job && job.getProperty('id')) {
	    		$location.path('/job/' + job.getProperty('id'));
	    	}
	    };
	    
	    
	    $q.all([$translate('JOBS.HEADER.ID'), 
	            $translate('JOBS.HEADER.DUE-DATE'),
	            $translate('JOBS.HEADER.PROCESS-DEFINITION'),
	            $translate('JOBS.HEADER.RETRIES'),
	            $translate('JOBS.HEADER.EXCEPTION')])
	    .then(function(headers) { 

          // Config for grid
          $scope.gridJobs = {
              data: 'jobData.data',
              enableRowReordering: true,
              multiSelect: false,
              keepLastSelected : false,
              enableSorting: false,
              rowHeight: 36,
              afterSelectionChange: $scope.jobSelected,
              columnDefs: [
                  { field: 'id', displayName: headers[0], width: 50, cellTemplate: gridConstants.defaultTemplate},
                  { field: 'dueDate', displayName: headers[1], cellTemplate: gridConstants.dateTemplate},
                  { field: 'processDefinition.name', displayName: headers[2], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'retries', displayName: headers[3], width: 50, cellTemplate: gridConstants.defaultTemplate},
                  { field: 'exceptionMessage', displayName: headers[4], width: 150, cellTemplate: gridConstants.defaultTemplate}
              ]
          };
	     });
        
        $scope.processQueryResponse = function(jobsResponse) {
        	for (var i = 0; i < jobsResponse.data.length; i++)
            {
        		jobsResponse.data[i].processDefinition = 
            		$rootScope.getProcessDefinitionFromCache(jobsResponse.data[i].processDefinitionId);
            	
            }
        	$scope.jobData = jobsResponse;
        };
        
        $scope.processDefinitionFilterChanged = function() 
        {
        	if ($scope.filter.processDefinition && $scope.filter.processDefinition !== '-1')
        	{
        		$scope.filter.properties.processDefinitionId = $scope.filter.processDefinition;
        		$scope.filter.refresh();
        	}
        	else
        	{
        		var tempProcessDefinitionId = $scope.filter.properties.processDefinitionId;
        		$scope.filter.properties.processDefinitionId = null;
        		if (tempProcessDefinitionId && tempProcessDefinitionId.length > 0)
        		{
        			$scope.filter.refresh();
        		}
        	}
        };
        
        $scope.executeWhenReady(function() {
          $scope.filter.refresh();
        });
    }]);
