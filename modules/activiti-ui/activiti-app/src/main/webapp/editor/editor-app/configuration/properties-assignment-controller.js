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

/*
 * Assignment
 */
'use strict';

angular.module('activitiModeler').controller('KisBpmAssignmentCtrl', [ '$scope', '$modal', function($scope, $modal) {

    // Config for the modal window
    var opts = {
        template:  'editor-app/configuration/properties/assignment-popup.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    _internalCreateModal(opts, $modal, $scope);
}]);

angular.module('activitiModeler').controller('KisBpmAssignmentPopupCtrl',
    [ '$rootScope', '$scope', '$translate', '$http', 'UserService', 'GroupService', function($rootScope, $scope, $translate, $http, UserService, GroupService) {

    // Put json representing assignment on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.assignment !== undefined
        && $scope.property.value.assignment !== null) {

        $scope.assignment = $scope.property.value.assignment;

    } else {
        $scope.assignment = {type:'idm'};
    }

    $scope.popup = {
        assignmentObject: {
            type:$scope.assignment.type,
            idm: {
                type:undefined,
                assignee: undefined,
                candidateUsers: [],
                candidateGroups: []
            },
            static: {
                assignee: undefined,
                candidateUsers: [],
                candidateGroups: []
            }
        }
    };


    $scope.assignmentOptions = [
        {id: "initiator", title: $translate.instant('PROPERTY.ASSIGNMENT.IDM.DROPDOWN.INITIATOR')},
        {id: "user", title: $translate.instant('PROPERTY.ASSIGNMENT.IDM.DROPDOWN.USER')},
        {id: "users", title: $translate.instant('PROPERTY.ASSIGNMENT.IDM.DROPDOWN.USERS')},
        {id: "groups", title: $translate.instant('PROPERTY.ASSIGNMENT.IDM.DROPDOWN.GROUPS')}
    ];

    if ($scope.assignment.idm && $scope.assignment.idm.type) {
        for (var i = 0; i < $scope.assignmentOptions.length; i++) {
            if ($scope.assignmentOptions[i].id == $scope.assignment.idm.type) {
                $scope.assignmentOption = $scope.assignmentOptions[i];
                break;
            }
        }
    }

    // fill the IDM area
    if (!$scope.assignmentOption) {
        // Default, first time opening the popup
        $scope.assignmentOption = $scope.assignmentOptions[0];
    } else {
        // Values already filled
        if ($scope.assignment.idm) { //fill the IDM tab
            if ($scope.assignment.idm.assignee) {
                if ($scope.assignment.idm.assignee.id) {
                    $scope.popup.assignmentObject.idm.assignee = $scope.assignment.idm.assignee;
                } else {
                    $scope.popup.assignmentObject.idm.assignee = {email: $scope.assignment.idm.assignee.email};
                }
            }

            if ($scope.assignment.idm.candidateUsers && $scope.assignment.idm.candidateUsers.length > 0) {
                for (var i = 0; i < $scope.assignment.idm.candidateUsers.length; i++) {
                    $scope.popup.assignmentObject.idm.candidateUsers.push($scope.assignment.idm.candidateUsers[i]);
                }
            }

            if ($scope.assignment.idm.candidateGroups && $scope.assignment.idm.candidateGroups.length > 0) {
                for (var i = 0; i < $scope.assignment.idm.candidateGroups.length; i++) {
                    $scope.popup.assignmentObject.idm.candidateGroups.push($scope.assignment.idm.candidateGroups[i]);
                }
            }
        }
    }
    
    //fill the static area
    if ($scope.assignment.assignee) {
        $scope.popup.assignmentObject.static.assignee = $scope.assignment.assignee;
    }

    if ($scope.assignment.candidateUsers && $scope.assignment.candidateUsers.length > 0) {
        for (var i = 0; i < $scope.assignment.candidateUsers.length; i++) {
            $scope.popup.assignmentObject.static.candidateUsers.push($scope.assignment.candidateUsers[i]);
        }
    }

    if ($scope.assignment.candidateGroups && $scope.assignment.candidateGroups.length > 0) {
        for (var i = 0; i < $scope.assignment.candidateGroups.length; i++) {
            $scope.popup.assignmentObject.static.candidateGroups.push($scope.assignment.candidateGroups[i]);
        }
    }

    initStaticContextForEditing($scope);

    $scope.$watch('popup.groupFilter', function () {
        $scope.updateGroupFilter();
    });

    $scope.$watch('popup.filter', function() {
        $scope.updateFilter();
    });

    $scope.updateFilter = function() {
        if ($scope.popup.oldFilter == undefined || $scope.popup.oldFilter != $scope.popup.filter) {
            if (!$scope.popup.filter) {
                $scope.popup.oldFilter = '';
            } else {
                $scope.popup.oldFilter = $scope.popup.filter;
            }

            if ($scope.popup.filter !== null && $scope.popup.filter !== undefined) {
                UserService.getFilteredUsers($scope.popup.filter).then(function (result) {
                    var filteredUsers = [];
                    for (var i=0; i<result.data.length; i++) {
                        var filteredUser = result.data[i];

                        var foundCandidateUser = false;
                        if ($scope.popup.assignmentObject.idm.candidateUsers !== null && $scope.popup.assignmentObject.idm.candidateUsers !== undefined) {
                            for (var j=0; j<$scope.popup.assignmentObject.idm.candidateUsers.length; j++) {
                                var candidateUser = $scope.popup.assignmentObject.idm.candidateUsers[j];
                                if (candidateUser.id === filteredUser.id) {
                                    foundCandidateUser = true;
                                    break;
                                }
                            }
                        }

                        if (!foundCandidateUser) {
                            filteredUsers.push(filteredUser);
                        }

                    }

                    $scope.popup.userResults = filteredUsers;
                    $scope.resetSelection();
                });
            }
        }
    };

    $scope.updateGroupFilter = function() {
        if ($scope.popup.oldGroupFilter == undefined || $scope.popup.oldGroupFilter != $scope.popup.groupFilter) {
            if (!$scope.popup.groupFilter) {
                $scope.popup.oldGroupFilter = '';
            } else {
                $scope.popup.oldGroupFilter = $scope.popup.groupFilter;
            }

            GroupService.getFilteredGroups($scope.popup.groupFilter).then(function(result) {
                $scope.popup.groupResults = result.data;
                $scope.resetGroupSelection();
            });
        }
    };

    $scope.confirmUser = function(user) {
        if (!user) {
            // Selection is done with keyboard, use selection index
            var users = $scope.popup.userResults;
            if ($scope.popup.selectedIndex >= 0 && $scope.popup.selectedIndex < users.length) {
                user = users[$scope.popup.selectedIndex];
            }
        }

        if (user) {
            if ("user" == $scope.assignmentOption.id) {
                $scope.popup.assignmentObject.idm.assignee = user;
            } else if ("users" == $scope.assignmentOption.id) {

                // Only add if not yet part of candidate users
                var found = false;
                if ($scope.popup.assignmentObject.idm.candidateUsers) {
                    for (var i = 0; i < $scope.popup.assignmentObject.idm.candidateUsers.length; i++) {
                        if ($scope.popup.assignmentObject.idm.candidateUsers[i].id === user.id) {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    $scope.addCandidateUser(user);
                }
            }
        }
    };

    $scope.confirmEmail = function() {
        if ("user" == $scope.assignmentOption.id) {
            $scope.popup.assignmentObject.idm.assignee = {email: $scope.popup.email};
        } else if ("users" == $scope.assignmentOption.id) {

            // Only add if not yet part of candidate users
            var found = false;
            if ($scope.popup.assignmentObject.idm.candidateUsers) {
                for (var i = 0; i < $scope.popup.assignmentObject.idm.candidateUsers.length; i++) {

                    if ($scope.popup.assignmentObject.idm.candidateUsers[i].id) {
                        if ($scope.popup.assignmentObject.idm.candidateUsers[i].id === user.id) {
                            found = true;
                            break;
                        }
                    } else if ($scope.popup.assignmentObject.idm.candidateUsers[i].email) {
                        if ($scope.popup.assignmentObject.idm.candidateUsers[i].email === $scope.popup.email) {
                            found = true;
                            break;
                        }
                    }
                }
            }

            if (!found) {
                $scope.addCandidateUser({email: $scope.popup.email});
            }
        }
    };

    $scope.confirmGroup = function(group) {
        if (!group) {
            // Selection is done with keyboard, use selection index
            var groups = $scope.popup.groupResults;
            if ($scope.popup.selectedGroupIndex >= 0 && $scope.popup.selectedGroupIndex < groups.length) {
                group = groups[$scope.popup.selectedGroupIndex];
            }
        }

        if (group) {
            // Only add if not yet part of candidate groups
            var found = false;
            if ($scope.popup.assignmentObject.idm.candidateGroups) {
                for (var i = 0; i < $scope.popup.assignmentObject.idm.candidateGroups.length; i++) {
                    if ($scope.popup.assignmentObject.idm.candidateGroups[i].id === group.id) {
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                $scope.addCandidateGroup(group);
            }
        }
    };

    $scope.addCandidateUser = function(user) {
        $scope.popup.assignmentObject.idm.candidateUsers.push(user);
    };
    
    $scope.removeCandidateUser = function(user) {
        var users = $scope.popup.assignmentObject.idm.candidateUsers;
        var indexToRemove = -1;
        for (var i = 0; i < users.length; i++) {
            if (user.id) {
                if (user.id === users[i].id) {
                    indexToRemove = i;
                    break;
                }
            } else {
                if (user.email === users[i].email) {
                    indexToRemove = i;
                    break;
                }
            }
        }
        if (indexToRemove >= 0) {
            users.splice(indexToRemove, 1);
        }
    };
    
    $scope.addCandidateGroup = function(group) {
        $scope.popup.assignmentObject.idm.candidateGroups.push(group);
    };
    
    $scope.removeCandidateGroup = function(group) {
        var groups = $scope.popup.assignmentObject.idm.candidateGroups;
        var indexToRemove = -1;
        for (var i = 0; i < groups.length; i++) {
            if (group.id == groups[i].id) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove >= 0) {
            groups.splice(indexToRemove, 1);
        }
    };

    $scope.resetSelection = function() {
        if ($scope.popup.userResults && $scope.popup.userResults.length > 0) {
            $scope.popup.selectedIndex = 0;
        } else {
            $scope.popup.selectedIndex = -1;
        }
    };

    $scope.nextUser = function() {
        var users = $scope.popup.userResults;
        if (users && users.length > 0 && $scope.popup.selectedIndex < users.length -1) {
            $scope.popup.selectedIndex += 1;
        }
    };

    $scope.previousUser = function() {
        var users = $scope.popup.userResults;
        if (users && users.length > 0 && $scope.popup.selectedIndex > 0) {
            $scope.popup.selectedIndex -= 1;
        }
    };

    $scope.resetGroupSelection = function() {
        if ($scope.popup.groupResults && $scope.popup.groupResults.length > 0) {
            $scope.popup.selectedGroupIndex = 0;
        } else {
            $scope.popup.selectedGroupIndex = -1;
        }
    };

    $scope.nextGroup = function() {
        var groups = $scope.popup.groupResults;
        if (groups && groups.length > 0 && $scope.popup.selectedGroupIndex < groups.length -1) {
            $scope.popup.selectedGroupIndex += 1;
        }
    };

    $scope.previousGroup = function() {
        var groups = $scope.popup.groupResults;
        if (groups && groups.length > 0 && $scope.popup.selectedGroupIndex > 0) {
            $scope.popup.selectedGroupIndex -= 1;
        }
    };

    $scope.removeAssignee = function() {
        $scope.popup.assignmentObject.idm.assignee = undefined;
    };
    
    // Click handler for + button after enum value
    $scope.addCandidateUserValue = function(index) {
        $scope.popup.assignmentObject.static.candidateUsers.splice(index + 1, 0, {value: ''});
    };

    // Click handler for - button after enum value
    $scope.removeCandidateUserValue = function(index) {
        $scope.popup.assignmentObject.static.candidateUsers.splice(index, 1);
    };

    // Click handler for + button after enum value
    $scope.addCandidateGroupValue = function(index) {
        $scope.popup.assignmentObject.static.candidateGroups.splice(index + 1, 0, {value: ''});
    };

    // Click handler for - button after enum value
    $scope.removeCandidateGroupValue = function(index) {
        $scope.popup.assignmentObject.static.candidateGroups.splice(index, 1);
    };
    
    $scope.setSearchType = function() {
        $scope.popup.assignmentObject.assignmentSourceType = 'search';
    };

    $scope.allSteps = EDITOR.UTIL.collectSortedElementsFromPrecedingElements($scope.selectedShape);

    $scope.save = function () {

        handleAssignmentInput($scope.popup.assignmentObject.static);

        $scope.assignment.type = $scope.popup.assignmentObject.type;

        if ('idm' === $scope.popup.assignmentObject.type) { // IDM
            $scope.popup.assignmentObject.static = undefined;

            //Construct an IDM object to be saved to the process model.
            var idm = {type: $scope.assignmentOption.id};
            if ('user' == idm.type) {
                if ($scope.popup.assignmentObject.idm.assignee) {
                    idm.assignee = $scope.popup.assignmentObject.idm.assignee;
                }
            } else if ('users' == idm.type) {
                if ($scope.popup.assignmentObject.idm.candidateUsers && $scope.popup.assignmentObject.idm.candidateUsers.length > 0) {
                    idm.candidateUsers = $scope.popup.assignmentObject.idm.candidateUsers;
                }
            } else if ('groups' == idm.type) {
                if ($scope.popup.assignmentObject.idm.candidateGroups && $scope.popup.assignmentObject.idm.candidateGroups.length > 0) {
                    idm.candidateGroups = $scope.popup.assignmentObject.idm.candidateGroups;
                }
            }
            $scope.assignment.idm = idm;
            $scope.assignment.assignee = undefined;
            $scope.assignment.candidateUsers = undefined;
            $scope.assignment.candidateGroups = undefined;

        }

        if ('static' === $scope.popup.assignmentObject.type) { // IDM
            $scope.popup.assignmentObject.idm = undefined;
            $scope.assignment.idm = undefined;
            $scope.assignment.assignee = $scope.popup.assignmentObject.static.assignee;
            $scope.assignment.candidateUsers = $scope.popup.assignmentObject.static.candidateUsers;
            $scope.assignment.candidateGroups = $scope.popup.assignmentObject.static.candidateGroups;
        }

        $scope.property.value = {};
        $scope.property.value.assignment = $scope.assignment;
        $scope.updatePropertyInModel($scope.property);
        $scope.close();
    };

    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
    	$scope.$hide();
    };

    var handleAssignmentInput = function ($assignment) {
    
        function isEmptyString(value) {
          return (value === undefined || value === null || value.trim().length === 0);
        }
    
        if (isEmptyString($assignment.assignee)){
          $assignment.assignee = undefined;
        }
        var toRemoveIndexes;
        var removedItems=0;
        var i = 0;
        if ($assignment.candidateUsers) {
          toRemoveIndexes = [];
          for (i = 0; i < $assignment.candidateUsers.length; i++) {
            if (isEmptyString($assignment.candidateUsers[i].value)) {
              toRemoveIndexes[toRemoveIndexes.length] = i;
            }
          }
    
          if (toRemoveIndexes.length == $assignment.candidateUsers.length) {
            $assignment.candidateUsers = undefined;
          } else {
             removedItems=0;
            for (i = 0; i < toRemoveIndexes.length; i++) {
              $assignment.candidateUsers.splice(toRemoveIndexes[i]-removedItems, 1);
              removedItems++;
            }
          }
        }
    
        if ($assignment.candidateGroups) {
          toRemoveIndexes = [];
          for (i = 0; i < $assignment.candidateGroups.length; i++) {
            if (isEmptyString($assignment.candidateGroups[i].value)) {
              toRemoveIndexes[toRemoveIndexes.length] = i;
            }
          }
    
          if (toRemoveIndexes.length == $assignment.candidateGroups.length) {
            $assignment.candidateGroups = undefined;
          } else {
             removedItems=0;
            for (i = 0; i < toRemoveIndexes.length; i++) {
              $assignment.candidateGroups.splice(toRemoveIndexes[i]-removedItems, 1);
              removedItems++;
            }
          }
        }
    };
    
    function initStaticContextForEditing($scope) {
        if (!$scope.popup.assignmentObject.static.candidateUsers || $scope.popup.assignmentObject.static.candidateUsers.length==0) {
          $scope.popup.assignmentObject.static.candidateUsers = [{value: ''}];
        }
        if (!$scope.popup.assignmentObject.static.candidateGroups || $scope.popup.assignmentObject.static.candidateGroups.length==0) {
          $scope.popup.assignmentObject.static.candidateGroups = [{value: ''}];
        }
    }
}]);
