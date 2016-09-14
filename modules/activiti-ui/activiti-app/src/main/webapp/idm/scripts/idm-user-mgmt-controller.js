/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Controller for user mgmt
 */
activitiApp.controller('IdmUserMgmtController', ['$rootScope', '$scope', '$translate', '$http', '$timeout','$location', '$modal',
    function ($rootScope, $scope, $translate, $http, $timeout, $location, $modal) {

        if (!$scope.hasAdminCapability()) {
            $scope.backToLanding();
        }

        $rootScope.setMainPageById('userMgmt');

        $scope.model = {
            loading: false,
            sorts: [
                {id: 'idAsc', name: $translate.instant('IDM.USER-MGMT.FILTERS.SORT-ID-A')},
                {id: 'idDesc', name: $translate.instant('IDM.USER-MGMT.FILTERS.SORT-ID-Z')},
                {id: 'emailAsc', name: $translate.instant('IDM.USER-MGMT.FILTERS.SORT-EMAIL-A')},
                {id: 'emailDesc', name: $translate.instant('IDM.USER-MGMT.FILTERS.SORT-EMAIL-Z')}
            ],
            waiting: false,
            delayed: false,
            selectedUsers: {},
            selectedUserCount: 0,
            start: 0
        };

        $scope.model.activeSort = $scope.model.sorts[0];

        $scope.clearSelectedUsers = function() {
            $scope.model.selectedUsers = {};
            $scope.model.selectedUserCount = 0;
        };

        $scope.loadUsers = function() {
            $scope.clearSelectedUsers();
            $scope.model.loading = true;
            var params = {
                filter: $scope.model.pendingFilterText,
                company: $scope.model.pendingCompanyText,
                sort: $scope.model.activeSort.id,
                start: $scope.model.start
            };

            $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/users', params: params}).
                success(function(data, status, headers, config) {
                    data.moreUsers = data.start + data.size < data.total;
                    $scope.model.users = data;
                    $scope.model.loading = false;
                }).
                error(function(data, status, headers, config) {
                    $scope.model.loading = false;

                    if(status == 403) {
                        console.log('Forbidden!');
                    }
                });
        };


        $scope.refreshDelayed = function() {
            // If already waiting, another wait-cycle will be done
            // after the current wait is over
            if($scope.model.waiting) {
                $scope.model.delayed = true;
            } else {
                $scope.scheduleDelayedRefresh();
            }
        };

        $scope.scheduleDelayedRefresh = function() {
            $scope.model.waiting = true;

            $timeout(function() {
                $scope.model.waiting = false;
                if( $scope.model.delayed) {
                    $scope.model.delayed = false;
                    // Delay again
                    $scope.scheduleDelayedRefresh();
                } else {
                    // Actually do the refresh-call, after resetting start
                    $scope.model.start = 0;
                    $scope.loadUsers();
                }
            }, 100);
        };

        $scope.showNextUsers = function() {
            if($scope.model.users) {
                $scope.model.start = $scope.model.users.start + $scope.model.users.size;
                $scope.loadUsers();
            }
        };

        $scope.showPreviousUsers = function() {
            if($scope.model.users) {
                $scope.model.start = Math.max(0, $scope.model.users.start - $scope.model.users.size);
                $scope.loadUsers();
            }
        };

        $scope.activateSort = function(sort) {
            $scope.model.activeSort = sort;
            $scope.model.start = 0;
            $scope.loadUsers();
        };

        $scope.toggleUserSelection = function(user) {
            if($scope.model.selectedUsers[user.id]) {
                delete $scope.model.selectedUsers[user.id];
                $scope.model.selectedUserCount -= 1;
            }  else {
                $scope.model.selectedUsers[user.id] = true;
                $scope.model.selectedUserCount +=1;
            }

        };

        $scope.addUser = function() {
            $scope.model.errorMessage = undefined;
            $scope.model.user = undefined;
            $scope.model.mode = 'create';
            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-user-create.html?version=' + new Date().getTime(),
                show: true
            }, $modal, $scope);
        };

        $scope.editUserAccountType = function() {

            $scope.model.mode = 'type';

            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-user-type-edit.html',
                show: true
            }, $modal, $scope);

        };

        $scope.editUserDetails = function() {

            $scope.model.user = undefined;
            $scope.model.mode = 'edit';
            var selectedUsers = $scope.getSelectedUsers();
            if (selectedUsers && selectedUsers.length == 1) {
                $scope.model.user = selectedUsers[0];
            }

            $scope.model.errorMessage = undefined;
            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-user-create.html?version=' + new Date().getTime(),
                show: true
            }, $modal, $scope);
        };

        $scope.editUserPassword = function() {

            $scope.model.mode = 'password';

            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-user-password-change.html',
                show: true
            }, $modal, $scope);

        };

        $scope.deleteUsers = function() {
            $scope.model.loading = true;
            $scope.getSelectedUsers().forEach(function(selectedUser) {
                $http({method: 'DELETE', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/users/' + selectedUser.id}).
                    success(function (data, status, headers, config) {

                        $rootScope.addAlert('User deleted', 'info');
                        $scope.loadUsers();

                        $scope.model.loading = false;
                    }).
                    error(function (data, status, headers, config) {
                        $scope.model.loading = false;
                        if (data && data.message) {
                            $rootScope.addAlert(data.message, 'error');
                        } else {
                            $rootScope.addAlert('Error while deleting user', 'error');
                        }
                    });
            });
        };

        $scope.getSelectedUsers = function() {
            var selected = [];
            for(var i = 0; i<$scope.model.users.size; i++) {
                var user = $scope.model.users.data[i];
                if(user) {
                    for(var prop in $scope.model.selectedUsers) {
                        if(user.id == prop) {
                            selected.push(user);
                            break;
                        }
                    }
                }
            }

            return selected;
        };

        $scope.loadUsers();

    }]);


