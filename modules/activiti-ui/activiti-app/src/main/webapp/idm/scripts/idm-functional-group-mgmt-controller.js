/*
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
activitiApp.controller('IdmFunctionalGroupMgmtController', ['$rootScope', '$scope', '$translate', '$http', '$timeout','$location', '$modal', '$popover', 'IdmService',
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
            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-group-create.html',
                show: true
            }, $modal, $scope);
        };

        /* Returns a mapping of groupId -> parent group object */
        $scope.groupListToParentMap = function(groups) {

            var _groupListToParentMap = function(groups, currentParent, mapping) {
                if (groups && groups.length > 0) {
                    for (var i = 0; i < groups.length; i++) {
                        if (currentParent) {
                            mapping[groups[i].id] = currentParent;
                        }
                        _groupListToParentMap(groups[i].groups, groups[i], mapping);
                    }
                }
            };

            var mapping = {};
            _groupListToParentMap(groups, undefined, mapping);
            return mapping;
        };

        var fetchUserPage = function() {
            $scope.model.loadingUsers = true;
            IdmService.getUsersForGroup($scope.model.selectedGroup.id, $scope.model.userFilter ,$scope.model.userPage, $scope.model.pageSize).then(function(data) {
                $scope.model.users = data;
                $scope.model.moreUsers = $scope.model.selectedGroup.userCount > (($scope.model.userPage+1 ) * $scope.model.pageSize);
                $scope.model.loadingUsers = false;
            });
        };

        $scope.selectGroup = function(groupId, force) {

            // Only refetch if it is a different selection
            var isNewSelection = true;
            if (force === null || force === undefined || force === false) {
                if (groupId && $scope.model.selectedGroup && groupId === $scope.model.selectedGroup.id) {
                    isNewSelection = false;
                }
            }

            // Switch expanded/collapsed state for the selected group (and potentially children)
            var switchExpandedState = function() {
                var currentState = $scope.model.expanded[$scope.model.selectedGroup.id];
                if (currentState === null || currentState === undefined) {
                    $scope.model.expanded[$scope.model.selectedGroup.id] = true;
                } else {
                    $scope.model.expanded[$scope.model.selectedGroup.id] = !currentState;
                }
            };

            if (isNewSelection) {
                $scope.model.loadingGroup = true;
                IdmService.getGroup(groupId).then(function (data) {
                    $scope.model.selectedGroup = data;

                    $scope.model.expanded[data.id] = true; // always expand the new selection

                    // All parents need to be expanded too
                    var parentMapping = $scope.groupListToParentMap($scope.model.groups);
                    var currentParent = parentMapping[data.id];
                    while (currentParent) {
                        $scope.model.expanded[currentParent.id] = true;
                        currentParent = parentMapping[currentParent.id];
                    }

                    $scope.model.userPage = 0;
                    $scope.model.pageSize = 50;
                    fetchUserPage();

                    $scope.model.loadingGroup = false;
                });
            } else {
                switchExpandedState();
            }

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

        $scope.saveGroup = function() {
            if (!$scope.model.editedGroup.id) {

                // Create
                $scope.model.loading = true;
                IdmService.createGroup($scope.model.editedGroup,  $rootScope.common.selectedTenantId, 1).then(function (data) {   // 1 == functional group
                    $scope.fetchGroups(data.id);
                    $scope.model.loading = false;
                });

            } else {

                // Update
                $scope.model.loadingGroup = true;
                IdmService.updateGroup($scope.model.editedGroup.id, $rootScope.common.selectedTenantId, $scope.model.editedGroup).then(function (data) {
                    $scope.model.selectedGroup = data;

                    // Find the entry in the list on the left, and update its name
                    for (var i=0; i<$scope.model.groups.length; i++){
                        if ($scope.model.groups[i].id === data.id) {
                            $scope.model.groups[i].name = data.name;
                        }
                    }

                    $scope.model.loadingGroup = false;
                });

            }
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

        $scope.showCreateSubgroupPopup = function() {
            $scope.model.editedGroup  = { parentGroupId: $scope.model.selectedGroup.id };
            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-group-create.html',
                show: true
            }, $modal, $scope);
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


        // Has to be done at the end: the default tenant needs to be set (see if-else) just above
        if(validUser) {
            $scope.fetchGroups();
        }

    }]);

