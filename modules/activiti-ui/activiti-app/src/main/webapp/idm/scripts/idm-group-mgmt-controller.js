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
activitiApp.controller('GroupMgmtController', ['$rootScope', '$scope', '$translate', '$http', '$timeout','$location', '$modal', '$popover', 'IdmService',
    function ($rootScope, $scope, $translate, $http, $timeout, $location, $modal, $popover, IdmService) {

        if (!$scope.hasAdminCapability()) {
            $scope.backToLanding();
        }

        var validUser = true;

        $rootScope.setMainPageById('functionalGroupMgmt');

        $scope.model = {
            loading: true,
            expanded: {}
        };

        $scope.showCreateGroupPopup = function() {
            $scope.model.editedGroup  = {};
            $scope.model.mode = 'create';
            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-group-create.html',
                show: true
            }, $modal, $scope);
        };

        var fetchUserPage = function() {
            $scope.model.loadingUsers = true;
            IdmService.getUsersForGroup($scope.model.selectedGroup.id, $scope.model.userFilter ,$scope.model.userPage, $scope.model.pageSize).then(function(data) {
                $scope.model.users = data;
                $scope.model.moreUsers = $scope.model.selectedGroup.userCount > (($scope.model.userPage+1 ) * $scope.model.pageSize);
                $scope.model.loadingUsers = false;
            });
        };

        $scope.selectGroup = function(groupId) {
            $scope.model.loadingGroup = true;
            IdmService.getGroup(groupId).then(function (data) {
                $scope.model.selectedGroup = data;

                $scope.model.userPage = 0;
                $scope.model.pageSize = 50;
                fetchUserPage();

                $scope.model.loadingGroup = false;
            });
        };

        $scope.showPreviousUsers = function() {
            $scope.model.userPage--;
            fetchUserPage();
        };

        $scope.showNextUsers = function() {
            $scope.model.userPage++;
            fetchUserPage();
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
                    $scope.model.userPage = 0;
                    fetchUserPage();
                }
            }, 100);
        };

        $scope.createGroup = function() {
            $scope.model.loading = true;
            IdmService.createGroup($scope.model.editedGroup).then(function (data) {
                $scope.fetchGroups(data.id);
                $scope.model.loading = false;
            });
        };

        $scope.updateGroup = function() {
            $scope.model.loadingGroup = true;
            IdmService.updateGroup($scope.model.editedGroup.id, $scope.model.editedGroup).then(function (data) {
                $scope.model.selectedGroup = data;

                // Find the entry in the list on the left, and update its name
                for (var i=0; i<$scope.model.groups.length; i++){
                    if ($scope.model.groups[i].id === data.id) {
                        $scope.model.groups[i].name = data.name;
                    }
                }

                $scope.model.loadingGroup = false;
            });
        };

        $scope.showDeleteGroupModal = function() {
            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-delete-group.html',
                show: true
            }, $modal, $scope);
        };

        $scope.deleteGroup = function() {
            $scope.model.loading = true;
            IdmService.deleteGroup($scope.model.selectedGroup.id).then(function() {
                $scope.model.loading = false;
                clearSelection();
                $scope.fetchGroups();
            });
        };

        $scope.showActivateGroupModal = function() {
            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-activate-group.html',
                show: true
            }, $modal, $scope);
        };

        $scope.activateGroup = function() {
            $scope.model.loading = true;
            IdmService.activateGroup($scope.model.selectedGroup.id).then(function() {
                $scope.model.loading = false;
                clearSelection();
                $scope.fetchGroups();
            });
        };

        // Clear any selected tenant
        var clearSelection = function() {
            delete $scope.model.groups;
            delete $scope.model.selectedGroup;
        };

        $scope.showEditGroupModal = function() {
            $scope.model.editedGroup  = $scope.model.selectedGroup;
            $scope.model.mode = 'edit';
            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-group-create.html',
                show: true
            }, $modal, $scope);
        };

        $scope.showRemoveMemberModal = function(user) {
            $scope.model.userToRemove = user;
            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-group-member-delete.html',
                show: true
            }, $modal, $scope);
        };

        $scope.deleteGroupMember = function() {
            IdmService.deleteGroupMember($scope.model.selectedGroup.id, $scope.model.userToRemove.id).then(function(data) {
                for (var i=0; i<$scope.model.users.data.length; i++) {
                    if ($scope.model.users.data[i].id === $scope.model.userToRemove.id) {
                        $scope.model.users.data.splice(i, 1);
                        delete $scope.model.userToRemove;
                        break;
                    }
                }
                fetchUserPage();
            });
        };

        $scope.addGroupMember = function(user) {
            IdmService.addGroupMember($scope.model.selectedGroup.id, user.id).then(function() {
                $scope.selectGroup($scope.model.selectedGroup.id, true);
            });
        };

        // Load the groups
        $scope.fetchGroups = function(groupIdToSelect) {
            $scope.model.loading = true;
            clearSelection();

            IdmService.getFunctionalGroups().then(function(data) {
                $scope.model.groups = data;

                var groupIndex;
                $scope.model.expanded = {};

                // Select a group
                if (groupIdToSelect) {
                    $scope.selectGroup(groupIdToSelect);
                }

                // By default, open first level of groups
                for (groupIndex = 0; groupIndex < data.length; groupIndex++) {
                    $scope.model.expanded[data[groupIndex].id] = true;
                }

                $scope.model.loading = false;
            });
        };

        if(validUser) {
            $scope.fetchGroups();
        }

    }]);

