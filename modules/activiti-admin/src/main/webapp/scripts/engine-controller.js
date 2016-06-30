/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('EngineController', ['$rootScope', '$scope', '$http', '$timeout','$modal', '$translate',
    function ($rootScope, $scope, $http, $timeout, $modal, $translate) {

        // Set root navigation
		$rootScope.navigation = {selection: 'engine'};

        // Static data
        $scope.options = {
          schemaUpdate : ['true', 'false'],
          history : ['none', 'activity', 'audit', 'full']
        };

        // Empty model
        $scope.model = {};

        // Show popup to edit the Activiti endpoint
		$scope.editEndpointConfig = function() {
		    if ($scope.activeServer) {
		        showEditpointConfigModel($scope.activeServer);
		    } else {
		        // load default endpoint configs properties
		        $http({method: 'GET', url: '/app/rest/server-configs/default'}).
	            success(function(defaultServerconfig, status, headers, config) {
	                defaultServerconfig.clusterConfigId = $scope.activeCluster.id;
	                showEditpointConfigModel(defaultServerconfig);
	            });
		    }
			
		    function showEditpointConfigModel(server) {
		        var cloneOfModel = {};
	            for(var prop in server) {
	                cloneOfModel[prop] = server[prop];
	            }

	            var modalInstance = $modal.open({
	                templateUrl: 'views/engine-edit-endpoint-popup.html',
	                controller: 'EditEndpointConfigModalInstanceCrtl',
	                resolve: {
	                    server: function() {return cloneOfModel;}
	                }
	            });

	            modalInstance.result.then(function (result) {
	                if(result) {
	                  $scope.addAlert($translate.instant('ALERT.ENGINE.ENDPOINT-UPDATED', result), 'info');
	                    $rootScope.activeServer = result;
	                }
	            });
		    }
		};

		$scope.checkEndpointConfig = function() {
			$http({method: 'GET', url: '/app/rest/activiti/engine-info', ignoreErrors: true}).
        	success(function(data, status, headers, config) {
        	  $scope.addAlert($translate.instant('ALERT.ENGINE.ENDPOINT-VALID', data), 'info');
            }).error(function(data, status, headers, config) {
              $scope.addAlert($translate.instant('ALERT.ENGINE.ENDPOINT-INVALID',  $rootScope.activeServer), 'error');
            });
		};
    }]);


activitiAdminApp.controller('EditEndpointConfigModalInstanceCrtl',
    ['$scope', '$modalInstance', '$http', 'server', function ($scope, $modalInstance, $http, server) {

	$scope.model = {server: server};

	$scope.status = {loading: false};

    $scope.ok = function () {
      $scope.status.loading = true;

      delete $scope.model.error;

      var serverConfigUrl = '/app/rest/server-configs';
      var method = 'PUT';
      if ($scope.model.server && $scope.model.server.id) {
          serverConfigUrl += '/' + $scope.model.server.id;
      } else {
          method = 'POST';
      }

      $http({method: method, url: serverConfigUrl, data: $scope.model.server}).
          success(function(data, status, headers, config) {
              $scope.status.loading = false;
              $modalInstance.close($scope.model.server);
          }).
          error(function(data, status, headers, config) {
              $scope.status.loading = false;
              $scope.model.error = {
                  statusCode: status,
                  message: data
              };
           });
    };

    $scope.cancel = function () {
        if(!$scope.status.loading) {
            $modalInstance.dismiss('cancel');
        }
    };
        
}]);
