/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

activitiAdminApp.controller('JobController', ['$scope', '$rootScope', '$http', '$timeout','$location','$routeParams', '$modal', '$translate',
    function ($scope, $rootScope, $http, $timeout, $location, $routeParams, $modal, $translate) {
		$rootScope.navigation = {selection: 'jobs'};
		
		$scope.returnToList = function() {
			$location.path("/jobs");
		};

		$scope.openDefinition = function(definitionId) {
			if (definitionId) {
				$location.path("/process-definition/" + definitionId);
			}
		};

		$scope.executeJob = function() {
			$http({method: 'POST', url: '/app/rest/activiti/jobs/' + $scope.job.id}).
        	success(function(data, status, headers, config) {
        	  $scope.addAlert($translate.instant('ALERT.JOB.EXECUTED', $scope.job), 'info');
        		$scope.returnToList();
        	})
        	.error(function(data, status, headers, config) {
        		$scope.loadJob();
        	});
		};

		$scope.deleteJob = function() {
			var modalInstance = $modal.open({
				templateUrl: 'views/job-delete-popup.html',
				controller: 'DeleteModalInstanceCrtl',
				resolve: {
					job: function() {
						return $scope.job;
					}
				}
			});

			modalInstance.result.then(function (deletejob) {
				if(deletejob) {
					$scope.addAlert($translate.instant('ALERT.JOB.DELETED', $scope.job), 'info');
					$scope.returnToList();
				}
			});
		};

		$scope.openProcessInstance = function(processInstanceId) {
			if (processInstanceId) {
				$location.path("/process-instance/" + processInstanceId);
			}
		};

		$scope.openProcessDefinition = function(processDefinitionId) {
			if (processDefinitionId) {
				$location.path("/process-definition/" + processDefinitionId);
			}
		};

		$scope.loadJob = function() {
			$scope.job = {};
			$http({method: 'GET', url: '/app/rest/activiti/jobs/' + $routeParams.jobId}).
	    	success(function(data, status, headers, config) {
	            $scope.job = data;

	            if($scope.job.exceptionMessage) {
	            	// Fetch the full stacktrace, associated with this job
	            	$http({method: 'GET', url: '/app/rest/activiti/jobs/' + $scope.job.id + "/stacktrace"}).
	            	success(function(data, status, headers, config) {
	    	            $scope.job.exceptionStack = data;
	            	});
	            }
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

		// Load job
		$scope.executeWhenReady(function() {
		    $scope.loadJob();
		});

}]);

activitiAdminApp.controller('DeleteModalInstanceCrtl',
    ['$rootScope', '$scope', '$modalInstance', '$http', 'job', function ($rootScope, $scope, $modalInstance, $http, job) {

	  $scope.job = job;
	  $scope.status = {loading: false};

	  $scope.ok = function () {
		  $scope.status.loading = true;
		  $http({method: 'DELETE', url: '/app/rest/activiti/jobs/' + $scope.job.id}).
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
