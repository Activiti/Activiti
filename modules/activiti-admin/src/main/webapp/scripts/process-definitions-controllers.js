/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('ProcessDefinitionsController', ['$rootScope', '$scope', '$http', '$timeout', '$location','$translate', '$q', '$modal', 'gridConstants',
    function ($rootScope, $scope, $http, $timeout, $location, $translate, $q, $modal, gridConstants) {

		$rootScope.navigation = {selection: 'process-definitions'};
        
		$scope.filter = {};
		$scope.processDefinitionsData = {};

		// Array to contain selected properties (yes - we only can select one, but ng-grid isn't smart enough)
	    $scope.selectedDefinitions = [];

	    var filterConfig = {
	    	url: '/app/rest/activiti/process-definitions',
	    	method: 'GET',
	    	success: function(data, status, headers, config) {
	    		$scope.processDefinitionsData = data;
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
                {name: 'PROCESS-DEFINITIONS.SORT.ID', id: 'id'},
                {name: 'PROCESS-DEFINITIONS.SORT.NAME', id: 'name'},
                {name: 'PROCESS-DEFINITIONS.SORT.VERSION', id: 'version'}
            ],

            supportedProperties: [
                {id: 'nameLike', name: 'PROCESS-DEFINITIONS.FILTER.NAME', showByDefault: false},
                {id: 'keyLike', name: 'PROCESS-DEFINITIONS.FILTER.KEY', showByDefault: true},
                {id: 'deploymentId', name: 'PROCESS-DEFINITIONS.FILTER.DEPLOYMENT-ID', showByDefault: false},
                {id: 'tenantIdLike', name: 'PROCESS-DEFINITIONS.FILTER.TENANT-ID', showByDefault: false},
                {id: 'latest', name: 'PROCESS-DEFINITIONS.FILTER.LATEST', showByDefault: true, defaultValue: true}
            ]
	    };

	    if ($rootScope.filters.forced.processDefinitionFilter) {
	        // Always recreate the filter and add all properties
	        $scope.filter = new ActivitiAdmin.Utils.Filter(filterConfig, $http, $timeout, $rootScope);
	        $rootScope.filters.processDefinitionFilter = $scope.filter;

	        for (var prop in $rootScope.filters.forced.processDefinitionFilter) {
	            $scope.filter.addProperty({id: prop}, $rootScope.filters.forced.processDefinitionFilter[prop]);
	            if (prop == 'deploymentId') {
	                $scope.filter.removeProperty({id: 'latest'});
	            }
	        }

	        $rootScope.filters.forced.processDefinitionFilter = undefined;

	    } else if ($rootScope.filters && $rootScope.filters.processDefinitionFilter) {
	    	// Reuse the existing filter
	    	$scope.filter = $rootScope.filters.processDefinitionFilter;
	    	$scope.filter.config = filterConfig;
	    } else {
		    $scope.filter = new ActivitiAdmin.Utils.Filter(filterConfig, $http, $timeout, $rootScope);
		    $rootScope.filters.processDefinitionFilter = $scope.filter;
	    }

	    $scope.definitionSelected = function(definition) {
	    	if (definition && definition.getProperty('id')) {
	    		$location.path('/process-definition/' + definition.getProperty('id'));
	    	}
	    };

	    $scope.toggleLatestVersion = function() {
	      if($scope.filter.properties.latest === true) {
	        $scope.filter.properties.nameLike = undefined;
	        $scope.filter.properties.deploymentId = undefined;
	      }

	      $scope.filter.refresh();
	    };

	    $q.all([$translate('PROCESS-DEFINITIONS.HEADER.ID'),
              $translate('PROCESS-DEFINITIONS.HEADER.NAME'),
              $translate('PROCESS-DEFINITIONS.HEADER.VERSION'),
              $translate('PROCESS-DEFINITIONS.HEADER.KEY'),
              $translate('PROCESS-DEFINITIONS.HEADER.TENANT')])
              .then(function(headers) {

          // Config for grid
          $scope.gridDefinitions = {
              data: 'processDefinitionsData.data',
              enableRowReordering: true,
              multiSelect: false,
              keepLastSelected : false,
              rowHeight: 36,
              selectedItems: $scope.selectedDefinitions,
              afterSelectionChange: $scope.definitionSelected,
              columnDefs: [
                  { field: 'id', displayName: headers[0], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'name', displayName: headers[1], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'version', displayName: headers[2], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'key', displayName: headers[3], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'tenantId', displayName: headers[4], cellTemplate: gridConstants.defaultTemplate}]
          };
       });


	    // Hook in initial fetching of the definitions
	     $scope.executeWhenReady(function() {
	       $scope.filter.refresh();
	     });
        

    }]);
