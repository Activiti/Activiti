/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('UsersController', ['$scope', '$rootScope', '$http', '$timeout','$location', '$modal', '$translate', '$q',
    function ($scope, $rootScope, $http, $timeout, $location, $modal, $translate, $q) {
		$rootScope.navigation = {selection: 'users'};
		
		$scope.selectedUsers = [];

	    $q.all([$translate('USERS.HEADER.LOGIN'),
              $translate('USERS.HEADER.FIRSTNAME'),
              $translate('USERS.HEADER.LASTNAME'),
              $translate('USERS.HEADER.EMAIL'),
              $translate('USERS.HEADER.CLUSTER_USER')])
              .then(function(headers) {

                  // Config for grid
                  $scope.gridUsers = {
                      data: 'usersData',
                      enableRowReordering: true,
                      multiSelect: false,
                      keepLastSelected : false,
                      enableSorting: false,
                      rowHeight: 36,
                      selectedItems: $scope.selectedUsers,
                      columnDefs: [{ field: 'login', displayName: headers[0]},
                          { field: 'firstName', displayName: headers[1]},
                          { field: 'lastName', displayName: headers[2]},
                          { field: 'email', displayName: headers[3]},
                          { field: 'isClusterUser', displayName: headers[4]}
                      ]
                  };
        });

        $scope.loadUsers = function() {
        	$http({method: 'GET', url: '/app/rest/users'}).
		        success(function(data, status, headers, config) {
		        	$scope.usersData = data;

		        	// Indicate if the user is used for sending events
		        	if($scope.usersData !== null && $scope.usersData !== undefined) {
		        	    for (var userIndex = 0; userIndex < $scope.usersData.length; userIndex++) {
		        	        var userData = $scope.usersData[userIndex];
		        	        userData.isClusterUser = userData.clusterUser ? $translate.instant('GENERAL.YES') : $translate.instant('GENERAL.NO');
		        	    }
		        	}

		        }).
		        error(function(data, status, headers, config) {
		            console.log('Something went wrong when fetching users');
		        });
        };

        $scope.executeWhenReady(function() {
            $scope.loadUsers();
        });

        // Dialogs
		var resolve = {
			// Reference the current task
			user: function () {
			    return $scope.selectedUsers[0];
	        }
		};

		$scope.deleteUser = function() {
			var modalInstance = $modal.open({
				templateUrl: 'views/user-delete-popup.html',
				controller: 'DeleteUserModalInstanceCrtl',
				resolve: resolve
			});

			modalInstance.result.then(function (deleteUser) {
				if (deleteUser) {
				    $scope.addAlert($translate.instant('ALERT.USER.DELETED', $scope.selectedUsers[0]), 'info');

				    // Clear selection after delete, or actions will still point to deleted user
				    $scope.selectedUsers.splice(0,1);
				    $scope.loadUsers();
				}
			});
		};

		$scope.editUser = function() {
			var modalInstance = $modal.open({
				templateUrl: 'views/user-edit-popup.html',
				controller: 'EditUserModalInstanceCrtl',
				resolve: resolve
			});

			modalInstance.result.then(function (userUpdated) {
				if (userUpdated) {
				  $scope.addAlert($translate.instant('ALERT.USER.UPDATED', $scope.selectedUsers[0]), 'info');
					$scope.loadUsers();
				}
			});
		};

		$scope.changePassword = function() {
			var modalInstance = $modal.open({
				templateUrl: 'views/user-change-password-popup.html',
				controller: 'ChangePasswordModalInstanceCrtl',
				resolve: resolve
			});

			modalInstance.result.then(function (userUpdated) {
				if (userUpdated) {
				  $scope.addAlert($translate.instant('ALERT.USER.PASSWORD-CHANGED', $scope.selectedUsers[0]), 'info');
				}
			});
		};

		$scope.newUser = function() {
			var modalInstance = $modal.open({
				templateUrl: 'views/user-new-popup.html',
				controller: 'NewUserModalInstanceCrtl',
				resolve: resolve
			});

			modalInstance.result.then(function (userCreated) {
				if (userCreated) {
				  $scope.addAlert($translate.instant('ALERT.USER.CREATED', userCreated), 'info');
					$scope.loadUsers();
				}
			});
		};
    }]);

activitiAdminApp.controller('DeleteUserModalInstanceCrtl',
    ['$scope', '$modalInstance', '$http', 'user', function ($scope, $modalInstance, $http, user) {

  $scope.user = user;
  $scope.status = {loading: false};

  $scope.ok = function () {
	  $scope.status.loading = true;
	  $http({method: 'DELETE', url: '/app/rest/users/' + $scope.user.login}).
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

activitiAdminApp.controller('EditUserModalInstanceCrtl',
    ['$scope', '$modalInstance', '$http', 'user', function ($scope, $modalInstance, $http, user) {

  $scope.user = user;
  $scope.model = {
		  login: user.login,
		  firstName: user.firstName,
		  lastName: user.lastName,
		  email: user.email
  };

  $scope.status = {loading: false};

  $scope.ok = function () {
	  $scope.status.loading = true;
	  $http({method: 'PUT', url: '/app/rest/users/' + $scope.user.login, data: $scope.model}).
  	  success(function(data, status, headers, config) {
  		  $modalInstance.close(true);
  		  $scope.status.loading = false;
      }).error(function(data, status, headers, config) {
          $scope.status.loading = false;

          if(data.message) {
            $scope.model.errorMessage = data.message;
          }
      });
  };

  $scope.cancel = function () {
	if(!$scope.status.loading) {
		$modalInstance.dismiss('cancel');
	}
  };
}]);

activitiAdminApp.controller('ChangePasswordModalInstanceCrtl',
    ['$scope', '$modalInstance', '$http', 'user', function ($scope, $modalInstance, $http, user) {

	  $scope.user = user;
	  $scope.model = {
			  oldPassword: '',
			  newPassword: ''
	  };

	  $scope.status = {loading: false};

	  $scope.ok = function () {
		  $scope.status.loading = true;
		  $http({method: 'PUT', url: '/app/rest/users/' + $scope.user.login + '/change-password', data: $scope.model}).
	  	  success(function(data, status, headers, config) {
	  		  $modalInstance.close(true);
	  		  $scope.status.loading = false;
	      }).error(function(data, status, headers, config) {
	        $scope.status.loading = false;

	        if(data.message) {
	          $scope.model.errorMessage = data.message;
	        }
	      });
	  };

	  $scope.cancel = function () {
		if(!$scope.status.loading) {
			$modalInstance.dismiss('cancel');
		}
	  };
	}]);

activitiAdminApp.controller('NewUserModalInstanceCrtl',
    ['$scope', '$modalInstance', '$http', function ($scope, $modalInstance, $http) {

  $scope.model = {
		  login: '',
		  password: '',
		  firstName: '',
		  lastName: '',
		  email: ''
  };

  $scope.status = {loading: false};

  $scope.ok = function () {
	  $scope.status.loading = true;
	  $http({method: 'POST', url: '/app/rest/users', data: $scope.model, ignoreErrors: true}).
  	  success(function(data, status, headers, config) {
  		  $modalInstance.close($scope.model);
  		  $scope.status.loading = false;
      }).error(function(data, status, headers, config) {
    	  $scope.status.loading = false;

    	  if(data.message) {
    	    $scope.model.errorMessage = data.message;
    	  }
      });
  };

  $scope.cancel = function () {
	if(!$scope.status.loading) {
		$modalInstance.dismiss('cancel');
	}
  };
}]);
