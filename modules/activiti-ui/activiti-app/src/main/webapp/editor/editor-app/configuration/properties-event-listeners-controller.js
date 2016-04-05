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
 * Execution listeners
 */

angular.module('activitiModeler').controller('KisBpmEventListenersCtrl',
    ['$scope', '$modal', '$timeout', '$translate', function ($scope, $modal, $timeout, $translate) {

        // Config for the modal window
        var opts = {
            template: 'editor-app/configuration/properties/event-listeners-popup.html?version=' + Date.now(),
            scope: $scope
        };

        // Open the dialog
        _internalCreateModal(opts, $modal, $scope);
    }]);

//Need a separate controller for the modal window due to https://github.com/angular-ui/bootstrap/issues/259
// Will be fixed in a newer version of Angular UI
angular.module('activitiModeler').controller('KisBpmEventListenersPopupCtrl',
    ['$scope', '$q', '$translate', '$timeout', function ($scope, $q, $translate, $timeout) {

        // Put json representing form properties on scope
        if ($scope.property.value !== undefined && $scope.property.value !== null
            && $scope.property.value.eventListeners !== undefined
            && $scope.property.value.eventListeners !== null) {

            if ($scope.property.value.eventListeners.constructor == String) {
                $scope.eventListeners = JSON.parse($scope.property.value.eventListeners);
            }
            else {
                // Note that we clone the json object rather then setting it directly,
                // this to cope with the fact that the user can click the cancel button and no changes should have happened
                $scope.eventListeners = angular.copy($scope.property.value.eventListeners);
            }

        } else {
            $scope.eventListeners = [];
        }

        $scope.translationsRetrieved = false;
        $scope.labels = {};

        var eventPromise = $translate('PROPERTY.EXECUTIONLISTENERS.EVENT');
        var implementationPromise = $translate('PROPERTY.EXECUTIONLISTENERS.FIELDS.IMPLEMENTATION');
        var namePromise = $translate('PROPERTY.EXECUTIONLISTENERS.FIELDS.NAME');

        $q.all([eventPromise, implementationPromise, namePromise]).then(function (results) {
            $scope.labels.eventLabel = results[0];
            $scope.labels.implementationLabel = results[1];
            $scope.labels.nameLabel = results[2];
            $scope.translationsRetrieved = true;

            // Config for grid
            $scope.gridOptions = {
                data: $scope.eventListeners,
                headerRowHeight: 28,
                enableRowSelection: true,
                enableRowHeaderSelection: false,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                enableHorizontalScrollbar: 0,
                enableColumnMenus: false,
                enableSorting: false,
                columnDefs: [{field: 'event', displayName: $scope.labels.eventLabel},
                    {field: 'implementation', displayName: $scope.labels.implementationLabel}]
            };

            $scope.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.gridApi = gridApi;
                gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                    $scope.selectedListener = row.entity;

                    if ($scope.selectedListener) {
                        var fields = $scope.selectedListener.fields;
                        if (fields !== undefined && fields !== null) {
                            for (var i = 0; i < fields.length; i++) {
                                var field = fields[i];
                                if (field.stringValue !== undefined && field.stringValue !== '') {
                                    field.implementation = field.stringValue;
                                } else if (field.expression !== undefined && field.expression !== '') {
                                    field.implementation = field.expression;
                                } else if (field.string !== undefined && field.string !== '') {
                                    field.implementation = field.string;
                                }
                            }
                        } else {
                            $scope.selectedListener.fields = [];
                        }
                        if (!$scope.selectedListener.events) {
                            $scope.selectedListener.events = [{event: ''}];
                        }
                    }
                });
            };
        });


        // Click handler for + button after enum value
        $scope.addEventValue = function (index) {
            $scope.selectedListener.events.splice(index + 1, 0, {event: ''});
        };

        // Click handler for - button after enum value
        $scope.removeEventValue = function (index) {
            $scope.selectedListener.events.splice(index, 1);
            $scope.listenerDetailsChanged();
        };

        $scope.listenerDetailsChanged = function () {
            var listener = $scope.selectedListener;
            if (listener.events) {
                var eventText = '';
                for (var i = 0; i < listener.events.length; i++) {
                    if (i > 0) {
                        eventText += ", ";
                    }
                    eventText += listener.events[i].event;
                }
                $scope.selectedListener.event = eventText;
            }

            if (listener.rethrowEvent) {
                var implementationText = '';
                if (listener.rethrowType && listener.rethrowType.length > 0) {
                    if (listener.rethrowType === 'error' && listener.errorcode !== '') {
                        implementationText = "Rethrow as error " + listener.errorcode;
                    }
                    else if (listener.rethrowType === 'message' && listener.messagename !== '') {
                        implementationText = "Rethrow as message " + listener.messagename;
                    }
                    else if ((listener.rethrowType === 'signal' || listener.rethrowType === 'globalSignal') && listener.signalname !== '') {
                        implementationText = "Rethrow as signal " + listener.signalname;
                    }
                }
                $scope.selectedListener.implementation = implementationText;
            }
            else {
                if ($scope.selectedListener.className !== '') {
                    $scope.selectedListener.implementation = $scope.selectedListener.className;
                }
                else if ($scope.selectedListener.delegateExpression !== '') {
                    $scope.selectedListener.implementation = $scope.selectedListener.delegateExpression;
                }
                else {
                    $scope.selectedListener.implementation = '';
                }
            }
        };

        // Click handler for add button
        $scope.addNewListener = function () {
            var newListener = {
                event: '',
                implementation: '',
                className: '',
                delegateExpression: '',
                retrowEvent: false
            };

            $scope.eventListeners.push(newListener);

            $timeout(function () {
                $scope.gridApi.selection.toggleRowSelection(newListener);
            });
        };

        // Click handler for remove button
        $scope.removeListener = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.eventListeners.indexOf(selectedItems[0]);
                $scope.gridApi.selection.toggleRowSelection(selectedItems[0]);
                $scope.eventListeners.splice(index, 1);

                if ($scope.eventListeners.length == 0) {
                    $scope.selectedListener = undefined;
                }

                $timeout(function () {
                    if ($scope.eventListeners.length > 0) {
                        $scope.gridApi.selection.toggleRowSelection($scope.eventListeners[0]);
                    }
                });
            }
        };

        // Click handler for up button
        $scope.moveListenerUp = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.eventListeners.indexOf(selectedItems[0]);
                if (index != 0) { // If it's the first, no moving up of course
                    var temp = $scope.eventListeners[index];
                    $scope.eventListeners.splice(index, 1);
                    $timeout(function () {
                        $scope.eventListeners.splice(index + -1, 0, temp);
                        $timeout(function () {
                            $scope.gridApi.selection.toggleRowSelection(temp);
                        });
                    });
                }
            }
        };

        // Click handler for down button
        $scope.moveListenerDown = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.eventListeners.indexOf(selectedItems[0]);
                if (index != $scope.eventListeners.length - 1) { // If it's the last element, no moving down of course
                    var temp = $scope.eventListeners[index];
                    $scope.eventListeners.splice(index, 1);
                    $timeout(function () {
                        $scope.eventListeners.splice(index + 1, 0, temp);
                        $timeout(function () {
                            $scope.gridApi.selection.toggleRowSelection(temp);
                        });
                    });
                }
            }
        };

        // Click handler for save button
        $scope.save = function () {

            if ($scope.eventListeners.length > 0) {
                $scope.property.value = {};
                $scope.property.value.eventListeners = $scope.eventListeners;
            } else {
                $scope.property.value = null;
            }

            $scope.updatePropertyInModel($scope.property);
            $scope.close();
        };

        $scope.cancel = function () {
            $scope.property.mode = 'read';
            $scope.$hide();
        };

        // Close button handler
        $scope.close = function () {
            $scope.property.mode = 'read';
            $scope.$hide();
        };

    }]);