/**
 * Controller for the create user dialog
 */
activitiApp.controller('IdmCreateUserPopupController', ['$rootScope', '$scope', '$http',
    function ($rootScope, $scope, $http) {

        if (!$scope.hasAdminCapability()) {
            $scope.backToLanding();
        }


        if ($scope.model.user === null || $scope.model.user === undefined) {
            $scope.model.user = {};
        }

        $scope.createNewUser = function () {
            if (!$scope.model.user.id) {
                return;
            }

            var model = $scope.model;
            model.loading = true;

            var data = {
                id: model.user.id,
                email: model.user.email,
                firstName: model.user.firstName,
                lastName: model.user.lastName,
                password: model.user.password,
            };

            $http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/users', data: data}).
                success(function (data, status, headers, config) {

                    $rootScope.addAlert('New user created', 'info');
                    $scope.loadUsers();

                    $scope.model.loading = false;
                    $scope.$hide();
                }).
                error(function (data, status, headers, config) {
                    $scope.model.loading = false;
                    if (data && data.message) {
                        $rootScope.addAlert(data.message, 'error');
                    } else {
                        $rootScope.addAlert('Error while updating user status', 'error');
                    }

                    if (status == 403) {
                        $scope.model.errorMessage = "Forbidden";
                    } else if (status == 409) {
                        $scope.model.errorMessage = "A user with that email address already exists";
                    } else {
                        $scope.$hide();
                    }
                });
        };

        $scope.editUserDetails = function() {
            if (!$scope.model.user.id) {
                return;
            }

            var model = $scope.model;
            model.loading = true;

            var data = {
                id: model.user.id,
                email: model.user.email,
                firstName: model.user.firstName,
                lastName: model.user.lastName,
            };

            $http({method: 'PUT', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/users/' + $scope.model.user.id, data: data}).
                success(function (data, status, headers, config) {

                    $scope.loadUsers();

                    $scope.model.loading = false;
                    $scope.$hide();
                }).
                error(function (data, status, headers, config) {
                    $scope.model.loading = false;
                    if (data && data.message) {
                        $rootScope.addAlert(data.message, 'error');
                    } else {
                        $rootScope.addAlert('Error while updating user status', 'error');
                    }

                    if (status == 403) {
                        $scope.model.errorMessage = "Forbidden";
                    } else if (status == 409) {
                        $scope.model.errorMessage = "A user with that email address already exists";
                    } else {
                        $scope.$hide();
                    }
                });
        };

        $scope.cancel = function () {
            if (!$scope.model.loading) {
                $scope.$hide();
            }
        };

    }]);

/**
 * Controller for the bulk update dialog
 */
activitiApp.controller('IdmUserBulkUpdatePopupController', ['$rootScope', '$scope', '$http',
  function ($rootScope, $scope, $http) {

      if (!$scope.hasAdminCapability()) {
          $scope.backToLanding();
      }

      if ($scope.model.mode == 'password') {
          $scope.model.updateUsers = {
              password: ''
          };
      }

     $scope.updateUsers = function () {
       $scope.model.loading = true;
       var users = $scope.getSelectedUsers();
       var userIds = [];
       for(var i=0; i<users.length; i++) {
         var user = users[i];
         if(user && user.id) {
           userIds.push(user.id);
         }
       }

       var data = {
           users: userIds
       };

       if ($scope.model.mode == 'password') {
         data.password = $scope.model.updateUsers.password;
       }

       $http({method: 'PUT', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/users', data: data})
           .success(function(data, status, headers, config) {
                $scope.$hide();
                $scope.model.loading = false;
                $rootScope.addAlert($scope.model.selectedUserCount + ' user(s) updated', 'info');
                $scope.loadUsers();

         }).
         error(function(data, status, headers, config) {
            $scope.model.loading = false;
            if(data && data.message) {
              $rootScope.addAlert(data.message, 'error');
            } else {
              $rootScope.addAlert('Error while updating user status', 'error');
            }
            $scope.$hide();
            if(status == 403) {
                console.log('Not permitted!');
            }
         });
    };

    $scope.setStatus = function(newStatus) {
      $scope.model.updateUsers.status = newStatus;
    };

    $scope.setType = function(newType) {
      $scope.model.updateUsers.type = newType;
    };

    $scope.cancel = function () {
      if(!$scope.model.loading) {
        $scope.$hide();
      }
    };

}]);

