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
angular.module('activitiModeler')
    .directive('loading', ['$translate', function ($translate) {
        return {
            restrict: 'E',
            template: '<div class=\'loading pull-right\' ng-show=\'status.loading\'><div class=\'l1\'></div><div class=\'l2\'></div><div class=\'l2\'></div></div>'
        };
    }]);

angular.module('activitiModeler')
    .directive('loadingLeftPull', ['$translate', function ($translate) {
        return {
            restrict: 'E',
            template: '<div class=\'loading pull-left\' ng-show=\'status.loading\'><div class=\'l1\'></div><div class=\'l2\'></div><div class=\'l2\'></div></div>'
        };
    }]);

/**
 * This is a template for the icon of a stencil item.
 */
angular.module('activitiModeler')
    .directive('stencilItemIcon', [function () {
        return {
            scope: {
                item: '=stencilItem'
            },
            restrict: 'E',
            template: '<img class="stencil-item-list-icon" ng-if=\"item.customIconId != null && item.customIconId != undefined\" ng-src=\"' + ACTIVITI.CONFIG.contextRoot + '/app/rest/image/{{item.customIconId}}\" width=\"16px\" height=\"16px\"/>' +
            '<img class="stencil-item-list-icon" ng-if=\"(item.customIconId == null || item.customIconId == undefined) && item.icon != null && item.icon != undefined\" ng-src=\"editor-app/stencilsets/bpmn2.0/icons/{{item.icon}}\" width=\"16px\" height=\"16px\"/>'
        };
    }]);

// Workaround for https://github.com/twbs/bootstrap/issues/8379 :
// prototype.js interferes with regular dropdown behavior
angular.module('activitiModeler')
    .directive('activitiFixDropdownBug', function () {
        return {
            restrict: 'AEC',
            link: function (scope, element, attrs) {
                if (!element.hasClass('btn-group')) {
                    // Fix applied to button, use parent instead
                    element = element.parent();
                }
                element.on('hidden.bs.dropdown	', function () {
                    element.show(); // evil prototype.js has added display:none to it ...
                })
            }
        };
    });


//form builder element renderer
angular.module('activitiModeler').directive('formBuilderElement', ['$rootScope', '$timeout', '$modal', '$http', '$templateCache', '$translate', 'RecursionHelper', 'FormBuilderService',
    function ($rootScope, $timeout, $modal, $http, $templateCache, $translate, RecursionHelper, FormBuilderService) {
    return {
        restrict: 'AE',
        templateUrl: 'views/templates/form-builder-element-template.html',
        transclude: false,
        scope: {
            formElement: '=formElement',
            editState: '=editState',
            formMode: '=formMode',
            drop: "&",
            moved: "&"
        },
        compile: function(element) {
            return RecursionHelper.compile(element, this.link);
        },
        link: function ($scope, $element, attributes) {

            $scope.formTabs = [
                {
                    "id": "general",
                    "name": $translate.instant('FORM-BUILDER.TABS.GENERAL')
                },
                {
                    "id": "options",
                    "name": $translate.instant('FORM-BUILDER.TABS.OPTIONS'),
                    "show": ['dropdown', 'radio-buttons']
                },
                {
                    "id": "upload",
                    "name": $translate.instant('FORM-BUILDER.TABS.UPLOAD-OPTIONS'),
                    "show": ['upload']
                }
            ];

            $scope.activeTab = $scope.formTabs[0];

            $scope.tabClicked = function (tab) {
                $scope.activeTab = tab;
            };

            var templateUrl = 'views/popover/formfield-edit-popover.html';


            $scope.removeFormElement = function (formElement) {
                if ($rootScope.formItems.indexOf(formElement) >= 0) {
                    $rootScope.formItems.splice($rootScope.formItems.indexOf(formElement), 1);
                }
            };

            $scope.pristine = true;
            $scope.newOption = {
                name: ''
            };

            $scope.insertFormField = {
                position: 0
            };

            $scope.openFieldPopover = function () {

                // Storing original values. In case the changes would trigger a layout change
                var originalFormElementType = $scope.formElement.type;
                var originalDisplayFieldType = undefined;
                if (originalFormElementType === 'readonly') {
                    if ($scope.formElement.params
                        && $scope.formElement.params.field
                        && $scope.formElement.params.field.type) {
                        originalDisplayFieldType = $scope.formElement.params.field.type;
                    }
                }

                // Create popover
                $scope.fieldEditPopup = _internalCreateModal({
                    template: 'views/popover/formfield-edit-popover.html?version=' + Date.now(),
                    scope: $scope,
                    backdrop: 'static',
                    keyboard: false
                }, $modal, $scope);

                // Check for layout changes
                var deregisterHideListener = $scope.$on('modal.hide', function() {
                    if ($scope.formElement.type === 'readonly') {

                        if ($scope.formElement.params && $scope.formElement.params.field && $scope.formElement.params.field.type
                            && $scope.formElement.params.field.type !== originalFormElementType) {

                            $scope.$emit('readonly-field-referenced-field-changed', {
                                formElement: $scope.formElement,
                                originalDisplayFieldType: originalDisplayFieldType
                            });

                        }
                    }

                    deregisterHideListener();
                });

            };

            $scope.formElementNameChanged = function (field) {
                if (!field.overrideId) {
                    var fieldId;
                    if (field.name && field.name.length > 0) {
                        fieldId = field.name.toLowerCase();
                        fieldId = fieldId.replace(new RegExp(' ', 'g'), '');
                        fieldId = fieldId.replace(/[&\/\\#,+~%.'":*?!<>{}()$@;]/g, '');
                    } else {
                        var index = 1;
                        if (field.layout) {
                            index = 1 + (2 * field.layout.row) + field.layout.column;
                        }
                        fieldId = 'field' + index;
                    }
                    field.id = fieldId;
                }
            };

            $scope.confirmNewOption = function ($event) {
                if ($scope.newOption.name) {
                    var options = $scope.formElement.options;
                    options.push($scope.newOption);

                    $scope.newOption = {name: ''};

                    if ($scope.formElement.type === 'radio-buttons' && options.length > 4) {
                        $scope.formElement.sizeY = 3;

                    } else if ($scope.formElement.type === 'radio-buttons' && options.length > 1) {
                        $scope.formElement.sizeY = 2;

                    } else {
                        $scope.formElement.sizeY = 1;
                    }

                    // if first additional option; first option is defaulted
                    if (options.length == 2) {
                        $scope.formElement.value = $scope.formElement.options[0].name;
                    }

                    if ($event) {
                        // Focus the input field again, to make adding more options possible immediatly
                        $($event.target).focus();
                    }
                }
            };

            $scope.optionKeyDown = function ($event) {
                if ($event.keyCode == 13) {
                    $scope.confirmNewOption($event);
                }
            };

            $scope.removeOption = function (index) {
                $scope.formElement.options.splice(index, 1);


                // if only 1 option left; reset default
                if ($scope.formElement.options == 1) {
                    $scope.formElement.value = '';
                } else {

                    // if removed element is the default option; first option is defaulted
                    var isPresent = false;
                    for (var i = 0; i < $scope.formElement.options.length; i++) {
                        if ($scope.formElement.options[i].name == $scope.formElement.value) {
                            isPresent = true;
                        }
                    }
                    if (!isPresent) {
                        $scope.formElement.value = $scope.formElement.options[0].name;
                    }
                }
            };

            $scope.doneEditing = function () {

                if ($scope.fieldEditPopup) {
                    $scope.fieldEditPopup.$scope.$hide();
                }
            };

            // Readonly field
            $scope.$watch('formElement.params.field', function (newValue, oldValue) {
                if (!$scope.pristine || (oldValue !== undefined && oldValue.id != newValue.id)) {
                    if (newValue && newValue.name) {
                        // Update the element's name
                        $scope.formElement.name = newValue.name;
                    }

                    $scope.formElement.sizeX = 1;
                    $scope.formElement.sizeY = 1;

                } else {
                    $scope.pristine = false;
                }

            });
        }
    };
}]);


angular.module('activitiModeler').directive('storeCursorPosition', ['$rootScope', '$timeout', '$popover', '$http', '$templateCache', function ($rootScope, $timeout, $popover, $http, $templateCache) {
    return {
        restrict: 'A',
        scope: {
            storeCursorPosition: '=storeCursorPosition'
        },
        link: function ($scope, $element, attributes) {
            $element.on('click change keypress', function () {
                if ($scope.storeCursorPosition !== undefined) {
                    $scope.storeCursorPosition = $element[0].selectionStart;
                }
            });
        }
    };
}]);

angular.module('activitiModeler').
    directive('selectGroupPopover', ['$http', '$popover', '$parse', 'GroupService', function ($http, $popover, $parse, GroupService) {
        var directive = {};
        directive.restrict = 'A';

        directive.scope = {
            excludeGroupIds: '=excludeGroupIds',
            selectGroupFormFields: '=selectGroupFormFields'
        };

        directive.link = function ($scope, $element, attrs) {
            // Set defaults
            var placement = "bottom";

            $element.addClass("group-selection");

            if (attrs.placement) {
                placement = attrs.placement;
            }

            $scope.popover = $popover($element, {template: 'views/popover/select-group-popover.html', placement: placement});

            // Parse callbacks
            var selectedCallback, cancelledCallback;
            if (attrs["onGroupSelected"]) {
                selectedCallback = $parse(attrs['onGroupSelected']);
            }
            if (attrs["onCancel"]) {
                cancelledCallback = $parse(attrs['onCancel']);
            }

            var popoverScope = $scope.popover.$scope;
            popoverScope.title = attrs['popoverTitle'];

            popoverScope.popupModel = {
                groupField: {},
                groupFieldFilter: ['functional-group']
            };

            if ($scope.selectGroupFormFields) {
                popoverScope.popupModel.formFields = $scope.selectGroupFormFields;
            }

            popoverScope.setSearchType = function () {
                popoverScope.popupModel.groupSourceType = 'search';
            };

            popoverScope.setFormFieldType = function () {
                popoverScope.popupModel.groupSourceType = 'field';
            };

            popoverScope.$watch('popupModel.groupField', function () {
                if (popoverScope.popupModel.groupField && popoverScope.popupModel.groupField.id) {
                    if (selectedCallback) {
                        // Run callback in parent scope of directive
                        var simpleGroupField = {
                            id: popoverScope.popupModel.groupField.id,
                            name: popoverScope.popupModel.groupField.name,
                            type: popoverScope.popupModel.groupField.type
                        };
                        selectedCallback($scope.$parent, {'groupField': simpleGroupField});
                        popoverScope.popupModel.groupField = {};
                    }

                    popoverScope.$hide();
                }
            });

            popoverScope.$watch('popupModel.filter', function () {
                $scope.updateFilter();
            });

            popoverScope.resetSelection = function () {
                if (popoverScope.popupModel.groupResults && popoverScope.popupModel.groupResults.length > 0) {
                    popoverScope.popupModel.selectedGroup = popoverScope.popupModel.groupResults[0];
                    popoverScope.popupModel.selectedIndex = 0;
                } else {
                    popoverScope.popupModel.selectedGroup = undefined;
                    popoverScope.popupModel.selectedIndex = -1;
                }
            };

            popoverScope.nextGroup = function () {
                var groups = popoverScope.popupModel.groupResults;
                if (groups && groups.length > 0 && popoverScope.popupModel.selectedIndex < groups.length - 1) {
                    popoverScope.popupModel.selectedIndex += 1;
                    popoverScope.popupModel.selectedGroup = groups[popoverScope.popupModel.selectedIndex];
                }
            };

            popoverScope.previousGroup = function () {
                var groups = popoverScope.popupModel.groupResults;
                if (groups && groups.length > 0 && popoverScope.popupModel.selectedIndex > 0) {
                    popoverScope.popupModel.selectedIndex -= 1;
                    popoverScope.popupModel.selectedGroup = groups[popoverScope.popupModel.selectedIndex];
                }
            };

            popoverScope.confirmGroup = function (group) {
                if (!group) {
                    // Selection is done with keyboard, use selection index
                    var groups = popoverScope.popupModel.groupResults;
                    if (popoverScope.popupModel.selectedIndex >= 0 && popoverScope.popupModel.selectedIndex < groups.length) {
                        group = groups[popoverScope.popupModel.selectedIndex];
                    }
                }

                if (group) {
                    if (selectedCallback) {
                        // Run callback in parent scope of directive
                        selectedCallback($scope.$parent, {'group': group});
                    }
                    popoverScope.$hide();
                }
            };

            $scope.updateFilter = function () {
                GroupService.getFilteredGroups(popoverScope.popupModel.filter).then(function (result) {

                    if ($scope.excludeGroupIds !== null && $scope.excludeGroupIds !== undefined) {
                        popoverScope.popupModel.groupResults = [];
                        for (var groupIndex = 0; groupIndex < result.data.length; groupIndex++) {
                            if (result.data[groupIndex].id && $scope.excludeGroupIds.indexOf(result.data[groupIndex].id) === -1) {
                                popoverScope.popupModel.groupResults.push(result.data[groupIndex]);
                            }
                        }
                    } else {
                        popoverScope.popupModel.groupResults = result.data;
                    }

                    popoverScope.resetSelection();
                })
            };

            popoverScope.$on('tooltip.show', function () {
                $scope.updateFilter();
            });

            popoverScope.$on('tooltip.hide', function () {
                popoverScope.popupModel.groupResults = [];
                popoverScope.popupModel.filter = '';


                if (popoverScope.popupModel.added) {
                    popoverScope.popupModel.added = false;
                } else {
                    if (cancelledCallback) {
                        // Run callback in parent scope of directive
                        cancelledCallback($scope.$parent);
                    }
                }
            });
        };
        return directive;
    }]);

angular.module('activitiModeler').
    directive('assignmentSelect', ['$http', '$templateCache', function ($http, $templateCache) {

        var directive = {};
        directive.restrict = 'A';
        directive.templateUrl = 'views/includes/select-assignment.html';
        directive.replace = true;
        directive.scope = {
            assignmentObject: "=assignmentSelect",
            assignmentFormFields: "=assignmentFormFields",
            title: "=title"
        };

        directive.controller = ['$rootScope', '$scope', '$translate', '$element', function ($rootScope, $scope, $translate, $element) {
            $scope.assignmentOptions = [
                {id: "initiator", title: $translate.instant('PROCESS-BUILDER.FIELD.ASSIGNMENT-SELECT.ASSIGNED-TO-INITIATOR')},
                {id: "user", title: $translate.instant('PROCESS-BUILDER.FIELD.ASSIGNMENT-SELECT.ASSIGNED-TO-SINGLE-USER')},
                {id: "users", title: $translate.instant('PROCESS-BUILDER.FIELD.ASSIGNMENT-SELECT.ASSIGNED-TO-CANDIDATE-USERS')}
            ];

            if ($rootScope.account.tenantId !== null && $rootScope.account.tenantId !== undefined) {
                $scope.assignmentOptions.push({id: "groups", title: $translate.instant('PROCESS-BUILDER.FIELD.ASSIGNMENT-SELECT.ASSIGNED-TO-CANDIDATE-GROUPS')});
            }

            $scope.assignmentOption = $scope.assignmentOptions[0];

            // Watch local value and update the actual object that is referenced by the directive
            $scope.$watch('assignmentOption', function (newValue) {
                if (newValue) {
                    $scope.assignmentObject.type = newValue.id;
                    if (newValue.id == 'initiator') {
                        $scope.assignmentObject.assignee = {};
                        $scope.assignmentObject.candidateUsers = [];
                        $scope.assignmentObject.candidateUserFields = [];
                        $scope.assignmentObject.candidateGroups = [];
                        $scope.assignmentObject.candidateGroupFields = [];

                    } else if (newValue.id == 'user') {
                        $scope.assignmentObject.candidateUsers = [];
                        $scope.assignmentObject.candidateUserFields = [];
                        $scope.assignmentObject.candidateGroups = [];
                        $scope.assignmentObject.candidateGroupFields = [];

                    } else if (newValue.id == 'users') {
                        // Create new empty array of users
                        if (!$scope.assignmentObject.candidateUsers) {
                            $scope.assignmentObject.candidateUsers = [];
                        }
                        if (!$scope.assignmentObject.candidateUserFields) {
                            $scope.assignmentObject.candidateUserFields = [];
                        }

                    } else if (newValue.id == 'groups') {
                        // Create new empty array of groups
                        if (!$scope.assignmentObject.candidateGroups) {
                            $scope.assignmentObject.candidateGroups = [];
                        }
                        if (!$scope.assignmentObject.candidateGroupFields) {
                            $scope.assignmentObject.candidateGroupFields = [];
                        }
                    }
                } else {
                    $scope.assignmentObject.type = $scope.assignmentOptions[0].id;
                }
            });

            $scope.setAssignee = function (user, userField) {
                if (user) {
                    $scope.assignmentObject.assignee = user;
                    $scope.assignmentObject.assigneeField = undefined;

                } else if (userField) {
                    $scope.assignmentObject.assigneeField = userField;
                    $scope.assignmentObject.assignee = undefined;
                }
            };

            $scope.addCandidateUser = function (user, userField) {
                // Only add it when it's not yet part of the current users
                var exists = false;
                if (user) {
                    for (var i = 0; i < $scope.assignmentObject.candidateUsers.length; i++) {
                        if ($scope.assignmentObject.candidateUsers[i].id === user.id) {
                            exists = true;
                            break;
                        }
                    }

                } else if (userField) {
                    for (var i = 0; i < $scope.assignmentObject.candidateUserFields.length; i++) {
                        if ($scope.assignmentObject.candidateUserFields[i].id === userField.id) {
                            exists = true;
                            break;
                        }
                    }
                }

                if (!exists && user) {
                    $scope.assignmentObject.candidateUsers.push(user);

                } else if (!exists && userField) {
                    $scope.assignmentObject.candidateUserFields.push(userField);
                }
            };

            $scope.addCandidateUserByEmail = function (email) {
                // Only add it when it's not yet part of the current users
                var exists = false;
                if ($scope.assignmentObject.candidateUsers) {
                    for (var i = 0; i < $scope.assignmentObject.candidateUsers.length; i++) {
                        if ($scope.assignmentObject.candidateUsers[i].email === email) {
                            exists = true;
                            break;
                        }
                    }
                }

                if (!exists) {
                    $scope.assignmentObject.candidateUsers.push({email: email});
                }
            };

            $scope.removeCandidateUser = function (user) {
                var users = $scope.assignmentObject.candidateUsers;
                var indexToRemove = -1;
                for (var i = 0; i < users.length; i++) {
                    if (user.id) {
                        if (user.id == users[i].id) {
                            indexToRemove = i;
                            break;
                        }
                    } else {
                        if (user.email == users[i].email) {
                            indexToRemove = i;
                            break;
                        }
                    }
                }
                if (indexToRemove >= 0) {
                    users.splice(indexToRemove, 1);
                }
            };

            $scope.removeCandidateUserField = function (userField) {
                var userFields = $scope.assignmentObject.candidateUserFields;
                var indexToRemove = -1;
                for (var i = 0; i < userFields.length; i++) {
                    if (userField.id) {
                        if (userField.id == userFields[i].id) {
                            indexToRemove = i;
                            break;
                        }
                    }
                }
                if (indexToRemove >= 0) {
                    userFields.splice(indexToRemove, 1);
                }
            };

            $scope.addCandidateGroup = function (group, groupField) {

                // Only add it when it's not yet part of the current users
                var exists = false;
                if (group) {
                    if ($scope.assignmentObject.candidateGroups) {
                        for (var i = 0; i < $scope.assignmentObject.candidateGroups.length; i++) {
                            if ($scope.assignmentObject.candidateGroups[i].id === group.id) {
                                exists = true;
                                break;
                            }
                        }
                    }
                }

                if (groupField) {
                    if ($scope.assignmentObject.candidateGroupFields) {
                        for (var i = 0; i < $scope.assignmentObject.candidateGroupFields.length; i++) {
                            if ($scope.assignmentObject.candidateGroupFields[i].id === groupField.id) {
                                exists = true;
                                break;
                            }
                        }
                    }
                }

                if (group && !exists) {
                    $scope.assignmentObject.candidateGroups.push(group);
                } else if (groupField && !exists) {
                    $scope.assignmentObject.candidateGroupFields.push(groupField);
                }

            };

            $scope.removeCandidateGroup = function (group) {
                var groups = $scope.assignmentObject.candidateGroups;
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

            $scope.removeCandidateGroupField = function (groupField) {
                var groups = $scope.assignmentObject.candidateGroupFields;
                var indexToRemove = -1;
                for (var i = 0; i < groups.length; i++) {
                    if (groupField.id == groups[i].id) {
                        indexToRemove = i;
                        break;
                    }
                }
                if (indexToRemove >= 0) {
                    groups.splice(indexToRemove, 1);
                }
            };

        }];

        directive.link = function ($scope, $element, attrs) {
            // Read the value from the scope and set all selections accordingly
            var assignmentOption;
            if ($scope.assignmentObject && $scope.assignmentObject.type) {
                for (var i = 0; i < $scope.assignmentOptions.length; i++) {
                    if ($scope.assignmentOptions[i].id == $scope.assignmentObject.type) {
                        assignmentOption = $scope.assignmentOptions[i];
                        break;
                    }
                }
            }

            // Revert to default assignment (initiator) if missing
            if (!assignmentOption) {
                assignmentOption = $scope.assignmentOptions[0];
            }
            $scope.assignmentOption = assignmentOption;
        };

        return directive;
    }]);

angular.module('activitiModeler').
    directive('fieldsSelect', ['$http', '$templateCache', function ($http, $templateCache) {

        var directive = {};
        directive.restrict = 'A';
        directive.templateUrl = 'views/includes/select-fields.html';
        directive.replace = true;
        directive.scope = {
            propertyObject: "=fieldsSelect",
            stepObject: "=fieldsSelectStep",
            title: "=title"
        };

        directive.controller = ['$rootScope', '$scope', '$element', function ($rootScope, $scope, $element) {

            if (!$scope.stepObject[$scope.propertyObject.id] || !$scope.stepObject[$scope.propertyObject.id].fields) {
                $scope.stepObject[$scope.propertyObject.id] = {fields: []};
            }

            $scope.model = {selectedField: undefined};

            $scope.addField = function () {
                if ($scope.model.selectedField) {
                    // Only add it when it's not yet part of the current users
                    var exists = false;
                    if ($scope.stepObject[$scope.propertyObject.id].fields) {
                        for (var i = 0; i < $scope.stepObject[$scope.propertyObject.id].fields.length; i++) {
                            if ($scope.stepObject[$scope.propertyObject.id].fields[i].id === $scope.model.selectedField.id) {
                                exists = true;
                                break;
                            }
                        }
                    }

                    if (!exists) {
                        $scope.stepObject[$scope.propertyObject.id].fields.push($scope.model.selectedField);
                    }
                }
            };

            $scope.removeField = function (field) {
                var fields = $scope.stepObject[$scope.propertyObject.id].fields;
                var indexToRemove = -1;
                for (var i = 0; i < fields.length; i++) {
                    if (field.id == fields[i].id) {
                        indexToRemove = i;
                        break;
                    }
                }
                if (indexToRemove >= 0) {
                    fields.splice(indexToRemove, 1);
                }
            };
        }];

        directive.link = function ($scope, $element, attrs) {
        };

        return directive;
    }]);

angular.module('activitiModeler').
    directive('recipientSelect', ['$http', '$translate', '$templateCache', function ($http, $translate, $templateCache) {

        var directive = {};
        directive.restrict = 'A';
        directive.templateUrl = 'views/includes/select-recipient.html';
        directive.replace = true;
        directive.scope = {
            recipientObject: "=recipientSelect",
            recipientFormFields: "=recipientFormFields",
            title: "=title"
        };

        directive.controller = ['$rootScope', '$scope', '$element', function ($rootScope, $scope, $element) {
            $scope.recipientOptions = [
                {id: "initiator", title: $translate.instant('PROCESS-BUILDER.EMAIL-STEP.RECIPIENT.POPOVER.SELECT-INITIATOR')},
                {id: "user", title: $translate.instant('PROCESS-BUILDER.EMAIL-STEP.RECIPIENT.POPOVER.SELECT-USER')},
                {id: "users", title: $translate.instant('PROCESS-BUILDER.EMAIL-STEP.RECIPIENT.POPOVER.SELECT-USERS')}
            ];

            $scope.recipientOption = $scope.recipientOptions[0];

            // Watch local value and update the actual object that is referenced by the directive
            $scope.$watch('recipientOption', function (newValue) {
                if (newValue) {
                    $scope.recipientObject.type = newValue.id;
                    if (newValue.id == 'initiator') {
                        $scope.recipientObject.user = null;
                        $scope.recipientObject.users = null;
                        $scope.recipientObject.userFields = null;

                    }  else if (newValue.id == 'user') {
                        $scope.recipientObject.users = null;
                        $scope.recipientObject.userFields = null;

                    }else if (newValue.id == 'users') {
                    	$scope.recipientObject.user = null;
                    	
                        // Create new empty array of users
                        if (!$scope.recipientObject.users) {
                            $scope.recipientObject.users = [];
                        }
                        if (!$scope.recipientObject.userFields) {
                            $scope.recipientObject.userFields = [];
                        }
                    }

                } else {
                    $scope.assignmentObject.type = $scope.assignmentOptions[0].id;
                }
            });

            $scope.setRecipient = function (user, userField) {
                if (user) {
                    $scope.recipientObject.user = user;
                    $scope.recipientObject.userField = undefined;
                    $scope.recipientObject.userFields = undefined;
                } else if (userField) {
                    $scope.recipientObject.userField = userField;
                    $scope.recipientObject.user = undefined;
                    $scope.recipientObject.userFields = undefined;
                }
            };

            $scope.addRecipient = function (user, userField) {
                // Only add it when it's not yet part of the current users
                var exists = false;

                if (user) {
                    if ($scope.recipientObject.users) {
                        for (var i = 0; i < $scope.recipientObject.users.length; i++) {
                            if ($scope.recipientObject.users[i].id === user.id) {
                                exists = true;
                                break;
                            }
                        }
                    }
                }else if (userField){
                    if ($scope.recipientObject.userFields) {
                        for (var i = 0; i < $scope.recipientObject.userFields.length; i++) {
                            if ($scope.recipientObject.userFields[i].id === userField.id) {
                                exists = true;
                                break;
                            }
                        }
                    }
                }

                if (!exists) {
                    if (user) {
                        $scope.recipientObject.users.push(user);
                    } else if (userField) {
                        $scope.recipientObject.userFields.push(userField);
                    }
                }
            };

            $scope.addUserByEmail = function (email) {
                // Only add it when it's not yet part of the current users
                var exists = false;
                if ($scope.recipientObject.users) {
                    for (var i = 0; i < $scope.recipientObject.users.length; i++) {
                        if ($scope.recipientObject.users[i].email === email) {
                            exists = true;
                            break;
                        }
                    }
                }

                if (!exists) {
                    $scope.recipientObject.users.push({email: email});
                }
            };

            $scope.removeUser = function (user) {
                var users = $scope.recipientObject.users;
                var indexToRemove = -1;
                for (var i = 0; i < users.length; i++) {
                    if (user.id) {
                        if (user.id == users[i].id) {
                            indexToRemove = i;
                            break;
                        }
                    } else {
                        if (user.email == users[i].email) {
                            indexToRemove = i;
                            break;
                        }
                    }
                }
                if (indexToRemove >= 0) {
                    users.splice(indexToRemove, 1);
                }
            };

            $scope.removeUserField = function (userField) {
                var userFields = $scope.recipientObject.userFields;
                var indexToRemove = -1;
                for (var i = 0; i < userFields.length; i++) {
                    if (userField.id) {
                        if (userField.id == userFields[i].id) {
                            indexToRemove = i;
                            break;
                        }
                    }
                }
                if (indexToRemove >= 0) {
                    userFields.splice(indexToRemove, 1);
                }
            };

        }];

        directive.link = function ($scope, $element, attrs) {
            // Read the value from the scope and set all selections accordingly
            var recipientOption;
            if ($scope.recipientObject && $scope.recipientObject.type) {
                for (var i = 0; i < $scope.recipientOptions.length; i++) {
                    if ($scope.recipientOptions[i].id == $scope.recipientObject.type) {
                        recipientOption = $scope.recipientOptions[i];
                        break;
                    }
                }
            }

            // Revert to default assignment (initiator) if missing
            if (!recipientOption) {
                recipientOption = $scope.recipientOptions[0];
            }
            $scope.recipientOption = recipientOption;
        };

        return directive;
    }]);

angular.module('activitiModeler').
    directive('editorInputCheck', function () {

        return {
            require: 'ngModel',
            link: function (scope, element, attrs, modelCtrl) {

                modelCtrl.$parsers.push(function (inputValue) {

                    var transformedInput = inputValue.replace(/[&\/\\#,+~%.'":*?<>{}()$@;]/g, '');

                    if (transformedInput != inputValue) {
                        modelCtrl.$setViewValue(transformedInput);
                        modelCtrl.$render();
                    }

                    return transformedInput;
                });
            }
        };
    });
